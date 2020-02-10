package io.nichijou.tujian.diff

import androidx.recyclerview.widget.*
import io.nichijou.tujian.common.entity.*

class HitokotoDiffCallback : DiffUtil.ItemCallback<Hitokoto>() {
  override fun areItemsTheSame(oldItem: Hitokoto, newItem: Hitokoto): Boolean = oldItem.source + "-" + oldItem.hitokoto == newItem.source + "-" + newItem.hitokoto

  override fun areContentsTheSame(oldItem: Hitokoto, newItem: Hitokoto): Boolean = oldItem == newItem
}
