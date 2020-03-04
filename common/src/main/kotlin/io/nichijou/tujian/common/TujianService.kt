package io.nichijou.tujian.common

import io.nichijou.tujian.common.entity.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url
import kotlin.random.Random

interface TujianService {
  @GET(C.API_TODAY)
  suspend fun today(@Query("sort") tid: String? = null): Response<List<Picture>>

  @GET(C.API_MEMBER)
  suspend fun member(@Query("id") id: String): Response<Picture>

  @GET(C.API_CATEGORY)
  suspend fun category(): Response<CategoryResp>

  @GET(C.API_LIST)
  suspend fun list(@Query("sort") tid: String, @Query("page") page: Int, @Query("size") size: Int): Response<ListResp>

  @GET(C.API_RANDOM)
  suspend fun random(@Query("op") tid: String = if (Random.nextBoolean()) "pc" else "mobile"): Response<List<Picture>>

  @GET
  suspend fun hitokoto(@Url url: String = C.API_HITOKOTO.split(",")[Random.nextInt(C.API_HITOKOTO.split(",").size)]): Response<Hitokoto>

  @GET
  suspend fun bing(@Url url: String = C.API_BING): Response<BingResp>
}
