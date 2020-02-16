package com.yarolegovich.slidingrootnav.menu

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.yarolegovich.slidingrootnav.R
import io.nichijou.oops.ext.setPaddingTopPlusStatusBarHeight
import kotlinx.android.synthetic.main.item_header.view.*

class HeaderItem(private val icon: Drawable?, private val title: CharSequence, private val subtitle: String) : DrawerItem<HeaderItem.ViewHolder>(isSelectable = false) {

  private var normalTint: Int = 0

  override fun createViewHolder(parent: ViewGroup): ViewHolder {
    return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_header, parent, false))
  }

  override fun bindViewHolder(holder: ViewHolder) {
    holder.itemView.title.text = title
    holder.itemView.subtitle.text = subtitle
    holder.itemView.icon.setImageDrawable(icon)
    holder.itemView.title.setTextColor(normalTint)
    holder.itemView.subtitle.setTextColor(normalTint)
    holder.itemView.icon.setColorFilter(normalTint)
  }

  fun withNormalTint(normalTint: Int): HeaderItem {
    this.normalTint = normalTint
    return this
  }

  class ViewHolder(itemView: View) : DrawerAdapter.ViewHolder(itemView) {
    init {
      itemView.setPaddingTopPlusStatusBarHeight()
    }
  }
}
