package io.nichijou.tujian.ui.search

import android.graphics.*
import android.view.*
import android.view.animation.*
import androidx.core.animation.*
import androidx.core.os.*
import androidx.fragment.app.*
import androidx.lifecycle.*
import androidx.recyclerview.widget.*
import io.nichijou.oops.*
import io.nichijou.tujian.R
import io.nichijou.tujian.base.*
import io.nichijou.tujian.common.ext.*
import io.nichijou.tujian.ui.*
import io.nichijou.tujian.ui.history.*
import jp.wasabeef.recyclerview.animators.*
import kotlinx.android.synthetic.main.fragment_history.*
import org.koin.androidx.viewmodel.ext.android.*

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
  override fun handleOnViewCreated() {
    startPoint = arguments?.getParcelable<Point>(START_POINT)
      ?: throw IllegalStateException("start point is NULL")
    initView()
    initViewModel()
    toggleAnimEnterExit(true)
  }

  override fun interceptBackPressed(): Boolean {
    toggleAnimEnterExit(false) {
      content_wrapper?.alpha = 0f
      close()
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
        if (isEnter) {
        } else {
          onEnd?.invoke()
        }
      })
      animator.setDuration(ANIM_DURATION).start()
    }
  }

  private fun initViewModel() {
    historyViewModel.getHistory().observe(this@SearchFragment, Observer {
      //      recycler_view?.adapter = ListAdapter(it)
    })
  }

  private fun initView() {
    setupDrawerWithToolbar(toolbar)
    top_bar.setMarginTopPlusStatusBarHeight()// 加上一个状态栏高度的margin
    mainViewModel.barColor.postValue(if (Oops.immed().isDark) Color.BLACK else Color.WHITE)
    mainViewModel.enableScreenSaver.postValue(true)
    recycler_view.addOnScrollListener(TopBarOnScrollListener(top_bar))
    recycler_view.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
    recycler_view.itemAnimator = LandingAnimator()
  }
}


