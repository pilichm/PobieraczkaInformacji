package pl.pilichm.pobieraczkainformacji

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_article.*
import pl.pilichm.pobieraczkainformacji.recyclerview.Constants
import java.lang.Exception

class ArticleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article)

        /**
         * Display article if url was passed.
         * */
        if (intent.hasExtra(Constants.EXTRA_ARTICLE_URL)){
            val articleUrl = intent.getStringExtra(Constants.EXTRA_ARTICLE_URL)
            val articleBody = intent.getStringExtra(Constants.EXTRA_ARTICLE_BODY)
            if (articleUrl != null && internetEnabled()) {
                webview.loadUrl(articleUrl)
            } else {
                try {
                    webview.loadData(articleBody!!, "text/html; charset=utf-8", "UTF-8")
                } catch (e: Exception){
                    Log.e("WBERR", "${e.printStackTrace()}")
                }
            }
        }
    }

    private fun internetEnabled(): Boolean {
        val cm = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }
}