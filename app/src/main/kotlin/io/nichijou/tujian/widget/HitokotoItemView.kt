package io.nichijou.tujian.widget

import android.content.*
import android.graphics.*
import android.util.*
import androidx.lifecycle.*
import io.nichijou.oops.ext.*
import io.nichijou.oops.widget.*
import io.nichijou.tujian.R
import io.nichijou.tujian.common.entity.*
import io.nichijou.tujian.common.ext.*
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

  fun setHitokoto(hi: Hitokoto) {
    hitokoto.text = hi.hitokoto
    if (hi.source.isBlank()) {
      source.makeGone()
    } else {
      source.makeVisible()
    }
    source.text = hi.source
  }
}
