package io.nichijou.tujian.ui.upload

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import io.nichijou.oops.Oops
import io.nichijou.oops.ext.setMarginTopPlusStatusBarHeight
import io.nichijou.tujian.R
import io.nichijou.tujian.Settings
import io.nichijou.tujian.base.BaseFragment
import io.nichijou.tujian.common.entity.Upload
import io.nichijou.tujian.common.ext.makeGone
import io.nichijou.tujian.common.ext.makeVisible
import io.nichijou.tujian.common.fresco.load
import io.nichijou.tujian.ext.target
import io.nichijou.tujian.ui.MainViewModel
import kotlinx.android.synthetic.main.fragment_upload.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.toast
import org.koin.androidx.viewmodel.ext.android.viewModel

class UploadFragment : BaseFragment() {

  override fun getFragmentViewId(): Int = R.layout.fragment_upload
  private val mainViewModel by activityViewModels<MainViewModel>()
  private val uploadViewModel by viewModel<UploadViewModel>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    mainViewModel.enableScreenSaver.postValue(false)
    toolbar.setMarginTopPlusStatusBarHeight()
    lifecycleScope.launch(Dispatchers.IO) {
      val dark = Oops.immed().isDark
      withContext(Dispatchers.Main) {
        overlay?.apply {
          setBackgroundResource(if (dark) R.drawable.bg_dark_linear_gradient else R.drawable.bg_light_linear_gradient)
          animate().alpha(1f).setDuration(360).start()
        }
      }
    }
    setupDrawerWithToolbar(toolbar)
    //上传图片按钮
    overlay.setOnClickListener {
      val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
      intent.type = "image/*"
      startActivityForResult(intent, REQUEST_CODE)
    }
    uploadViewModel.msg.observe(viewLifecycleOwner, Observer { target().application.toast(it) })
    uploadViewModel.url.observe(viewLifecycleOwner, Observer {
      if (it.isNullOrBlank()) {
        field_url_wrapper?.makeGone()
        field_url?.setText(null)
      } else {
        field_url_wrapper?.makeVisible()
        field_url?.setText(it)
        toast("图片已上传 请填写信息")
        overlay_progress.makeGone()
      }
    })
    uploadViewModel.result.observe(viewLifecycleOwner, Observer {
      target().application.toast(it.toString())
    })
    submit?.setOnClickListener {
      if (!Settings.feiHua) {
        target().application.toast("未同意许可协议，无法投稿")
        return@setOnClickListener
      }
      val url = uploadViewModel.url.value
      if (url.isNullOrBlank()) {
        target().application.toast("请先选择待上传图片")
        return@setOnClickListener
      }
      val title = field_title.text.toString()
      if (title.isBlank()) {
        target().application.toast("请填写标题")
        return@setOnClickListener
      }
      val desc = field_desc.text.toString()
      if (desc.isBlank()) {
        target().application.toast("请填写描述")
        return@setOnClickListener
      }
      val poster = field_poster.text.toString()
      if (poster.isBlank()) {
        target().application.toast("请填写投稿人")
        return@setOnClickListener
      }
      val posterEmail = field_poster_email.text.toString()
      val upload = Upload(title, desc, url, poster, "4ac1c07f-a9f7-11e8-a8ea-0202761b0892", posterEmail)
      uploadViewModel.post(upload, { _, _ ->
        run { Looper.prepare();toast("投稿成功");Looper.loop() }
      }, { _, _ ->
        run { Looper.prepare();toast("投稿失败");Looper.loop() }
      })
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == REQUEST_CODE) {
      when (resultCode) {
        Activity.RESULT_OK -> {
          val uri = data?.data ?: return
          banner?.load(uri)
          field_url_wrapper?.makeGone()
          field_url?.text = null
          toast("正在上传图片")
          overlay_progress.makeVisible()
          uploadViewModel.upload(uri)
        }
        Activity.RESULT_CANCELED -> {
          toast(R.string.not_selected_picture)
        }
      }
    }
  }

  companion object {
    private const val REQUEST_CODE = 1
    fun newInstance() = UploadFragment()
  }
}
