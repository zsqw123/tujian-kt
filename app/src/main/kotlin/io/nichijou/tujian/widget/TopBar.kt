package io.nichijou.tujian.widget

import android.content.*
import android.util.*
import androidx.lifecycle.*
import io.nichijou.oops.ext.*
import io.nichijou.oops.widget.*
import io.nichijou.tujian.*

class TopBar(context: Context, attrs: AttributeSet) : MaterialCardView(context, attrs) {

  override fun liveInOops() {
    super.liveInOops()
    val activity = this.activity()
    val live = StyleViewModel.live(activity)
    live.topBarRadius.observe(this, Observer {
      radius = (it / 100).toFloat()
    })
    live.topBarElevation.observe(this, Observer {
      cardElevation = (it / 100).toFloat()
    })
  }
}
