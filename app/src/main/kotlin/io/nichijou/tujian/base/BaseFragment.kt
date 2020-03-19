package io.nichijou.tujian.base

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.*
import androidx.appcompat.widget.Toolbar
import io.nichijou.tujian.ext.FragmentBackHandler
import io.nichijou.tujian.ext.target
import io.nichijou.tujian.isDark
import io.nichijou.tujian.ui.MainActivity
import me.yokeyword.fragmentation.SupportActivity
import me.yokeyword.fragmentation.SupportFragment
import org.jetbrains.anko.support.v4.longToast
import kotlin.system.exitProcess

abstract class BaseFragment : SupportFragment(), FragmentBackHandler {
  protected abstract fun getFragmentViewId(): Int
  private var isExit = false
  override fun onBackPressed(): Boolean = false
  override fun onBackPressedSupport(): Boolean {
    if (MainActivity.swipeConsumer != null) {
      MainActivity.swipeConsumer!!.enableHorizontal()
      if (MainActivity.swipeConsumer!!.isOpened) {
        MainActivity.swipeConsumer!!.smoothClose()
      } else if (MainActivity.nowFragment != MainActivity.mFragments[0]) {
        MainActivity.showHideListener(target() as SupportActivity, MainActivity.mFragments[0]!!)
      } else if (!isExit) {// 双击退出
        isExit = true
        longToast("再按一次退出图鉴日图")
        Handler().postDelayed({ isExit = false }, 2000)
      } else {
        exitProcess(0)
      }
    }
    return true
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(getFragmentViewId(), container, false)
    view.setOnTouchListener { _, _ -> true }
    view.setBackgroundColor(if (isDark()) Color.BLACK else Color.WHITE)
    return view
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    super.onCreateOptionsMenu(menu, inflater)
    menu.clear()
  }

  protected fun setupDrawerWithToolbar(toolbar: Toolbar) {
    MainActivity.swipeConsumer!!.enableHorizontal()
    target().setSupportActionBar(toolbar)
    toolbar.setNavigationOnClickListener {
      MainActivity.swipeConsumer!!.smoothLeftOpen()
    }
  }

  protected fun setupBackToolbar(toolbar: Toolbar) {
    MainActivity.swipeConsumer!!.disableAllDirections()
    target().setSupportActionBar(toolbar)
    toolbar.setNavigationOnClickListener { pop() }
  }
}
