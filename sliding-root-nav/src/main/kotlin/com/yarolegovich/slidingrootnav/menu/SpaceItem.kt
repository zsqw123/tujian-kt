package com.yarolegovich.slidingrootnav.menu

import android.util.*
import android.view.*


class SpaceItem(private val spaceDp: Float) : DrawerItem<SpaceItem.ViewHolder>(isSelectable = false) {

  override var isSelectable = false

  override fun createViewHolder(parent: ViewGroup): ViewHolder {
    val c = parent.context
    val view = View(c)
    view.layoutParams = ViewGroup.LayoutParams(
      ViewGroup.LayoutParams.MATCH_PARENT,
      (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, spaceDp, view.context.resources.displayMetrics) + 0.5f).toInt()
    )
    return ViewHolder(view)
  }

  override fun bindViewHolder(holder: ViewHolder) = Unit

  class ViewHolder(itemView: View) : DrawerAdapter.ViewHolder(itemView)
}
