package io.nichijou.tujian.common

import android.os.Environment
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.nichijou.tujian.common.db.TujianStore
import io.nichijou.tujian.common.entity.BingUrlAdapter
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

val commonModule = module {
  factory {
    val builder = OkHttpClient.Builder()
    if (BuildConfig.DEBUG) {
      val loggingInterceptor = HttpLoggingInterceptor()
      loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
      builder.addInterceptor(loggingInterceptor)
    }
    builder.readTimeout(30, TimeUnit.SECONDS)
      .writeTimeout(30, TimeUnit.SECONDS)
      .connectTimeout(30, TimeUnit.SECONDS)
      .retryOnConnectionFailure(true)
      .hostnameVerifier(HostnameVerifier { _, _ -> true })
    val trustManager = object : X509TrustManager {
      @Throws(CertificateException::class)
      override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = Unit

      @Throws(CertificateException::class)
      override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = Unit

      override fun getAcceptedIssuers(): Array<X509Certificate> {
        return arrayOf()
      }
    }
    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(null, arrayOf<TrustManager>(trustManager), SecureRandom())
    builder.sslSocketFactory(sslContext.socketFactory, trustManager)
    val cachePath = if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
      || !Environment.isExternalStorageRemovable()) {
      androidContext().externalCacheDir?.absolutePath
    } else {
      androidContext().cacheDir.absolutePath
    }
    if (cachePath != null && File(cachePath).exists()) {
      builder.cache(Cache(File(cachePath), Long.MAX_VALUE))
    }
    builder.build()
  }
  single {
    val retrofit = Retrofit.Builder()
      .baseUrl("https://v2.api.dailypics.cn/")
      .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder()
        .add(BingUrlAdapter())
        .add(KotlinJsonAdapterFactory())
        .build()))
      .client(get())
      .build()
    retrofit.create(TujianService::class.java)
  }
  single { TujianStore(androidContext()) }
}
