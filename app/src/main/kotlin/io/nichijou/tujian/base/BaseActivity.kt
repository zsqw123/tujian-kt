package io.nichijou.tujian.base

import android.os.*
import android.view.*
import io.nichijou.oops.*
import kotlinx.coroutines.*

abstract class BaseActivity : OopsActivity(), CoroutineScope by MainScope() {

  protected abstract fun getContentViewId(): Int

  protected open fun isFullScreen(): Boolean = false

  protected abstract fun handleOnCreate(savedInstanceState: Bundle?)

  override fun onCreate(savedInstanceState: Bundle?) {
    if (isFullScreen()) {
      requestWindowFeature(Window.FEATURE_NO_TITLE)
      window.setFlags(
        WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN
      )
    }
    super.onCreate(savedInstanceState)
    setContentView(getContentViewId())
    handleOnCreate(savedInstanceState)
  }

  override fun onDestroy() {
    cancel()
    super.onDestroy()
  }
}
