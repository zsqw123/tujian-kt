package io.nichijou.tujian.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.lifecycle.Observer
import io.nichijou.oops.ext.activity
import io.nichijou.oops.ext.applyOopsThemeStore
import io.nichijou.oops.widget.MaterialCardView
import io.nichijou.tujian.R
import kotlinx.android.synthetic.main.item_menu_hitokoto.view.*

class HitokotoItemView(context: Context, attrs: AttributeSet) : MaterialCardView(context, attrs) {

  override fun liveInOops() {
    this.activity().applyOopsThemeStore {
      isDark.observe(this@HitokotoItemView, Observer {
        if (it) {
          setBackgroundResource(R.drawable.bg_dark_hitokoto_gradient)
          hitokoto.setTextColor(Color.BLACK)
          source.setTextColor(Color.BLACK)
        } else {
          setBackgroundResource(R.drawable.bg_light_hitokoto_gradient)
          hitokoto.setTextColor(Color.WHITE)
          source.setTextColor(Color.WHITE)
        }
      })
    }
  }
}
