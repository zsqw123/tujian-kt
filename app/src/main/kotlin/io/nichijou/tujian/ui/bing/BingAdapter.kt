package io.nichijou.tujian.ui.bing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.drawable.ProgressBarDrawable
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import io.nichijou.tujian.R
import io.nichijou.tujian.common.entity.Bing
import io.nichijou.tujian.common.ext.dp2px
import io.nichijou.tujian.common.ext.makeGone
import io.nichijou.tujian.common.ext.makeVisible
import io.nichijou.tujian.common.fresco.getPaletteSwatches
import io.nichijou.tujian.common.fresco.load
import io.nichijou.tujian.diff.BingDiffCallback
import io.nichijou.tujian.ui.ColorAdapter
import io.nichijou.utils.randomColor
import jp.wasabeef.recyclerview.animators.LandingAnimator
import kotlinx.android.synthetic.main.item_bing.view.*
import java.util.*

class BingAdapter : PagedListAdapter<Bing, BingAdapter.ViewHolder>(BingDiffCallback()) {

  private val cacheColors = WeakHashMap<String, List<Palette.Swatch>>()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_bing, parent, false))

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val bing = getItem(position) ?: return
    val colors = cacheColors[bing.url]
    if (colors.isNullOrEmpty()) {
      holder.itemView.colors?.makeGone()
      if (colors == null) {
        bing.url.getPaletteSwatches { s, c ->
          bindColors(holder, c)
          cacheColors[s] = c
        }
      }
    } else {
      bindColors(holder, colors)
    }
    holder.bind(bing)
  }

  private fun bindColors(holder: ViewHolder, colors: List<Palette.Swatch>) {
    holder.itemView.colors?.apply {
      makeVisible()
      itemAnimator = LandingAnimator()
      layoutManager = FlexboxLayoutManager(context, FlexDirection.ROW, FlexWrap.NOWRAP)
      adapter = ColorAdapter(colors, -1, context.dp2px(8f).toInt())
    }
  }

  class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(bing: Bing?) {
      if (bing == null) return
      itemView.desc.text = bing.copyright
      itemView.date.text = bing.date
      itemView.drawee_history_bing.aspectRatio = 1920f / 1080f//bing 返回的图片固定横向1920*1080
      itemView.drawee_history_bing.load(bing.url, progressDrawable = ProgressBarDrawable().apply {
        barWidth = itemView.context.dp2px(8f).toInt()
        setPadding(0)
        color = randomColor()
      })
    }
  }
}
