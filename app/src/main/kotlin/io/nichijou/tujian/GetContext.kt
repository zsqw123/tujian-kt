package io.nichijou.tujian

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

@SuppressLint("Registered")
class GetContext private constructor():Application() {
  companion object{
    private var context: Context? = null
    /**
    获取ApplicationContext
     */
    fun init(context: Context) {
      GetContext.context = context.applicationContext
    }
    fun getContext(): Context {
      val i = context
      if (i != null) return i
      throw NullPointerException("u should init first")
    }
  }
}
