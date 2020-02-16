package io.nichijou.tujian.diff

import androidx.recyclerview.widget.DiffUtil
import io.nichijou.tujian.common.entity.Bing

class BingDiffCallback : DiffUtil.ItemCallback<Bing>() {
  override fun areItemsTheSame(oldItem: Bing, newItem: Bing): Boolean = oldItem.url + "-" + oldItem.date == newItem.url + "-" + newItem.date

  override fun areContentsTheSame(oldItem: Bing, newItem: Bing): Boolean = oldItem == newItem
}
