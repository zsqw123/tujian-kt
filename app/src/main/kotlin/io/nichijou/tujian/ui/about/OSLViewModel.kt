package io.nichijou.tujian.ui.about

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.nichijou.tujian.common.ext.readAssetsFileText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OSLViewModel(application: Application) : AndroidViewModel(application) {
  private val moshi by lazy(LazyThreadSafetyMode.NONE) {
    Moshi.Builder()
      .add(KotlinJsonAdapterFactory())
      .build()
  }
  private val osl = MutableLiveData<List<OSL>>()
  fun getOSL(): MutableLiveData<List<OSL>> {
    viewModelScope.launch(Dispatchers.IO) {
      val json = getApplication<Application>().readAssetsFileText("osl.json")
      val type = Types.newParameterizedType(List::class.java, OSL::class.java)
      val list = moshi.adapter<List<OSL>>(type).fromJson(json) ?: emptyList()
      osl.postValue(list)
    }
    return osl
  }
}
