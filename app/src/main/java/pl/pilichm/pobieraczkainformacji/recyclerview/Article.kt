package pl.pilichm.pobieraczkainformacji.recyclerview

data class Article(
    val articleTitle: String,
    val authorName: String,
    val creationDate: Int,
    val articleUrl: String,
    val articleBody: String = ""
)
