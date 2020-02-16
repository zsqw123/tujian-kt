package io.nichijou.tujian.widget

import android.content.Context
import android.util.AttributeSet
import androidx.core.view.setPadding
import androidx.lifecycle.Observer
import io.nichijou.oops.ext.activity
import io.nichijou.oops.widget.RecyclerView
import io.nichijou.tujian.StyleViewModel
import io.nichijou.tujian.common.ext.dp2px
import io.nichijou.tujian.common.ext.getStatusBarHeight

class List(context: Context, attrs: AttributeSet) : RecyclerView(context, attrs) {
  override fun liveInOops() {
    super.liveInOops()
    val activity = this.activity()
    StyleViewModel.live(activity).cardSpace.observe(this, Observer {
      val size = context.dp2px((16 - it / 100).toFloat()).toInt()
      if (paddingTop == paddingBottom) {
        setPadding(size)
      } else {
        setPadding(size, size + context.dp2px(58f).toInt() + context.getStatusBarHeight(), size, size)
      }
    })
  }
}
