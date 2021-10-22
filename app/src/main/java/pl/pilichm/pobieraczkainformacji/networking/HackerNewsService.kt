package pl.pilichm.pobieraczkainformacji.networking

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface HackerNewsService {

    companion object {
        const val BASE_URL = "https://hacker-news.firebaseio.com/v0/"
    }

    @GET("${BASE_URL}maxitem.json")
    fun getArticleMaxId(): Call<Int>

    @GET("${BASE_URL}topstories.json")
    fun getTopArticlesNumbers(): Call<String>

    @GET("${BASE_URL}item/{id}.json")
    fun getArticleData(@Path("id") id: Int?): Call<ArticleItem>

}