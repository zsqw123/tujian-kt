package io.nichijou.tujian

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.nichijou.tujian.common.C
import io.nichijou.tujian.common.entity.BingUrlAdapter
import io.nichijou.tujian.common.entity.SplashResp
import io.nichijou.tujian.ui.MainActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.*
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url

class SplashActivity : Activity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (BuildConfig.DEBUG){
      startActivity<MainActivity>()
      finish()
    }
    GlobalScope.launch {
      delay(3000L)
      startActivity<MainActivity>()
      finish()
    }
    verticalLayout {
      imageView {
        id = R.id.splash
        backgroundColor = if (isDark()) Color.BLACK else Color.WHITE
        scaleType = ImageView.ScaleType.CENTER_CROP
      }.lparams(matchParent, matchParent)
    }
    val retrofit = Retrofit.Builder().baseUrl("https://v2.api.dailypics.cn/")
      .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder()
        .add(BingUrlAdapter())
        .add(KotlinJsonAdapterFactory())
        .build()))
      .build()
    val splahService = retrofit.create(SplahService::class.java)
    GlobalScope.launch() {
      val call = splahService.splash()
      if (call.isSuccessful) {
        val body = call.body()
        val url = body?.url
        if (url == null) {
          toast("获取启动图失败")
        } else {
          runOnUiThread {
            Glide.with(this@SplashActivity).load(url).placeholder(R.drawable.splash).into(findViewById(R.id.splash))
          }
        }
      }
    }

  }
}

interface SplahService {
  @GET
  suspend fun splash(@Url url: String = C.API_SPLASH): Response<SplashResp>
}
