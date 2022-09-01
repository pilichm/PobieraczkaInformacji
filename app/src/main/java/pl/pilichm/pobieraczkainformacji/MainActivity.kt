package pl.pilichm.pobieraczkainformacji

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import org.jsoup.Jsoup
import pl.pilichm.pobieraczkainformacji.databinding.ActivityMainBinding
import pl.pilichm.pobieraczkainformacji.db.ArticleDataBaseHelper
import pl.pilichm.pobieraczkainformacji.networking.ArticleItem
import pl.pilichm.pobieraczkainformacji.networking.HackerNewsService
import pl.pilichm.pobieraczkainformacji.recyclerview.Article
import pl.pilichm.pobieraczkainformacji.recyclerview.ArticleRecyclerView
import pl.pilichm.pobieraczkainformacji.recyclerview.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var mArticles: ArrayList<Article>? = ArrayList()
    private var mAdapter: ArticleRecyclerView? = null
    private var mDbHelper: ArticleDataBaseHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hideArticlesList()

        mDbHelper = ArticleDataBaseHelper(applicationContext)
        displayListOfArticles()

        if (internetEnabled()) {
            Log.i("MAIN-ACT", "Displaying downloaded articles.")
            downloadNewestArticles()
            notifyAdapterDataChanged()
        } else {
            Log.i("MAIN-ACT", "Displaying articles saved in db.")
            val savedArticles = mDbHelper!!.readAllArticles()
            mArticles!!.clear()
            mArticles!!.addAll(savedArticles)
            notifyAdapterDataChanged()
            showArticlesList()
        }
    }

    /**
     * Function for checking if internet connection is enabled.
     * */
    private fun internetEnabled(): Boolean {
        val cm = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }

    /**
     * Function downloading and displaying in main activity 10 latest news.
     * */
    private fun downloadNewestArticles(){
        hideArticlesList()
        val rBuilder = Retrofit.Builder()
        val service = rBuilder
            .addConverterFactory(ScalarsConverterFactory.create())
            .baseUrl(HackerNewsService.BASE_URL)
            .build().create(HackerNewsService::class.java)

        service.getTopArticlesNumbers().enqueue(object: Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful){
                    Log.i("GET_TOP_ARTICLES", "Get top articles ok response: ${response.body()}")

                    mArticles!!.clear()

                    /**
                     * Download data for 10 latest articles.
                     * */
                    val topArticles = response.body()?.replace("[", "")
                        ?.replace("]", "")?.split(",")
                    if (topArticles!!.isNotEmpty()){
                        val articleService = rBuilder
                            .addConverterFactory(GsonConverterFactory.create())
                            .baseUrl(HackerNewsService.BASE_URL)
                            .build().create(HackerNewsService::class.java)

                        downloadArticleInfo(topArticles, 5, articleService)

                        notifyAdapterDataChanged()
                    }

                } else {
                    Log.i("GET_TOP_ARTICLES", "Get top articles nok response!")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("GET_TOP_ARTICLES", "Get top articles error: ${t.printStackTrace()}}")
            }
        })
    }

    /**
     * Display list of newest articles.
     * */
    private fun displayListOfArticles(){
        mAdapter = ArticleRecyclerView(mArticles!!)
        mAdapter!!.addOnClickListener(object: ArticleRecyclerView.OnClickListener {
            override fun onClick(position: Int, item: Article) {
                val intent = Intent(applicationContext, ArticleActivity::class.java)
                intent.putExtra(Constants.EXTRA_ARTICLE_URL, item.articleUrl)
                intent.putExtra(Constants.EXTRA_ARTICLE_BODY, item.articleBody)
                startActivity(intent)
            }
        })
        binding.rvArticles.adapter = mAdapter
        binding.rvArticles.layoutManager = LinearLayoutManager(this)
    }

    /**
     * Notify adapter that new article info was added.
     * */
    private fun notifyAdapterDataChanged(){
        if (mAdapter!=null){
            mAdapter!!.notifyDataSetChanged()
        }
    }

    /**
     * Downloads new article info and web page body, displays it in activity and saves in sqlite db.
     * */
    private fun downloadArticleInfo(articles: List<String>, articleId: Int, service: HackerNewsService){
        if (articleId>=0){
            service.getArticleData(articles[articleId].toInt())
                .enqueue(object: Callback<ArticleItem>{
                    override fun onResponse(
                        call: Call<ArticleItem>,
                        response: Response<ArticleItem>
                    ) {
                        if (response.isSuccessful){
                            Log.i("GET_ARTICLE_DATA", "Get article data ok response!")
                            val article = response.body()

                            val articleObj = Article(
                                article?.title ?: resources.getString(R.string.mock_news_title),
                                article?.author ?: resources.getString(R.string.mock_author_name),
                                article?.time ?: 0,
                                article?.url ?: resources.getString(R.string.mock_article_url)
                            )
                            mArticles!!.add(articleObj)
                            notifyAdapterDataChanged()

                            Log.i("GET_ARTICLE_DATA", article.toString() ?: "EMPTY")
                            downloadArticleInfo(articles, articleId-1, service)
                        } else {
                            Log.i("GET_ARTICLE_DATA", "Get article data nok response!")
                        }
                    }

                    override fun onFailure(call: Call<ArticleItem>, t: Throwable) {
                        Log.e("GET_ARTICLE_DATA", "Get article data error: ${t.printStackTrace()}}")
                    }
                })
        } else {
            showArticlesList()
        }
    }

    /**
     * Save downloaded articles on app exit.
     * */
    override fun onPause() {
        super.onPause()
        Thread {
            if (!mArticles.isNullOrEmpty()&&mDbHelper!=null) {
                for (article in mArticles!!){
                    val doc = Jsoup.connect(article.articleUrl).get()
                    val articleHtmlBody = doc.outerHtml()
                    mDbHelper!!.saveArticle(article, articleHtmlBody)
                    Log.i("MAIN", "Article saved!")
                }
            }
        }.start()
    }

    /**
     * Shows recycler view with articles and hides progress bar.
     * */
    private fun showArticlesList(){
        binding.rvArticles.visibility = View.VISIBLE
        binding.progressBarMain.visibility = View.INVISIBLE
    }

    /**
     * Hides recycler view with articles and shows progress bar.
     * */
    private fun hideArticlesList(){
        binding.rvArticles.visibility = View.INVISIBLE
        binding.progressBarMain.visibility = View.VISIBLE
    }

    /**
     * Create menu with icon for refreshing articles info.
     * */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.main_menu_refresh -> {
                downloadNewestArticles()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun getMaxItemId(){
        val rBuilder = Retrofit.Builder()
        val service = rBuilder
            .addConverterFactory(ScalarsConverterFactory.create())
            .baseUrl("https://hacker-news.firebaseio.com")
            .build().create(HackerNewsService::class.java)

        service.getArticleMaxId().enqueue(object: Callback<Int> {
            override fun onResponse(call: Call<Int>, response: Response<Int>) {
                if (response.isSuccessful){
                    Log.i("downloadNewestArticles", "Max article id: ${response.body()}")
                } else {
                    Log.i("downloadNewestArticles", "Max article id cannot be downloaded!")
                }
            }

            override fun onFailure(call: Call<Int>, t: Throwable) {
                Log.e("downloadNewestArticles", "Failure on get max article id: ${t.printStackTrace()}}")
            }
        })
    }

}