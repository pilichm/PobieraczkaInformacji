package pl.pilichm.pobieraczkainformacji

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import pl.pilichm.pobieraczkainformacji.databinding.ActivityArticleBinding
import pl.pilichm.pobieraczkainformacji.recyclerview.Constants
import java.lang.Exception

class ArticleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityArticleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /**
         * Display article if url was passed.
         * */
        if (intent.hasExtra(Constants.EXTRA_ARTICLE_URL)){
            val articleUrl = intent.getStringExtra(Constants.EXTRA_ARTICLE_URL)
            val articleBody = intent.getStringExtra(Constants.EXTRA_ARTICLE_BODY)
            if (articleUrl != null && internetEnabled()) {
                binding.webview.loadUrl(articleUrl)
            } else {
                try {
                    binding.webview.loadData(articleBody!!, "text/html; charset=utf-8", "UTF-8")
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