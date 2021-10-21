package pl.pilichm.pobieraczkainformacji

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_article.*
import pl.pilichm.pobieraczkainformacji.recyclerview.Constants

class ArticleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article)

        /**
         * Display article if url was passed.
         * */
        if (intent.hasExtra(Constants.EXTRA_ARTICLE_URL)){
            val articleUrl = intent.getStringExtra(Constants.EXTRA_ARTICLE_URL)
            if (articleUrl != null) {
                webview.loadUrl(articleUrl)
            }
        }
    }
}