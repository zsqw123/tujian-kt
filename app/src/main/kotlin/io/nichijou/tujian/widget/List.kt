package io.nichijou.tujian.widget

import android.content.*
import android.util.*
import androidx.core.view.*
import androidx.lifecycle.*
import io.nichijou.oops.ext.*
import io.nichijou.oops.widget.*
import io.nichijou.tujian.*
import io.nichijou.tujian.common.ext.*

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
