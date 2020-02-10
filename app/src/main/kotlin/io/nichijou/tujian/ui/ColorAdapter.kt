package io.nichijou.tujian.ui

import android.view.*
import androidx.palette.graphics.*
import androidx.recyclerview.widget.*
import com.google.android.flexbox.*
import io.nichijou.oops.*
import io.nichijou.tujian.R
import io.nichijou.tujian.common.ext.*
import io.nichijou.utils.*
import kotlinx.coroutines.*

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
