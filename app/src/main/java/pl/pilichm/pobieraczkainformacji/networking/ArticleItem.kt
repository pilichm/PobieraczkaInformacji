package pl.pilichm.pobieraczkainformacji.networking

import com.google.gson.annotations.SerializedName

data class ArticleItem(
    @SerializedName("time")
    val time: Int,
    @SerializedName("by")
    val author: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("url")
    val url: String
)