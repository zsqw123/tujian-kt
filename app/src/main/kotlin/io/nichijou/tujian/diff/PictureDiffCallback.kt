package io.nichijou.tujian.diff

import androidx.recyclerview.widget.*
import io.nichijou.tujian.common.entity.*

class PictureDiffCallback : DiffUtil.ItemCallback<Picture>() {
  override fun areItemsTheSame(oldItem: Picture, newItem: Picture): Boolean = oldItem.pid == newItem.pid

  override fun areContentsTheSame(oldItem: Picture, newItem: Picture): Boolean = oldItem == newItem
}
