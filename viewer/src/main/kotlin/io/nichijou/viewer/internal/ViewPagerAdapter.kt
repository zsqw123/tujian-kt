package io.nichijou.viewer.internal

import android.view.*
import io.nichijou.viewer.*

internal class ViewPagerAdapter<T>(
  private var images: List<T>,
  private val imageLoader: (view: ViewerItemView, data: T) -> Unit
) : RecyclingPagerAdapter<ViewPagerAdapter<T>.ViewHolder>() {

  private val holders = mutableListOf<ViewHolder>()

  fun isScaled(position: Int): Boolean =
    holders.firstOrNull { it.position == position }?.isScaled() ?: false

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return ViewHolder(ViewerItemView(parent.context)).also { holders.add(it) }
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(position)

  override fun getItemCount() = images.size

  fun updateImages(images: List<T>) {
    this.images = images
    notifyDataSetChanged()
  }

  fun resetScale(position: Int) = holders.firstOrNull { it.position == position }?.resetScale()

  inner class ViewHolder(itemView: View) : RecyclingPagerAdapter.ViewHolder(itemView) {

    private val viewerView = itemView as ViewerItemView

    fun isScaled(): Boolean {
      val view = viewerView.getActualImageView() ?: return false
      return view.scale > view.minScale
    }

    fun bind(position: Int) {
      this.position = position
      imageLoader.invoke(viewerView, images[position])
    }

    fun resetScale() {
      viewerView.getActualImageView()?.apply {
        animateScaleAndCenter(this.minScale, this.center)?.start()
      }
    }
  }
}
