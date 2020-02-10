package io.nichijou.tujian

import androidx.appcompat.app.*
import androidx.lifecycle.*
import io.nichijou.tujian.common.ext.*

class StyleViewModel : ViewModel() {

  val cardRadius by lazy {
    Settings.asLiveData(Settings::cardRadius)
  }
  val cardElevation by lazy {
    Settings.asLiveData(Settings::cardElevation)
  }

  val cardSpace by lazy {
    Settings.asLiveData(Settings::cardSpace)
  }

  val topBarElevation by lazy {
    Settings.asLiveData(Settings::topBarElevation)
  }

  val topBarRadius by lazy {
    viewModelScope
    Settings.asLiveData(Settings::topBarRadius)
  }

  companion object {
    fun live(activity: AppCompatActivity): StyleViewModel = ViewModelProviders.of(activity).get(StyleViewModel::class.java)
  }
}
