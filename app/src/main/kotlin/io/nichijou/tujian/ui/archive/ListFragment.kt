package io.nichijou.tujian.ui.archive

import android.content.Context
import android.graphics.Point
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewpager.widget.PagerAdapter
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.bm.library.PhotoView
import com.bumptech.glide.Glide
import com.facebook.drawee.view.SimpleDraweeView
import com.zzhoujay.richtext.RichText
import io.nichijou.tujian.R
import io.nichijou.tujian.Settings
import io.nichijou.tujian.base.BaseFragment
import io.nichijou.tujian.common.entity.Picture
import io.nichijou.tujian.common.ext.dp2px
import io.nichijou.tujian.common.ext.logd
import io.nichijou.tujian.common.ext.sbl
import io.nichijou.tujian.common.fresco.load
import io.nichijou.tujian.paging.LoadState
import io.nichijou.tujian.paging.Status
import io.nichijou.viewer.Viewer
import jp.wasabeef.recyclerview.animators.LandingAnimator
import kotlinx.android.synthetic.main.fragment_list.*
import kotlinx.android.synthetic.main.photo_item_layout.view.*
import kotlinx.android.synthetic.main.photo_item_viewpager_layout.*
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.support.v4.viewPager
import org.koin.androidx.viewmodel.ext.android.viewModel

class ListFragment : BaseFragment() {
  companion object {
    private const val CATEGORY_ID = "category_id"
    fun newInstance(cid: String) = ListFragment().apply {
      arguments = bundleOf(CATEGORY_ID to cid)
    }
  }

  override fun getFragmentViewId(): Int = R.layout.fragment_list

  private val listViewModel by viewModel<ListViewModel>()

  override fun handleOnViewCreated() {
    initView()
    initViewModel()
  }

  override fun needClearMenu(): Boolean = false
  private lateinit var result: Listing<Picture>

  private fun initViewModel() {
    val cid = arguments?.getString(CATEGORY_ID)
    if (cid.isNullOrBlank()) {
      toast(R.string.no_category_id_available)
      return
    }
    result = listViewModel.get(cid)
    result.loadState.observe(this, Observer(::requestState))
    result.pagedList.observe(this, Observer {
      adapter.submitList(it)
    })
  }

  private fun requestState(state: LoadState) {
    when (state.status) {
      Status.RUNNING -> refresh_layout.isRefreshing = true
      Status.SUCCESS,
      Status.FINISHED -> {
        refresh_layout.isRefreshing = false
        viewer?.updateImages(adapter.currentList)
      }
      Status.FAILED -> {
        refresh_layout?.sbl("${state.msg}\n是否重试", "重试", {
          result.retry()
        }) {
          refresh_layout?.isRefreshing = false
        }
      }
    }
  }

  private var viewer: Viewer<Picture>? = null

  private val adapter by lazy(LazyThreadSafetyMode.NONE) {
    ListAdapter { v, a, pos ->
      val images = a.currentList ?: emptyList<Picture>()
//      val list = arrayListOf<Picture>()
//      list.addAll(images)
//      val images = this.currentList ?: emptyList<Picture>()
      val list = arrayListOf<Picture>()
      list.addAll(images)
      val point = Point()
      context!!.windowManager.defaultDisplay.getRealSize(point)
      val screenX = point.x

      val dialog = MaterialDialog(context!!)
        .customView(R.layout.photo_item_viewpager_layout)
        .title(text = "作者:" + list[pos].user)
      dialog.show {
        photo_item_viewpager.adapter = Viewpager2Adapter(list)
        photo_item_viewpager.layoutParams = LinearLayout.LayoutParams(screenX * 9 / 10, wrapContent)
        photo_item_viewpager.currentItem = pos
      }.cornerRadius((Settings.cardRadius / 100).toFloat())


//      viewer = Viewer.Builder(v.context, list, { itemView, dat ->
//        itemView.update(dat.local)
//      },
//        { drawee, dat ->
//          drawee.load(dat.local)
//        })
//        .withStartPosition(pos)
//        .withImageMarginPixels(v.context.dp2px(16f).toInt())
//        .withImageChangeListener {
//          logd("withImageChangeListener: $it")
//          recycler_view?.scrollToPosition(it)
//          val view = recycler_view?.layoutManager?.findViewByPosition(it)
//            ?.findViewById<SimpleDraweeView>(v.id)
//          if (view != null) {
//            viewer?.updateTransitionImage(view)
//          }
//        }
//        .withOnSingleTap {
//        }
//        .withTransitionFrom(v)
//        .show()
    }
  }

  private fun initView() {
    refresh_layout.setOnRefreshListener {
      result.refresh()
    }
    recycler_view.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
    recycler_view.adapter = adapter
    recycler_view.itemAnimator = LandingAnimator()
  }
}













