package io.nichijou.tujian.ui.search

import android.graphics.Point
import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.animation.addListener
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import io.nichijou.tujian.R
import io.nichijou.tujian.base.BaseFragment
import io.nichijou.tujian.base.TopBarOnScrollListener
import io.nichijou.tujian.common.ext.getRadiusByCenterPoint
import io.nichijou.tujian.common.ext.postApply
import io.nichijou.tujian.common.ext.setMarginTopPlusStatusBarHeight
import io.nichijou.tujian.ext.target
import io.nichijou.tujian.ui.MainViewModel
import io.nichijou.tujian.ui.history.HistoryViewModel
import jp.wasabeef.recyclerview.animators.LandingAnimator
import kotlinx.android.synthetic.main.fragment_history.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : BaseFragment() {
  companion object {
    private const val ANIM_DURATION = 560L
    private const val START_POINT = "START_POINT"
    fun newInstance(point: Point) = SearchFragment().apply {
      arguments = bundleOf(START_POINT to point)
    }
  }

  private val mainViewModel by activityViewModels<MainViewModel>()
  override fun getFragmentViewId(): Int = R.layout.fragment_history
  private val historyViewModel by viewModel<HistoryViewModel>()
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    startPoint = arguments?.getParcelable(START_POINT)
      ?: throw IllegalStateException("start point is NULL")
    initView()
    initViewModel()
    toggleAnimEnterExit(true)
  }

  override fun onBackPressed(): Boolean {
    toggleAnimEnterExit(false) {
      content_wrapper?.alpha = 0f
      target().supportFragmentManager.popBackStack()
    }
    return true
  }

  private lateinit var startPoint: Point

  private fun toggleAnimEnterExit(isEnter: Boolean, onEnd: (() -> Unit)? = null) {
    content_wrapper?.postApply {
      if (!this.isAttachedToWindow) return@postApply
      if (isEnter) {
        this.animate().alpha(1f).setDuration(ANIM_DURATION).start()
      }
      val finalRadius = context.getRadiusByCenterPoint(startPoint)
      val animator = ViewAnimationUtils.createCircularReveal(this, startPoint.x, startPoint.y, if (isEnter) 0f else finalRadius, if (isEnter) finalRadius else 0f)
      animator.interpolator = AccelerateDecelerateInterpolator()
      animator.addListener(onEnd = {
        if (!isEnter) {
          onEnd?.invoke()
        }
      })
      animator.setDuration(ANIM_DURATION).start()
    }
  }

  private fun initViewModel() {
    historyViewModel.getHistory().observe(viewLifecycleOwner, Observer {
      //      recycler_view?.adapter = ListAdapter(it)
    })
  }

  private fun initView() {
    setupDrawerWithToolbar(toolbar)
    top_bar.setMarginTopPlusStatusBarHeight()
    mainViewModel.enableScreenSaver.postValue(true)
    recycler_view.addOnScrollListener(TopBarOnScrollListener(top_bar))
    recycler_view.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
    recycler_view.itemAnimator = LandingAnimator()
  }
}


