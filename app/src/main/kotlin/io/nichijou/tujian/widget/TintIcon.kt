package io.nichijou.tujian.widget

import android.content.*
import android.graphics.drawable.*
import android.util.*
import androidx.lifecycle.*
import io.nichijou.oops.ext.*
import io.nichijou.oops.widget.*
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
