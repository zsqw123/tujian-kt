package com.yarolegovich.slidingrootnav.menu

import android.view.*
import com.yarolegovich.slidingrootnav.*
import kotlinx.android.synthetic.main.item_option.view.*


class SectionItem(private val title: String) : DrawerItem<SectionItem.ViewHolder>(isSelectable = false) {

  private var normalTint: Int = 0

  override fun createViewHolder(parent: ViewGroup): ViewHolder = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_section, parent, false))

  override fun bindViewHolder(holder: ViewHolder) {
    holder.itemView.title.text = title
    holder.itemView.title.setTextColor(normalTint)
  }

  fun withNormalTint(normalTint: Int): SectionItem {
    this.normalTint = normalTint
    return this
  }

  class ViewHolder(itemView: View) : DrawerAdapter.ViewHolder(itemView)
}
