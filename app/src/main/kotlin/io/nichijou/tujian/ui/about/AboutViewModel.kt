package io.nichijou.tujian.ui.about

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.nichijou.tujian.common.db.TujianStore
import io.nichijou.tujian.common.ext.readAssetsFileText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AboutViewModel(application: Application, private val tujianStore: TujianStore) : AndroidViewModel(application) {

  val lastPicture by lazy(LazyThreadSafetyMode.NONE) {
    tujianStore.lastPicture()
  }

  private val team = MutableLiveData<List<Team>>()
//  private val osl = MutableLiveData<List<OSL>>()

  private val moshi by lazy(LazyThreadSafetyMode.NONE) {
    Moshi.Builder()
      .add(KotlinJsonAdapterFactory())
      .build()
  }


  fun getTeam(): MutableLiveData<List<Team>> {
    viewModelScope.launch(Dispatchers.IO) {
      val json = getApplication<Application>().readAssetsFileText("team.json")
      val type = Types.newParameterizedType(List::class.java, Team::class.java)
      val list = moshi.adapter<List<Team>>(type).fromJson(json) ?: emptyList()
      team.postValue(list)
    }
    return team
  }

//  fun getOSL(): MutableLiveData<List<OSL>> {
//    viewModelScope.launch(Dispatchers.IO) {
//      val json = getApplication<Application>().readAssetsFileText("osl.json")
//      val type = Types.newParameterizedType(List::class.java, OSL::class.java)
//      val list = moshi.adapter<List<OSL>>(type).fromJson(json) ?: emptyList()
//      osl.postValue(list)
//    }
//    return osl
//  }
}
