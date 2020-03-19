package io.nichijou.tujian.ui.archive

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.bm.library.PhotoView
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import io.nichijou.tujian.R
import io.nichijou.tujian.common.C
import io.nichijou.tujian.common.entity.Picture
import io.nichijou.tujian.common.ext.dp2px
import io.nichijou.tujian.common.ext.makeGone
import io.nichijou.tujian.common.ext.makeVisible
import io.nichijou.tujian.common.fresco.getPaletteSwatches
import io.nichijou.tujian.diff.PictureDiffCallback
import io.nichijou.tujian.isDark
import io.nichijou.tujian.ui.ColorAdapter
import jp.wasabeef.recyclerview.animators.LandingAnimator
import kotlinx.android.synthetic.main.item_picture.view.*
import java.util.*

class ListAdapter(
  private val draweeClicked: (
    view: PhotoView,
    adapter: PagedListAdapter<Picture, ViewHolder>,
    pos: Int
  ) -> Unit
) : PagedListAdapter<Picture, ListAdapter.ViewHolder>(PictureDiffCallback()) {

  private val cacheColors = WeakHashMap<String, List<Palette.Swatch>>()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
    ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_picture, parent, false))

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val picture = getItem(position)
    val colors = cacheColors[getNewUrl(picture, 360)]
    if (colors.isNullOrEmpty()) {
      holder.itemView.colors?.makeGone()
      if (colors == null) {
        getNewUrl(picture, 360)?.getPaletteSwatches { s, c ->
          bindColors(holder, c)
          cacheColors[s] = c
        }
      }
    } else {
      bindColors(holder, colors)
    }
    holder.itemView.list_item_thumbnail.setOnClickListener {
      draweeClicked.invoke(it.list_item_thumbnail, this, position)
    }
    holder.bind(picture, position)
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
    fun bind(picture: Picture?, position: Int) {
      if (picture == null) return
      itemView.title.text = picture.title
      itemView.date.text = picture.date
      val newUrl = getNewUrl(picture, 360)
      if (position == 0) {
        itemView.list_item_thumbnail.ratio = 1.0F
      } else {
        itemView.list_item_thumbnail.ratio = 1.618F
      }
      if (isDark()) Glide.with(itemView).load(newUrl).placeholder(R.mipmap.placeholder_n).into(itemView.list_item_thumbnail)
      else Glide.with(itemView).load(newUrl).placeholder(R.mipmap.placeholder).into(itemView.list_item_thumbnail)
    }
  }
}

// tujian v2 API
fun getNewUrl(picture: Picture?, level: Int = 0): String? {
  // 360 480 720 1080
  val levelText = if (level != 0) "!w$level" else ""
  return if (picture?.nativePath == picture?.local) picture?.local else C.API_SS + picture?.nativePath + levelText
}
