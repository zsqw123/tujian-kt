package io.nichijou.tujian.widget

import android.content.*
import android.util.*
import androidx.lifecycle.*
import com.google.android.material.shape.*
import io.nichijou.oops.ext.*
import io.nichijou.oops.widget.*
import io.nichijou.tujian.*

class Fab(context: Context, attrs: AttributeSet) : FloatingActionButton(context, attrs) {
  override fun liveInOops() {
    super.liveInOops()
    val activity = this.activity()
    StyleViewModel.live(activity).topBarRadius.observe(this, Observer {
      shapeAppearanceModel.toBuilder().apply {
        if (it==0){
          setAllCorners(CornerFamily.CUT, -1f)//默认为-1，会自动处理view大小的一半cut掉，若使用自定义，会出现阴影问题
        }
      }
    })
  }
}
