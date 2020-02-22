package io.nichijou.tujian

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.SimpleResource
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.chibatching.kotpref.Kotpref
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tencent.bugly.crashreport.CrashReport
import io.nichijou.oops.Oops
import io.nichijou.tujian.common.C
import io.nichijou.tujian.common.commonModule
import io.nichijou.tujian.common.entity.BingUrlAdapter
import io.nichijou.tujian.common.entity.SplashResp
import io.nichijou.tujian.common.shortcuts.ShortcutsController
import io.nichijou.tujian.ui.MainActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.yokeyword.fragmentation.Fragmentation
import okhttp3.OkHttpClient
import org.jetbrains.anko.*
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url

class SplashActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val appContext = App.context!!
    Oops.init(this)
    Kotpref.init(appContext)
    val isDark: Boolean = isDark()
    frameLayout {
      textView("图鉴\n日图"){
        textSize = sp(40).toFloat()
        gravity=Gravity.CENTER
      }
      imageView {
        id = R.id.splash
        visibility = View.INVISIBLE
        scaleType = ImageView.ScaleType.CENTER_CROP
      }.lparams(matchParent, matchParent)
    }

    // Activity init
    GlobalScope.launch {
      // Bugly
      @Suppress("ConstantConditionIf")
      if (!BuildConfig.DEBUG) {
        if (BuildConfig.API_BUGLY != "null") {
          CrashReport.initCrashReport(appContext, BuildConfig.API_BUGLY, false)
        }
      }
      startKoin {
        if (BuildConfig.DEBUG) {
          printLogger()
        }
        androidContext(appContext)
        modules(normalModule, commonModule)
      }
      val okHttpClient: OkHttpClient by inject()
      App.initFresco(okHttpClient)
      ShortcutsController.updateShortcuts(App.context!!)
      Fragmentation.builder().debug(BuildConfig.DEBUG).install()
      delay(2500L)
      startActivity<MainActivity>()
      finish()
    }
    // load Splash
    val retrofit = Retrofit.Builder().baseUrl("https://v2.api.dailypics.cn/")
      .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder()
        .add(BingUrlAdapter()).add(KotlinJsonAdapterFactory()).build())).build()
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
            val view = findViewById<ImageView>(R.id.splash)
            Glide.with(this@SplashActivity).load(url).listener(object : RequestListener<Drawable> {
              override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean = false
              override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                view.visibility = View.VISIBLE
                return false
              }
            }).into(view)
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
