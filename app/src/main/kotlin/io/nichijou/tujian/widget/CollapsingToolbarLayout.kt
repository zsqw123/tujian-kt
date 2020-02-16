package io.nichijou.tujian.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.annotation.Nullable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.Observer
import com.google.android.material.appbar.CollapsingToolbarLayout
import io.nichijou.oops.KEY_DEFAULT_COLLAPSING_TOOLBAR_TAG
import io.nichijou.oops.OopsLifecycleOwner
import io.nichijou.oops.ext.*

class CollapsingToolbarLayout @JvmOverloads constructor(context: Context, @Nullable attrs: AttributeSet? = null) : CollapsingToolbarLayout(context, attrs), OopsLifecycleOwner {
  private val backgroundAttrValue = context.attrValue(attrs, android.R.attr.background)
  private val lifecycleRegistry = LifecycleRegistry(this)

  override fun liveInOops() {
    val tag = if (this.tag == null) {
      KEY_DEFAULT_COLLAPSING_TOOLBAR_TAG
    } else {
      this.tag.toString()
    }
    this.activity().applyOopsThemeStore {
      collapsingToolbarStateColor(tag, live(backgroundAttrValue, colorPrimary)!!)
        .observe(this@CollapsingToolbarLayout, Observer {
          setContentScrimColor(it.bgColor)
          setBackgroundColor(it.bgColor)
          setStatusBarScrimColor(it.statusBarColor)
        })
    }
  }

  override fun getLifecycle(): Lifecycle = lifecycleRegistry

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    attachOopsLife()
  }

  override fun onVisibilityChanged(changedView: View, visibility: Int) {
    if (visibility == View.VISIBLE) {
      super.onVisibilityChanged(changedView, visibility)
      changedView.resumeOopsLife()
    } else {
      changedView.pauseOopsLife()
      super.onVisibilityChanged(changedView, visibility)
    }
  }

  override fun onWindowVisibilityChanged(visibility: Int) {
    if (visibility == View.VISIBLE) {
      super.onWindowVisibilityChanged(visibility)
      resumeOopsLife()
    } else {
      pauseOopsLife()
      super.onWindowVisibilityChanged(visibility)
    }
  }

  override fun onDetachedFromWindow() {
    detachOopsLife()
    super.onDetachedFromWindow()
  }
}
