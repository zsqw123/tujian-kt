package io.nichijou.tujian.common.ext

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

fun <T> RecyclerView.addNew(newData: List<T>?, reset: Boolean = false) {
  if (newData == null) return
  (this.adapter as? RecyclerAdapter<T>?)?.add(newData, reset)
    ?: throw IllegalStateException("you need use RecyclerView.with() init RecyclerView")
}

fun <T> RecyclerView.resetData(newData: List<T>?) {
  if (newData == null) return
  (this.adapter as? RecyclerAdapter<T>?)?.reset(newData)
    ?: throw IllegalStateException("you need use RecyclerView.with() init RecyclerView")
}

fun RecyclerView.clearList() {
  (this.adapter as? RecyclerAdapter<*>?)?.clear()
    ?: throw IllegalStateException("you need use RecyclerView.with() init RecyclerView")
}

fun <T> RecyclerView.getList(): MutableList<T> {
  return (this.adapter as? RecyclerAdapter<T>?)?.data()
    ?: throw IllegalStateException("you need use RecyclerView.with() init RecyclerView")
}

fun <T> RecyclerView.notifyItemsChanged() {
  (this.adapter as? RecyclerAdapter<*>?)?.notifyItemsChanged()
    ?: throw IllegalStateException("you need use RecyclerView.with() init RecyclerView")
}

fun <T> RecyclerView.with(
  @LayoutRes layoutRes: Int,
  data: MutableList<T> = ArrayList(),
  viewTypeMatching: (bean: T, layoutResId: Int) -> Boolean = { _, _ -> true },
  bind: (View.(bean: T, pos: Int) -> Unit)?
): RecyclerAdapter<T> = RecyclerAdapter(this, data).with(layoutRes, viewTypeMatching, bind)

class RecyclerAdapter<T>(private val recycler: RecyclerView, private val data: MutableList<T>) :
  RecyclerView.Adapter<ViewHolder<T>>() {
  private var items = ArrayList<ItemCache<T>>()
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<T> {
    return ViewHolder(
      LayoutInflater.from(parent.context).inflate(items.first { it.layoutRes == viewType }.layoutRes, parent, false), viewType)
  }

  override fun onBindViewHolder(holder: ViewHolder<T>, position: Int) {
    holder.bindView(data[position], items.first { it.layoutRes == holder.viewType }.bind)
  }

  override fun getItemCount() = data.size

  override fun getItemViewType(position: Int) = items.first { it.matching(data[position], it.layoutRes) }.layoutRes

  fun with(@LayoutRes layoutRes: Int, viewTypeMatching: (bean: T, layoutResId: Int) -> Boolean, bind: (View.(bean: T, pos: Int) -> Unit)? = null): RecyclerAdapter<T> {
    items.add(ItemCache(layoutRes, viewTypeMatching, bind))
    recycler.adapter = this
    return this
  }

  fun data() = data
  fun add(newData: List<T>, reset: Boolean = false) {
    if (reset) {
      reset(newData)
    } else {
      val size = data.size
      data.addAll(newData)
      notifyItemRangeInserted(size, newData.size)
    }
  }

  fun reset(newData: List<T>) {
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

class ItemCache<T>(
  @LayoutRes val layoutRes: Int,
  val matching: (T, Int) -> Boolean,
  val bind: (View.(T, Int) -> Unit)?
)

class ViewHolder<T>(itemView: View, val viewType: Int) : RecyclerView.ViewHolder(itemView) {
  fun bindView(entity: T, bind: (View.(T, Int) -> Unit)?) {
    adapterPosition
    itemView.apply {
      bind?.invoke(this, entity, adapterPosition)
    }
  }
}
