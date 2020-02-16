package io.nichijou.tujian

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.nichijou.tujian.common.ext.asLiveData

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
    fun live(activity: AppCompatActivity): StyleViewModel =
      ViewModelProvider(activity).get(StyleViewModel::class.java)
  }
}
