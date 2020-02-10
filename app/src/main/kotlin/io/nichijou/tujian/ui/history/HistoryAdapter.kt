package io.nichijou.tujian.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import io.nichijou.tujian.R
import io.nichijou.tujian.common.entity.BaseEntity
import io.nichijou.tujian.common.entity.Bing
import io.nichijou.tujian.common.entity.Hitokoto
import io.nichijou.tujian.common.entity.Picture
import io.nichijou.tujian.common.ext.makeGone
import io.nichijou.tujian.common.ext.makeVisible
import io.nichijou.tujian.common.ext.toClipboard
import io.nichijou.tujian.common.ext.toast
import io.nichijou.tujian.widget.Card
import java.util.*

class HistoryAdapter(private val items: List<BaseEntity>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      BING -> BingViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_history_bing, parent, false))
      HITOKOTO -> HitokotoViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_history_hitokoto, parent, false))
      PICTURE -> PictureViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_history_picture, parent, false))
      else -> throw IllegalStateException()
    }
  }

  override fun getItemCount(): Int = items.size

  companion object {
    private const val BING = 1
    private const val PICTURE = 2
    private const val HITOKOTO = 3
  }

  override fun getItemViewType(position: Int): Int {
    return when (items[position]) {
      is Hitokoto -> HITOKOTO
      is Picture -> PICTURE
      is Bing -> BING
      else -> -1
    }
  }

  private val cacheColors = WeakHashMap<String, List<Palette.Swatch>>()

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (holder) {
      is HitokotoViewHolder -> holder.bind(items[position] as Hitokoto)
      is BingViewHolder -> {
        val view = holder.itemView as Card
        val item = items[position] as Bing
        view.setDataBing(item, cacheColors, view)
      }
      is PictureViewHolder -> {
        val view = holder.itemView as Card
        val item = items[position] as Picture
        view.setDataPicture(item, cacheColors, view)
      }
    }
  }

  class HitokotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val hitokoto = itemView.findViewById<TextView>(R.id.hitokoto)
    private val source = itemView.findViewById<TextView>(R.id.source)
    fun bind(hi: Hitokoto?) {
      if (hi == null) return
      hitokoto.text = hi.hitokoto
      source.text = hi.source
      if (hi.source.trim().isBlank()) {
        source.makeGone()
      } else {
        source.makeVisible()
      }
      itemView.setOnLongClickListener {
        it.context.toClipboard(hi.hitokoto + "\n" + hi.source)
        it.context.toast(R.string.already_copied)
        true
      }
    }
  }

  class PictureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

  class BingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
