package io.nichijou.tujian.ui

import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import io.nichijou.oops.Oops
import io.nichijou.tujian.R
import io.nichijou.utils.bodyColor
import io.nichijou.utils.titleColor
import io.nichijou.utils.toHexColor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.toast

class ColorAdapter(private val colors: List<Palette.Swatch>, private val width: Int, private val height: Int) : RecyclerView.Adapter<ColorAdapter.ViewHolder>() {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(View(parent.context).apply {
    val layoutParams = FlexboxLayoutManager.LayoutParams(this@ColorAdapter.width, this@ColorAdapter.height)
    layoutParams.flexShrink = 1f
    layoutParams.flexGrow = 1f
    this.layoutParams = layoutParams
  })

  override fun getItemCount(): Int = colors.size

  override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(colors[position])

  class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(swatch: Palette.Swatch) {
      val rgb = swatch.rgb
      itemView.setBackgroundColor(rgb)
      itemView.setOnClickListener {
        it.context.run {
          toast(getString(R.string.apply_theme_color).format(rgb.toHexColor()))
        }
        GlobalScope.launch {
          //这里异步切换主题色，自生自灭
          Oops.bulk {
            colorAccent = rgb
            toolbarIconColor = rgb
            toolbarTitleColor = rgb
            snackbarBackgroundColor = rgb
            bottomNavigationViewSelectedColor = rgb
            snackbarTextColor = rgb.bodyColor()
            snackbarActionColor = rgb.titleColor()
          }
        }
      }
    }
  }
}

fun Bitmap.doPalettes(method: (p: Palette?) -> Unit) {
  Palette.from(this).generate { palette ->
    method(palette)
  }
}
//    val s = palette?.dominantSwatch //独特的一种
//    val s1 = palette?.vibrantSwatch //获取到充满活力的这种色调
//    val s2 = palette?.darkVibrantSwatch //获取充满活力的黑
//    val s3 = palette?.lightVibrantSwatch //获取充满活力的亮
//    val s4 = palette?.mutedSwatch //获取柔和的色调
//    val s5 = palette?.darkMutedSwatch //获取柔和的黑
//    val s6 = palette?.lightMutedSwatch //获取柔和的亮

