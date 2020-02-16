package io.nichijou.tujian.func

import android.os.Handler
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

class ServiceLifecycleDispatcher(provider: LifecycleOwner) {

  private val mRegistry: LifecycleRegistry = LifecycleRegistry(provider)
  private val mHandler: Handler = Handler()
  private var mLastDispatchRunnable: DispatchRunnable? = null

  val lifecycle: Lifecycle
    get() = mRegistry

  private fun postDispatchRunnable(event: Lifecycle.Event) {
    mLastDispatchRunnable?.run()
    mLastDispatchRunnable = DispatchRunnable(mRegistry, event)
    mHandler.postAtFrontOfQueue(mLastDispatchRunnable)
  }

  fun onServiceLifecycleEventCreate() {
    postDispatchRunnable(Lifecycle.Event.ON_CREATE)
  }


  fun onServiceLifecycleEventStart() {
    postDispatchRunnable(Lifecycle.Event.ON_START)
  }

  fun onServiceLifecycleEventStop() {
    postDispatchRunnable(Lifecycle.Event.ON_STOP)
  }

  fun onServiceLifecycleEventDestroy() {
    postDispatchRunnable(Lifecycle.Event.ON_STOP)
    postDispatchRunnable(Lifecycle.Event.ON_DESTROY)
  }

  internal class DispatchRunnable(private val mRegistry: LifecycleRegistry, private val mEvent: Lifecycle.Event) : Runnable {
    private var mWasExecuted = false

    override fun run() {
      if (!mWasExecuted) {
        mRegistry.handleLifecycleEvent(mEvent)
        mWasExecuted = true
      }
    }
  }
}
