package io.nichijou.tujian.ui.today

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.nichijou.tujian.common.entity.Picture
import io.nichijou.tujian.ui.archive.getNewUrl
import io.nichijou.tujian.ui.archive.toolDialog
import kotlinx.android.synthetic.main.view_today_item.view.*

class TodayAdapter(private val items: List<Picture>) : RecyclerView.Adapter<TodayAdapter.ViewHolder>() {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return ViewHolder(TodayItemView(parent.context).apply {
      layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT)
    })
  }

  override fun getItemCount(): Int = items.size
  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val path = getNewUrl(items[position], 1080)
    holder.itemView.actual_view.setOnLongClickListener {
      toolDialog(holder.itemView.context, items[position])
      return@setOnLongClickListener true
    }
    return (holder.itemView as TodayItemView).updateUrl(path!!)
  }

  class ViewHolder(itemView: TodayItemView) : RecyclerView.ViewHolder(itemView)
}
