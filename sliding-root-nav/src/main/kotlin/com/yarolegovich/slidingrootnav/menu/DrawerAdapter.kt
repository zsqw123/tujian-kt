package com.yarolegovich.slidingrootnav.menu

import android.util.*
import android.view.*
import androidx.recyclerview.widget.*
import java.util.*

class DrawerAdapter(val items: MutableList<DrawerItem<ViewHolder>>) : RecyclerView.Adapter<DrawerAdapter.ViewHolder>() {
  private val viewTypes: HashMap<Class<out DrawerItem<*>>, Int> = hashMapOf()
  private val holderFactories: SparseArray<DrawerItem<*>> = SparseArray()
  private var listener: ((drawerItem: DrawerItem<ViewHolder>) -> Unit)? = null

  init {
    processViewTypes()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = holderFactories.get(viewType).createViewHolder(parent)

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val drawerItem = items[position]
    drawerItem.bindViewHolder(holder)
    if (drawerItem.isSelectable) {
      holder.itemView.setOnClickListener {
        for (i in items.indices) {
          val item = items[i]
          if (item.isChecked) {
            item.setChecked(false)
            notifyItemChanged(i)
            break
          }
        }
        drawerItem.setChecked(true)
        notifyItemChanged(position)
        listener?.invoke(drawerItem)
      }
    } else {
      holder.itemView.setOnClickListener(null)
    }
  }

  override fun getItemCount(): Int {
    return items.size
  }

  override fun getItemViewType(position: Int): Int {
    return viewTypes[items[position]::class.java]!!
  }

  private fun processViewTypes() {
    var type = 0
    for (item in items) {
      if (!viewTypes.containsKey(item::class.java)) {
        viewTypes[item::class.java] = type
        holderFactories.put(type, item)
        type++
      }
    }
  }

  fun setListener(listener: (drawerItem: DrawerItem<ViewHolder>) -> Unit) {
    this.listener = listener
  }

  abstract class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
