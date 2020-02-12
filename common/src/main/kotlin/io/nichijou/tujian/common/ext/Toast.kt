package io.nichijou.tujian.common.ext

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun Fragment.toast(msg: String?, duration: Int = Toast.LENGTH_SHORT) {
  context?.toast(msg, duration)
}

fun Fragment.toast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
  context?.toast(resId, duration)
}

fun Activity.toast(msg: String?, duration: Int = Toast.LENGTH_SHORT) {
  toast(msg, duration)
}

fun Activity.toast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
  toast(resId, duration)
}

fun Context.toast(
  @StringRes resId: Int,
  duration: Int = Toast.LENGTH_SHORT
) {
  GlobalScope.launch(Dispatchers.Main) {
    Toast.makeText(this@toast.applicationContext, resId, duration).show()
  }
}

fun Context.toast(
  msg: String?,
  duration: Int = Toast.LENGTH_SHORT
) {
  GlobalScope.launch(Dispatchers.Main) {
    Toast.makeText(this@toast.applicationContext, msg, duration).show()
  }
}
