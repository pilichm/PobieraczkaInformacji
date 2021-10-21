package pl.pilichm.pobieraczkainformacji

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import pl.pilichm.pobieraczkainformacji.recyclerview.Article
import pl.pilichm.pobieraczkainformacji.recyclerview.ArticleRecyclerView
import pl.pilichm.pobieraczkainformacji.recyclerview.Constants

class MainActivity : AppCompatActivity() {
    private var mArticles: ArrayList<Article>? = ArrayList()
    private var mAdapter: ArticleRecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        displayListOfArticles()
    }

    /**
     * Display list of newest articles.
     * */
    private fun displayListOfArticles(){
        val article = Article(
            resources.getString(R.string.mock_news_title),
            resources.getString(R.string.mock_author_name),
            1977,
            resources.getString(R.string.mock_article_url)
        )

        mArticles!!.add(article)
        mArticles!!.add(article)
        mArticles!!.add(article)

        mAdapter = ArticleRecyclerView(mArticles!!)
        mAdapter!!.addOnClickListener(object: ArticleRecyclerView.OnClickListener {
            override fun onClick(position: Int, item: Article) {
                val intent = Intent(applicationContext, ArticleActivity::class.java)
                intent.putExtra(Constants.EXTRA_ARTICLE_URL, item.articleUrl)
                startActivity(intent)
            }
        })
        rvArticles.adapter = mAdapter
        rvArticles.layoutManager = LinearLayoutManager(this)
    }

}