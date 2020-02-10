package com.yarolegovich.slidingrootnav

import android.app.*
import android.os.*
import android.view.*
import androidx.annotation.*
import androidx.annotation.IntRange
import androidx.appcompat.widget.*
import androidx.drawerlayout.widget.*
import com.yarolegovich.slidingrootnav.transform.*
import com.yarolegovich.slidingrootnav.util.*
import java.util.*

class SlidingDrawer(private val activity: Activity) {

  private var contentView: ViewGroup? = null

  private var menuView: View? = null

  private var menuLayoutRes: Int = 0

  private val transformations: MutableList<RootTransformation>

  private val drawerListeners: MutableList<DrawerLayout.DrawerListener>

  private var dragDistance: Int = 0

  private var toolbar: Toolbar? = null

  private var gravity: SlideGravity? = null

  private var isMenuOpened: Boolean = false

  private var isMenuLocked: Boolean = false

  private var isContentClickableWhenMenuOpened: Boolean = false

  private var savedState: Bundle? = null
  private lateinit var root: SlidingDrawerLayout
  private lateinit var menu: View

  init {
    this.transformations = ArrayList()
    this.drawerListeners = ArrayList()
    this.gravity = SlideGravity.START
    this.dragDistance = dpToPx(DEFAULT_DRAG_DIST_DP)
    this.isContentClickableWhenMenuOpened = true
  }

  fun withMenuView(view: View): SlidingDrawer {
    menuView = view
    return this
  }

  fun withMenuLayout(@LayoutRes layout: Int): SlidingDrawer {
    menuLayoutRes = layout
    return this
  }

  fun withToolbarMenuToggle(tb: Toolbar): SlidingDrawer {
    toolbar = tb
    return this
  }

  fun withGravity(g: SlideGravity): SlidingDrawer {
    gravity = g
    return this
  }

  fun withContentView(cv: ViewGroup): SlidingDrawer {
    contentView = cv
    return this
  }

  fun withMenuLocked(locked: Boolean): SlidingDrawer {
    isMenuLocked = locked
    return this
  }

  fun withSavedState(state: Bundle?): SlidingDrawer {
    savedState = state
    return this
  }

  fun withMenuOpened(opened: Boolean): SlidingDrawer {
    isMenuOpened = opened
    return this
  }

  fun withContentClickableWhenMenuOpened(clickable: Boolean): SlidingDrawer {
    isContentClickableWhenMenuOpened = clickable
    return this
  }

  fun withDragDistance(dp: Int): SlidingDrawer {
    return withDragDistancePx(dpToPx(dp))
  }

  private fun withDragDistancePx(px: Int): SlidingDrawer {
    dragDistance = px
    return this
  }

  fun withRootViewScale(@FloatRange(from = 0.01) scale: Float): SlidingDrawer {
    transformations.add(ScaleTransformation(scale))
    return this
  }

  fun withRootViewElevation(@IntRange(from = 0) elevation: Int): SlidingDrawer {
    return withRootViewElevationPx(dpToPx(elevation))
  }

  private fun withRootViewElevationPx(@IntRange(from = 0) elevation: Int): SlidingDrawer {
    transformations.add(ElevationTransformation(elevation.toFloat()))
    return this
  }

  fun withRootViewYTranslation(translation: Int): SlidingDrawer {
    return withRootViewYTranslationPx(dpToPx(translation))
  }

  private fun withRootViewYTranslationPx(translation: Int): SlidingDrawer {
    transformations.add(YTranslationTransformation(translation.toFloat()))
    return this
  }

  fun addRootTransformation(transformation: RootTransformation): SlidingDrawer {
    transformations.add(transformation)
    return this
  }

  fun withDrawerListener(drawerListener: DrawerLayout.DrawerListener): SlidingDrawer {
    drawerListeners.add(drawerListener)
    return this
  }

  fun addDrawerListener(drawerListener: DrawerLayout.DrawerListener) {
    root.addDrawerListener(drawerListener)
  }

  fun removeDrawerListener(drawerListener: DrawerLayout.DrawerListener) {
    root.removeDrawerListener(drawerListener)
  }

  fun setDispatchTouch(dispatch: (MotionEvent?) -> Unit) {
    root.setDispatchTouch(dispatch)
  }

  fun isMenuOpened(): Boolean {
    return root.isMenuOpened()
  }

  fun isMenuLocked(): Boolean {
    return root.isMenuLocked()
  }

  fun setMenuLocked(locked: Boolean) {
    root.setMenuLocked(locked)
  }

  fun closeMenu() {
    root.closeMenu()
  }

  fun closeMenu(animated: Boolean) {
    root.closeMenu(animated)
  }

  fun openMenu() {
    root.openMenu()
  }

  fun openMenu(animated: Boolean) {
    root.openMenu(animated)
  }

  fun getToolbar(): Toolbar? {
    return toolbar
  }

  fun getMenuView(): View {
    return menu
  }

  fun getDrawer(): SlidingDrawerLayout {
    return root
  }

  fun inject(): SlidingDrawer {
    val contentView = getContentView()

    val oldRoot = contentView.getChildAt(0)
    contentView.removeAllViews()

    root = createAndInitNewRoot(oldRoot)
    menu = getMenuViewFor(root)

    val clickConsumer = HiddenMenuClickConsumer(activity, root)

    root.addView(menu)
    root.addView(clickConsumer)
    root.addView(oldRoot)

    contentView.addView(root)

    if (savedState == null && isMenuOpened) {
      root.openMenu(false)
    }

    root.setMenuLocked(isMenuLocked)

    return this
  }

  private fun createAndInitNewRoot(oldRoot: View): SlidingDrawerLayout {
    val newRoot = SlidingDrawerLayout(activity)
    newRoot.id = R.id.srn_root_layout
    newRoot.setRootTransformation(createCompositeTransformation())
    newRoot.setMaxDragDistance(dragDistance)
    if (gravity != null) newRoot.setGravity(gravity!!)
    newRoot.contentView = oldRoot
    newRoot.setContentClickableWhenMenuOpened(isContentClickableWhenMenuOpened)
    for (l in drawerListeners) {
      newRoot.addDrawerListener(l)
    }
    return newRoot
  }

  private fun getContentView(): ViewGroup {
    if (contentView == null) {
      contentView = activity.findViewById(android.R.id.content)
    }
    if (contentView!!.childCount != 1) {
      throw IllegalStateException(activity.getString(R.string.srn_ex_bad_content_view))
    }
    return contentView!!
  }

  private fun getMenuViewFor(parent: SlidingDrawerLayout?): View {
    if (menuView == null) {
      if (menuLayoutRes == 0) {
        throw IllegalStateException(activity.getString(R.string.srn_ex_no_menu_view))
      }
      menuView = LayoutInflater.from(activity).inflate(menuLayoutRes, parent, false)
    }
    return menuView!!
  }

  private fun createCompositeTransformation(): RootTransformation {
    return if (transformations.isEmpty()) {
      CompositeTransformation(Arrays.asList(
        ScaleTransformation(DEFAULT_END_SCALE),
        ElevationTransformation(dpToPx(DEFAULT_END_ELEVATION_DP).toFloat())))
    } else {
      CompositeTransformation(transformations)
    }
  }


  private fun dpToPx(dp: Int): Int {
    return Math.round(activity.resources.displayMetrics.density * dp)
  }

  companion object {
    private const val DEFAULT_END_SCALE = 0.65f
    private const val DEFAULT_END_ELEVATION_DP = 8
    private const val DEFAULT_DRAG_DIST_DP = 180
  }

}
