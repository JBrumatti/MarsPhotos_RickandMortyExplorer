package com.example.marsphotos.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

@Serializable
data class MarsPhoto(val id: String, @SerialName("img_src") val imgSrc: String)
@Serializable
data class PageInfo(val count: Int)
@Serializable
data class ApiPage<T>(val info: PageInfo, val results: List<T>)
@Serializable
data class Character(val id: Int, val name: String, val image: String, val status: String, val species: String)
@Serializable
data class Episode(val id: Int, val name: String, val episode: String, @SerialName("air_date") val airDate: String, val characters: List<String>)
@Serializable
data class Location(val id: Int, val name: String, val type: String, val dimension: String, val residents: List<String>)

private fun retrofit(baseUrl: String): Retrofit = Retrofit.Builder()
    .baseUrl(baseUrl)
    .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType()))
    .build()

interface MarsApiService {
    @GET("photos") suspend fun getPhotos(): List<MarsPhoto>
}
interface RickApiService {
    @GET("character") suspend fun characters(@Query("name") name: String): ApiPage<Character>
    @GET("episode") suspend fun episodes(@Query("name") name: String): ApiPage<Episode>
    @GET("location") suspend fun locations(@Query("name") name: String): ApiPage<Location>
    @GET suspend fun character(@Url url: String): Character
}
object MarsApi {
    val service: MarsApiService by lazy {
        retrofit("https://android-kotlin-fun-mars-server.appspot.com/").create(MarsApiService::class.java)
    }
}
object RickApi {
    val service: RickApiService by lazy {
        retrofit("https://rickandmortyapi.com/api/").create(RickApiService::class.java)
    }
}
