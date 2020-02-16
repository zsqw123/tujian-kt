package io.nichijou.tujian.ui.bing

import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.nichijou.tujian.R
import io.nichijou.tujian.base.BaseFragment
import io.nichijou.tujian.ext.target
import jp.wasabeef.recyclerview.animators.LandingAnimator
import kotlinx.android.synthetic.main.fragment_bing.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class BingFragment : BaseFragment() {
  companion object {
    fun newInstance() = BingFragment()
  }

  override fun getFragmentViewId(): Int = R.layout.fragment_bing

  private val bingViewModel by viewModel<BingViewModel>()

  override fun handleOnViewCreated() {
    initView()
    initViewModel()
  }

  override fun needClearMenu(): Boolean = false

  private fun initViewModel() {
    bingViewModel.getBing().observe(this@BingFragment, Observer(adapter::submitList))
  }

  private val adapter by lazy(LazyThreadSafetyMode.NONE) { BingAdapter() }

  private fun initView() {
    recycler_view.layoutManager = LinearLayoutManager(target(), RecyclerView.VERTICAL, false)
    recycler_view.adapter = adapter
    recycler_view.itemAnimator = LandingAnimator()
  }
}
