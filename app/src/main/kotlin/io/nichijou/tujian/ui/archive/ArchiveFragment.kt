package io.nichijou.tujian.ui.archive

import android.graphics.Color
import android.graphics.Point
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import io.nichijou.oops.Oops
import io.nichijou.oops.ext.setPaddingTopPlusStatusBarHeight
import io.nichijou.tujian.R
import io.nichijou.tujian.base.BaseFragment
import io.nichijou.tujian.common.entity.Category
import io.nichijou.tujian.common.ext.postApply
import io.nichijou.tujian.ext.addFragmentToActivity
import io.nichijou.tujian.ui.MainViewModel
import io.nichijou.tujian.ui.bing.BingFragment
import io.nichijou.tujian.ui.history.HistoryFragment
import kotlinx.android.synthetic.main.fragment_archive.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.support.v4.toast
import org.koin.androidx.viewmodel.ext.android.viewModel


class ArchiveFragment : BaseFragment() {
  companion object {
    fun newInstance() = ArchiveFragment()
  }

  private val mainViewModel by activityViewModels<MainViewModel>()

  override fun getFragmentViewId(): Int = R.layout.fragment_archive
  override fun getMenuResId(): Int = R.menu.menu_archive
  override fun onMenuItemSelected(item: MenuItem) {
    toolbar?.postApply {
      val menu = findViewById<View>(item.itemId)
      val location = IntArray(2)
      menu.getLocationOnScreen(location)
      val point = Point(location[0], location[1])
      when (item.itemId) {
        R.id.action_history -> {
          this@ArchiveFragment.addFragmentToActivity(HistoryFragment.newInstance(point))
        }
//        R.id.action_search -> {
//        }
      }
    }
  }

  override fun handleOnViewCreated() {
    top_bar.setPaddingTopPlusStatusBarHeight()
    setupDrawerWithToolbar(toolbar)
    mainViewModel.barColor.postValue(if (Oops.immed().isDark) Color.BLACK else Color.WHITE)
    mainViewModel.enableScreenSaver.postValue(true)
    initViewModel()
  }

  private fun bind2View(categories: List<Category>) {
    if (categories.isEmpty()) {
      toast(getString(R.string.no_category_info_available))
      return
    }
    val fragments: MutableList<Fragment> =
      categories.map { ListFragment.newInstance(it.tid) }.toMutableList()
    fragments.add(BingFragment.newInstance())
    view_pager.adapter = object : FragmentStateAdapter(this) {
      override fun createFragment(position: Int): Fragment = fragments[position]
      override fun getItemCount(): Int = fragments.size
    }
    view_pager.offscreenPageLimit = Int.MAX_VALUE
    TabLayoutMediator(tab, view_pager) { tab, position ->
      tab.text = if (position < categories.size) categories[position].name else getString(R.string.bing)
    }.attach()
  }

  private var categories = hashSetOf<Pair<String, String>>()
  private val categoryViewModel by viewModel<ArchiveViewModel>()

  private fun initViewModel() {
    categoryViewModel.categories.observe(this, Observer {
      lifecycleScope.launch {
        if (it.isNullOrEmpty()) {
          return@launch
        }
        val cats = it.map { c -> Pair(c.tid, c.name) }.toHashSet()
        if (categories.containsAll(cats)) {
          return@launch
        }
        categories = cats
        withContext(context = Dispatchers.Main) {
          bind2View(it)
        }
      }
    })
  }
}
