package io.nichijou.tujian.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.billy.android.swipe.SwipeConsumer
import io.nichijou.tujian.ext.FragmentBackHandler
import io.nichijou.tujian.ext.handleBackPress
import io.nichijou.tujian.ext.target
import io.nichijou.tujian.ui.MainActivity
import me.yokeyword.fragmentation.SupportFragment

abstract class BaseFragment : SupportFragment(), FragmentBackHandler {
  protected abstract fun getFragmentViewId(): Int
  override fun onBackPressed(): Boolean = handleBackPress()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(getFragmentViewId(), container, false)
    view.setOnTouchListener { _, _ -> true }
    return view
  }

  protected fun setupDrawerWithToolbar(toolbar: Toolbar) {
    target().setSupportActionBar(toolbar)
    toolbar.setNavigationOnClickListener {
      MainActivity.swipeConsumer!!.open(true, SwipeConsumer.DIRECTION_LEFT)
    }
  }

}
