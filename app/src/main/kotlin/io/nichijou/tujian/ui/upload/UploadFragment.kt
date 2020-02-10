package io.nichijou.tujian.ui.upload

import android.app.*
import android.content.*
import android.graphics.*
import androidx.fragment.app.*
import androidx.lifecycle.*
import io.nichijou.oops.*
import io.nichijou.oops.ext.*
import io.nichijou.tujian.R
import io.nichijou.tujian.base.*
import io.nichijou.tujian.common.entity.*
import io.nichijou.tujian.common.ext.*
import io.nichijou.tujian.common.fresco.*
import io.nichijou.tujian.ext.*
import io.nichijou.tujian.ui.*
import kotlinx.android.synthetic.main.fragment_upload.*
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.*


class UploadFragment : BaseFragment() {

  override fun getFragmentViewId(): Int = R.layout.fragment_upload
  private val mainViewModel by activityViewModels<MainViewModel>()
  private val uploadViewModel by viewModel<UploadViewModel>()

  override fun handleOnViewCreated() {
    mainViewModel.barColor.postValue(Color.TRANSPARENT)
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
    overlay.setOnClickListener {
      val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
      intent.type = "image/*"
      startActivityForResult(intent, REQUEST_CODE)
    }
    uploadViewModel.msg.observe(this, Observer { target().application.toast(it) })
    uploadViewModel.url.observe(this, Observer {
      if (it.isNullOrBlank()) {
        field_url_wrapper?.makeGone()
        field_url?.setText(null)
      } else {
        field_url_wrapper?.makeVisible()
        field_url?.setText(it)
      }
    })
    uploadViewModel.result.observe(this, Observer {
      target().application.toast(it.toString())
    })
    submit?.setOnClickListener {
      val url = uploadViewModel.url.value
      if (url.isNullOrBlank()) {
        target().application.toast("请先选择待上传图片")
        return@setOnClickListener
      }
      val title = field_title?.text
      if (title.isNullOrBlank()) {
        target().application.toast("请填写标题")
        return@setOnClickListener
      }
      val desc = field_desc?.text
      if (desc.isNullOrBlank()) {
        target().application.toast("请填写描述")
        return@setOnClickListener
      }
      val poster = field_poster?.text
      if (poster.isNullOrBlank()) {
        target().application.toast("请填写投稿人")
        return@setOnClickListener
      }
      val posterEmail = field_poster_email?.text
      val upload = Upload(title, desc, url, poster, "4ac1c07f-a9f7-11e8-a8ea-0202761b0892", posterEmail)
      uploadViewModel.post(upload)
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
