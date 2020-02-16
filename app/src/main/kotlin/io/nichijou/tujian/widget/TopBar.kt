package io.nichijou.tujian.widget

import android.content.Context
import android.util.AttributeSet
import androidx.lifecycle.Observer
import io.nichijou.oops.ext.activity
import io.nichijou.oops.widget.MaterialCardView
import io.nichijou.tujian.StyleViewModel

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
