package io.nichijou.tujian.ui.archive

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
import io.nichijou.tujian.BuildConfig
import io.nichijou.tujian.R
import io.nichijou.tujian.common.entity.Picture
import io.nichijou.tujian.common.ext.dp2px
import io.nichijou.tujian.common.ext.makeGone
import io.nichijou.tujian.common.ext.makeVisible
import io.nichijou.tujian.common.fresco.getPaletteSwatches
import io.nichijou.tujian.common.fresco.load
import io.nichijou.tujian.diff.PictureDiffCallback
import io.nichijou.tujian.ui.ColorAdapter
import io.nichijou.utils.randomColor
import jp.wasabeef.recyclerview.animators.LandingAnimator
import kotlinx.android.synthetic.main.item_picture.view.*
import java.util.*

class ListAdapter(
  private val draweeClicked: (
    view: View,
    adapter: PagedListAdapter<Picture, ViewHolder>,
    pos: Int
  ) -> Unit
) : PagedListAdapter<Picture, ListAdapter.ViewHolder>(PictureDiffCallback()) {

  private val cacheColors = WeakHashMap<String, List<Palette.Swatch>>()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_picture, parent, false))

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val picture = getItem(position)
    val colors = cacheColors[getNewUrl(picture)]
    if (colors.isNullOrEmpty()) {
      holder.itemView.colors?.makeGone()
      if (colors == null) {
        picture?.local?.getPaletteSwatches { s, c ->
          bindColors(holder, c)
          cacheColors[s] = c
        }
      }
    } else {
      bindColors(holder, colors)
    }
    holder.itemView.drawee_history_bing.setOnClickListener {
      draweeClicked.invoke(it, this, position)
    }
    holder.bind(picture)
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
    fun bind(picture: Picture?) {
      if (picture == null) return
      itemView.title.text = picture.title
      itemView.date.text = picture.date
      val newUrl = getNewUrl(picture)+"!w360"
      itemView.drawee_history_bing.aspectRatio = picture.width.toFloat() / picture.height.toFloat()
      itemView.drawee_history_bing.load(newUrl, progressDrawable = ProgressBarDrawable().apply {
        barWidth = itemView.context.dp2px(8f).toInt()
        setPadding(0)
        color = randomColor()
      })
    }
  }
}

fun getNewUrl(picture: Picture?): String? = if (picture?.nativePath == picture?.local) picture?.local else BuildConfig.API_SS + picture?.nativePath
