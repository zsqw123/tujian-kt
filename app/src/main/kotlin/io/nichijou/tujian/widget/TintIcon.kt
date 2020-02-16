package io.nichijou.tujian.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.lifecycle.Observer
import io.nichijou.oops.ext.activity
import io.nichijou.oops.ext.applyOopsThemeStore
import io.nichijou.oops.ext.attrValue
import io.nichijou.oops.ext.tint
import io.nichijou.oops.widget.ImageView
import io.nichijou.tujian.R

class TintIcon(context: Context, attrs: AttributeSet?) : ImageView(context, attrs) {
  private val tint = context.attrValue(attrs, R.attr.tint)
  private var tintColor = 0

  override fun liveInOops() {
    this.activity().applyOopsThemeStore {
      live(tint, textColorPrimary)!!.observe(this@TintIcon, Observer {
        tintColor = it
        setImageDrawable(drawable)
      })
    }
  }

  override fun setImageDrawable(drawable: Drawable?) {
    super.setImageDrawable(drawable?.tint(tintColor))
  }
}
