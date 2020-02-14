package io.nichijou.tujian.ui.archive

import android.graphics.Point
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zzhoujay.richtext.RichText
import io.nichijou.tujian.R
import io.nichijou.tujian.common.entity.Picture
import io.nichijou.tujian.common.ext.ViewHolder
import kotlinx.android.synthetic.main.fragment_today.*
import kotlinx.android.synthetic.main.photo_item_layout.view.*
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.windowManager

class Viewpager2Adapter(private val data: ArrayList<Picture>) :
  RecyclerView.Adapter<RecyclerView.ViewHolder>() {
  private var items: ArrayList<Picture> = data
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    val view: View = LayoutInflater.from(parent.context).inflate(R.layout.photo_item_layout, parent, false)
    return ViewHolder<RecyclerView>(view, viewType)
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = holder.itemView
    val point = Point()
    item.context.windowManager.defaultDisplay.getRealSize(point)
    val screenX = point.x
    val screenY = point.y

    val photoView = item.photo_item
    photoView.enable()
    photoView.layoutParams = RelativeLayout.LayoutParams(matchParent, screenY * 9 / 16)
    photoView.scaleType = ImageView.ScaleType.FIT_CENTER
    item.photo_item_desc.typeface = Typeface.DEFAULT_BOLD
    RichText.fromMarkdown(items[position].desc.replace("\n", "  \n")).into(item.photo_item_desc)
    Glide.with(item.context).load(items[position].local).into(photoView)
  }

  override fun getItemCount() = data.size

  fun add(newData: ArrayList<Picture>, reset: Boolean = false) {
    if (reset) {
      reset(newData)
    } else {
      val size = data.size
      data.addAll(newData)
      notifyItemRangeInserted(size, newData.size)
    }
  }

  fun reset(newData: ArrayList<Picture>) {
    data.clear()
    data.addAll(newData)
    notifyDataSetChanged()
  }

  fun notifyItemsChanged() {
    notifyItemRangeChanged(0, data.size)
  }

  fun clear() {
    data.clear()
    notifyDataSetChanged()
  }
}
