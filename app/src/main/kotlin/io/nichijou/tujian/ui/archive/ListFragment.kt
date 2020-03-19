package io.nichijou.tujian.ui.archive

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.gms.ads.formats.UnifiedNativeAd
import io.nichijou.tujian.R
import io.nichijou.tujian.base.BaseFragment
import io.nichijou.tujian.common.entity.Picture
import io.nichijou.tujian.common.ext.sbl
import io.nichijou.tujian.ext.addFragmentToActivity
import io.nichijou.tujian.paging.LoadState
import io.nichijou.tujian.paging.Status
import jp.wasabeef.recyclerview.animators.LandingAnimator
import kotlinx.android.synthetic.main.fragment_list.*
import org.jetbrains.anko.support.v4.toast
import org.koin.androidx.viewmodel.ext.android.viewModel

class ListFragment : BaseFragment() {
  companion object {
    private const val CATEGORY_ID = "category_id"
    fun newInstance(cid: String, nativeAd: UnifiedNativeAd?) = ListFragment().apply {
      arguments = bundleOf(CATEGORY_ID to cid)
    }
  }

  override fun getFragmentViewId(): Int = R.layout.fragment_list

  private val listViewModel by viewModel<ListViewModel>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initView()
    initViewModel()
  }

  private lateinit var result: Listing<Picture>

  private fun initViewModel() {
    val cid = arguments?.getString(CATEGORY_ID)
    if (cid.isNullOrBlank()) {
      toast(R.string.no_category_id_available)
      return
    }
    result = listViewModel.get(cid)
    result.loadState.observe(viewLifecycleOwner, Observer(::requestState))
    result.pagedList.observe(viewLifecycleOwner, Observer {
      adapter.submitList(it)
    })
  }

  private fun requestState(state: LoadState) {
    when (state.status) {
      Status.RUNNING -> refresh_layout.isRefreshing = true
      Status.SUCCESS,
      Status.FINISHED -> {
        refresh_layout.isRefreshing = false
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

  private val adapter by lazy(LazyThreadSafetyMode.NONE) {
    ListAdapter { _, a, pos ->
      val images = a.currentList ?: emptyList<Picture>()
      val list = arrayListOf<Picture>()
      list.addAll(images)
      val photoItem = PhotoItem.newInstance(list, pos)
      addFragmentToActivity(photoItem)
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













