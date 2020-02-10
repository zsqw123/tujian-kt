package io.nichijou.viewer.ext

import android.util.*
import java.util.*

inline fun <T> SparseArray<T>.forEach(block: (Int, T) -> Unit) {
  val size = this.size()
  for (index in 0 until size) {
    if (size != this.size()) throw ConcurrentModificationException()
    block(this.keyAt(index), this.valueAt(index))
  }
}
