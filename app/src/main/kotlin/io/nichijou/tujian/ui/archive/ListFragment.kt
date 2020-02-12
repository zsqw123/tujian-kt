package io.nichijou.tujian.ui.archive

import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.facebook.drawee.view.SimpleDraweeView
import io.nichijou.tujian.R
import io.nichijou.tujian.base.BaseFragment
import io.nichijou.tujian.common.entity.Picture
import io.nichijou.tujian.common.ext.*
import io.nichijou.tujian.common.fresco.load
import io.nichijou.tujian.paging.LoadState
import io.nichijou.tujian.paging.Status
import io.nichijou.viewer.Viewer
import jp.wasabeef.recyclerview.animators.LandingAnimator
import kotlinx.android.synthetic.main.fragment_list.*
import org.jetbrains.anko.support.v4.toast
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
    ListAdapter { v, a, position ->
      val images = a.currentList ?: emptyList<Picture>()
      val list = arrayListOf<Picture>()
      list.addAll(images)
      viewer = Viewer.Builder(v.context, list, { itemView, dat ->
        itemView.update(dat.local)
      },
        { drawee, dat ->
          drawee.load(dat.local)
        })
        .withStartPosition(position)
        .withImageMarginPixels(v.context.dp2px(16f).toInt())
        .withImageChangeListener {
          logd("withImageChangeListener: $it")
          recycler_view?.scrollToPosition(it)
          val view = recycler_view?.layoutManager?.findViewByPosition(it)
            ?.findViewById<SimpleDraweeView>(v.id)
          if (view != null) {
            viewer?.updateTransitionImage(view)
          }
        }
        .withOnSingleTap{
        }
        .withTransitionFrom(v)
        .show()
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














