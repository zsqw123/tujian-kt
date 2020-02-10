package io.nichijou.tujian.common.ext

import android.content.SharedPreferences
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.chibatching.kotpref.KotprefModel
import com.chibatching.kotpref.pref.PreferenceKey
import io.nichijou.oops.live.CoroutineLiveData
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty0
import kotlin.reflect.jvm.isAccessible

@MainThread
fun <T> MutableLiveData<T>.notifyObserverOnMain() {
  this.value = this.value
}

fun <T> MutableLiveData<T>.notifyObserver() {
  postValue(this.value)
}

fun <T> KotprefModel.asLiveData(property: KProperty0<T>): LiveData<T> {
  return object : CoroutineLiveData<T>(), SharedPreferences.OnSharedPreferenceChangeListener {
    private val prefKey: String?
      get() {
        property.isAccessible = true
        return (property.getDelegate() as? PreferenceKey)?.key ?: property.name
      }

    init {
      launch {
        postValue(property.get())
      }
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, changed: String) {
      if (changed == prefKey) {
        ifChangeUpdate()
      }
    }

    private fun ifChangeUpdate() {
      launch {
        val newValue = property.get()
        if (value != newValue) postValue(newValue)
      }
    }

    override fun onActive() {
      this@asLiveData.preferences.registerOnSharedPreferenceChangeListener(this)
      ifChangeUpdate()
    }

    override fun onInactive() {
      this@asLiveData.preferences.unregisterOnSharedPreferenceChangeListener(this)
      super.onInactive()
    }
  }
}
