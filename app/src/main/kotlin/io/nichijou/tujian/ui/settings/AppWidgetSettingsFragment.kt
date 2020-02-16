package io.nichijou.tujian.ui.settings

import android.graphics.Color
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import io.nichijou.oops.Oops
import io.nichijou.oops.ext.setMarginTopPlusStatusBarHeight
import io.nichijou.tujian.R
import io.nichijou.tujian.base.BaseFragment
import io.nichijou.tujian.ui.MainViewModel
import kotlinx.android.synthetic.main.fragment_settings_appwidget.*

class AppWidgetSettingsFragment : BaseFragment() {
  companion object {
    fun newInstance() = AppWidgetSettingsFragment()
  }

  private val mainViewModel by activityViewModels<MainViewModel>()

  override fun getFragmentViewId(): Int = R.layout.fragment_settings_appwidget

  private val adapter by lazy(LazyThreadSafetyMode.NONE) {
    val fragments = arrayOf<Fragment>(TujianAppWidgetSettingsFragment.newInstance(), BingAppWidgetSettingsFragment.newInstance(), HitokotoAppWidgetSettingsFragment.newInstance())
    object : FragmentStateAdapter(this@AppWidgetSettingsFragment) {
      override fun getItemCount(): Int = fragments.size
      override fun createFragment(position: Int): Fragment = fragments[position]
    }
  }

  override fun handleOnViewCreated() {
    top_bar.setMarginTopPlusStatusBarHeight()
    setupDrawerWithToolbar(toolbar)
    mainViewModel.barColor.postValue(if (Oops.immed().isDark) Color.BLACK else Color.WHITE)
    mainViewModel.enableScreenSaver.postValue(true)
    view_pager.adapter = adapter
    view_pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
      override fun onPageSelected(position: Int) {
        bnv?.menu?.getItem(position)?.isChecked = true
      }
    })
    view_pager.offscreenPageLimit = Int.MAX_VALUE
    bnv.setOnNavigationItemSelectedListener {
      when (it.itemId) {
        R.id.action_tujian -> {
          view_pager?.currentItem = 0
        }
        R.id.action_bing -> {
          view_pager?.currentItem = 1
        }
        R.id.action_hitokoto -> {
          view_pager?.currentItem = 2
        }
      }
      true
    }
    view_pager?.animate()?.alpha(1f)?.setDuration(120)?.start()
    lifecycleScope.launchWhenResumed {
      view_pager?.currentItem = 1
    }
  }
}
