package io.nichijou.tujian.common

import io.nichijou.tujian.common.entity.*
import okhttp3.FormBody
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import kotlin.random.Random

interface TujianService {
  @POST
  @Multipart
  suspend fun upload(@Url url: String = BuildConfig.API_UPLOAD, @Part body: MultipartBody.Part): Response<UploadResult>

  @POST
  suspend fun post(@Url url: String = BuildConfig.API_TG, @Body formBody: FormBody): Response<PostResp>

  @GET(BuildConfig.API_TODAY)
  suspend fun today(@Query("sort") tid: String? = null): Response<List<Picture>>

  @GET(BuildConfig.API_MEMBER)
  suspend fun member(@Query("id") id: String): Response<Picture>

  @GET(BuildConfig.API_CATEGORY)
  suspend fun category(): Response<CategoryResp>

  @GET(BuildConfig.API_LIST)
  suspend fun list(@Query("sort") tid: String, @Query("page") page: Int, @Query("size") size: Int): Response<ListResp>

  @GET(BuildConfig.API_RANDOM)
  suspend fun random(@Query("op") tid: String = if (Random.nextBoolean()) "pc" else "mobile"): Response<Picture>

  @GET
  suspend fun hitokoto(@Url url: String = BuildConfig.API_HITOKOTO.split(",")[Random.nextInt(BuildConfig.API_HITOKOTO.split(",").size)]): Response<Hitokoto>

  @GET
  suspend fun bing(@Url url: String = BuildConfig.API_BING): Response<BingResp>
}
