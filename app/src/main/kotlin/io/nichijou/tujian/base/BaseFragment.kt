package io.nichijou.tujian.base

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.billy.android.swipe.SwipeConsumer
import com.yarolegovich.slidingrootnav.util.DrawerToggleListenerAdapter
import io.nichijou.tujian.ext.FragmentBackHandler
import io.nichijou.tujian.ext.handleBackPress
import io.nichijou.tujian.ext.target
import io.nichijou.tujian.ui.MainActivity

abstract class BaseFragment : Fragment(), FragmentBackHandler {
  protected abstract fun getFragmentViewId(): Int

  protected abstract fun handleOnViewCreated()

  protected open fun getMenuResId() = NO_MENU

  protected open fun onMenuItemSelected(item: MenuItem) {}
  protected open fun needClearMenu() = true

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    if (needClearMenu()) menu.clear()
    if (getMenuResId() != NO_MENU) {
      inflater.inflate(getMenuResId(), menu)
    }
    super.onCreateOptionsMenu(menu, inflater)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    onMenuItemSelected(item)
    return super.onOptionsItemSelected(item)
  }

  protected open fun interceptTouch() = true

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(getFragmentViewId(), container, false)
    if (interceptTouch()) {
      view.setOnTouchListener { _, _ -> true }
    }
    handleOnCreateView(view)
    setHasOptionsMenu(true)
    return view
  }

  protected open fun handleOnCreateView(view: View) {
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    handleOnViewCreated()
  }

  protected fun close() {
    target().supportFragmentManager.popBackStack()
  }

  override fun onBackPressed(): Boolean {
    return interceptBackPressed() || handleBackPress()
  }

  open fun interceptBackPressed(): Boolean {
    return false
  }

  private lateinit var drawerToggleListenerAdapter: DrawerToggleListenerAdapter
  private lateinit var toggle: ActionBarDrawerToggle

  protected fun setupDrawerWithToolbar(toolbar: Toolbar) {
    val activity = target()
    activity.setSupportActionBar(toolbar)
    toolbar.setNavigationOnClickListener {
      MainActivity.swipeConsumer!!.open(true, SwipeConsumer.DIRECTION_LEFT)
    }
    if (activity is MainActivity) {
    }
  }

  companion object {
    private const val NO_MENU = -1
  }

  override fun onDestroyView() {
    val activity = target()
    if (activity is MainActivity && ::drawerToggleListenerAdapter.isInitialized) {
    }
    super.onDestroyView()
  }
}
