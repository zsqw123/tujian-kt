package io.nichijou.tujian.ui.today

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.nichijou.tujian.App
import io.nichijou.tujian.R
import io.nichijou.tujian.common.TujianService
import io.nichijou.tujian.common.db.TujianStore
import io.nichijou.tujian.common.entity.Picture
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class TodayViewModel(application: Application, private val tujianService: TujianService, private val tujianStore: TujianStore) : AndroidViewModel(application) {

  private lateinit var today: MutableLiveData<List<Picture>>

  val msg by lazy(LazyThreadSafetyMode.NONE) { MutableLiveData<String>() }

  fun getToday(): MutableLiveData<List<Picture>> {
    if (!::today.isInitialized) {
      today = MutableLiveData()
    }
    viewModelScope.launch(IO) {
      val tujianResp = tujianService.today()
      val bingResp = tujianService.bing()
      val pictures = mutableListOf<Picture>()
      if (tujianResp.isSuccessful) {
        val list = tujianResp.body()
        if (list.isNullOrEmpty()) {
          msg.postValue(getApplication<App>().getString(R.string.today_tujian_is_empty))
        } else {
          tujianStore.insertPicture(list)
          pictures.addAll(list)
        }
      } else {
        msg.postValue(tujianResp.message())
      }
      if (bingResp.isSuccessful) {
        val list = bingResp.body()?.data
        if (list.isNullOrEmpty()) {
          msg.postValue(getApplication<App>().getString(R.string.today_bing_is_empty))
        } else {
          tujianStore.insertBing(list)
          val bing = list[0]
          var date = bing.date.substring(4)
          date = date.substring(0, 2) + "-" + date.substring(2)
          val matcher = Pattern.compile("(.*) \\(.*© (.*)\\)").matcher(bing.copyright)
          var desc: String? = null
          var user: String? = null
          if (matcher.find()) {
            desc = matcher.group(1)
            user = matcher.group(2)
          }
          pictures.add(Picture(
            "",
            bing.url,
            desc ?: bing.copyright,
            date,
            bing.url,
            getApplication<App>().getString(R.string.today_bing_wallpaper),
            user ?: getApplication<App>().getString(R.string.bing), 1920, 1080,
            nativePath = bing.url,//暂时先写在这儿
            category = getApplication<App>().getString(R.string.bing),
            from = Picture.FROM_BING))
        }
      } else {
        msg.postValue(tujianResp.message())
      }
      today.postValue(pictures)
    }
    return today
  }
}
