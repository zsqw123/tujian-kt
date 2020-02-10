package com.yarolegovich.slidingrootnav.menu

import android.graphics.drawable.*
import android.view.*
import com.yarolegovich.slidingrootnav.*
import kotlinx.android.synthetic.main.item_option.view.*


class SimpleItem(override val id: Int, private val icon: Drawable?, private val title: String) : DrawerItem<SimpleItem.ViewHolder>(id) {

  private var selectedTint: Int = 0
  private var normalTint: Int = 0

  override fun createViewHolder(parent: ViewGroup): ViewHolder = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_option, parent, false))

  override fun bindViewHolder(holder: ViewHolder) {
    holder.itemView.title.text = title
    holder.itemView.icon.setImageDrawable(icon)

    holder.itemView.title.setTextColor(if (isChecked) selectedTint else normalTint)
    holder.itemView.icon.setColorFilter(if (isChecked) selectedTint else normalTint)
  }

  fun withSelectedTint(selectedTint: Int): SimpleItem {
    this.selectedTint = selectedTint
    return this
  }

  fun withNormalTint(normalTint: Int): SimpleItem {
    this.normalTint = normalTint
    return this
  }

  class ViewHolder(itemView: View) : DrawerAdapter.ViewHolder(itemView)
}
