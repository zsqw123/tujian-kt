package io.nichijou.tujian

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.gms.ads.MobileAds
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tencent.bugly.Bugly
import com.tencent.bugly.beta.Beta
import io.nichijou.oops.Oops
import io.nichijou.tujian.common.C
import io.nichijou.tujian.common.entity.SplashResp
import io.nichijou.tujian.ui.MainActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.yokeyword.fragmentation.Fragmentation
import org.jetbrains.anko.*
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url
import java.text.SimpleDateFormat
import java.util.*

class SplashActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val isDark: Boolean = isDark()
    val imgID: Int = if (isDark) R.mipmap.splash_night else R.mipmap.splash
    frameLayout {
      imageView {
        imageResource = imgID
        id = R.id.splash
        scaleType = ImageView.ScaleType.CENTER_CROP
      }.lparams(matchParent, matchParent)
    }
    Oops.bulk { navBarColor = 0 }

    // Activity init
    GlobalScope.launch {
      try {
        // Bugly
        @Suppress("ConstantConditionIf")
        if (BuildConfig.FLAVOR != "googlePlay" && !BuildConfig.DEBUG) {
          if (BuildConfig.API_BUGLY != "null") {
            Beta.upgradeDialogLayoutId = R.layout.update_dialog
            Bugly.init(applicationContext, BuildConfig.API_BUGLY, false)
          }
        }
        Fragmentation.builder().debug(BuildConfig.DEBUG).install()
      } catch (e: Exception) {
        Log.e("restartApp", "Exception")
      }
      MobileAds.initialize(applicationContext){}
      delay(2500L)
      startActivity<MainActivity>()
      finish()
    }
    // load Splash
    val retrofit = Retrofit.Builder().baseUrl("https://v2.api.dailypics.cn/")
      .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder()
        .add(KotlinJsonAdapterFactory()).build())).build()
    val splahService = retrofit.create(SplahService::class.java)
    GlobalScope.launch {
      val call = splahService.splash()
      if (call.isSuccessful) {
        val body = call.body()
        val url = body?.url
        if (url == null) {
          toast("获取启动图失败")
        } else {
          val start = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).parse(body.start)
          val end = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).parse(body.end)
          if (Date() in start..end) runOnUiThread {
            try {
              val view = findViewById<ImageView>(R.id.splash)
              Glide.with(this@SplashActivity).load(url).placeholder(imgID)
                .transition(DrawableTransitionOptions.withCrossFade(300)).into(view)
            } catch (e: Exception) {
            }
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
