package pl.pilichm.pobieraczkainformacji.recyclerview

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pl.pilichm.pobieraczkainformacji.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ArticleRecyclerView(private val articles: ArrayList<Article>)
    : RecyclerView.Adapter<ArticleRecyclerView.ViewHolder>() {
    private var onClickListener: OnClickListener? = null

    interface OnClickListener {
        fun onClick(position: Int, item: Article)
    }

    fun addOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvArticleTitle = itemView.findViewById(R.id.tvArticleTitle) as TextView
        val tvAuthor = itemView.findViewById(R.id.tvAuthor) as TextView
        val tvCreationTime = itemView.findViewById(R.id.tvCreationTime) as TextView
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val article = articles[position]
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val netDate = Date((article.creationDate * 1000).toLong())
        val date = sdf.format(netDate)

        holder.tvArticleTitle.text = article.articleTitle
        holder.tvArticleTitle.typeface = Typeface.DEFAULT_BOLD
        holder.tvAuthor.text = "by: ${article.authorName}"
        holder.tvCreationTime.text = date

        holder.itemView.setOnClickListener {
            if (onClickListener!=null){
                onClickListener!!.onClick(position, article)
            }
        }
    }

    override fun getItemCount(): Int {
        return articles.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val layoutInflater = LayoutInflater.from(context)
        val noteView = layoutInflater.inflate(R.layout.news_item, parent, false)
        return ViewHolder(noteView)
    }

}