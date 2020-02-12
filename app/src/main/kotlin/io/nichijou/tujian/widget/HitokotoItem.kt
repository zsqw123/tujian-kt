package io.nichijou.tujian.widget

import android.view.*
import com.yarolegovich.slidingrootnav.menu.*
import io.nichijou.tujian.*


class HitokotoItem(override val id: Int) : DrawerItem<HitokotoItem.ViewHolder>(id, isSelectable = false) {

  override fun createViewHolder(parent: ViewGroup): ViewHolder = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_menu_hitokoto, parent, false))

  override fun bindViewHolder(holder: ViewHolder) = Unit

  class ViewHolder(itemView: View) : DrawerAdapter.ViewHolder(itemView)
}
