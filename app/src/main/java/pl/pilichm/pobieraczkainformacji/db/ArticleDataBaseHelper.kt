package pl.pilichm.pobieraczkainformacji.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import pl.pilichm.pobieraczkainformacji.recyclerview.Article

class ArticleDataBaseHelper(context: Context):
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        const val DB_NAME = "articles_db"
        const val DB_VERSION = 1

        /**
         * Table name.
         * */
        const val DB_ARTICLE_TABLE_NAME = "ARTICLE"

        /**
         * Article table columns.
         * */
        const val COL_ARTICLE_TITLE = "TITLE"
        const val COL_ARTICLE_AUTHOR = "AUTHOR"
        const val COL_ARTICLE_CREATION_DATE = "CREATION_DATE"
        const val COL_ARTICLE_URL = "URL"
        const val COL_ARTICLE_BODY = "HTML_BODY"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val stmCreateArticleTable = "CREATE TABLE $DB_ARTICLE_TABLE_NAME ( " +
                "$COL_ARTICLE_TITLE TEXT, " +
                "$COL_ARTICLE_AUTHOR TEXT, " +
                "$COL_ARTICLE_CREATION_DATE INTEGER, " +
                "$COL_ARTICLE_URL TEXT, " +
                "$COL_ARTICLE_BODY TEXT)"
        db?.execSQL(stmCreateArticleTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val stmDropArticleTable = "DROP TABLE IF EXISTS $DB_ARTICLE_TABLE_NAME"
        db?.execSQL(stmDropArticleTable)
        onCreate(db)
    }

    override fun onConfigure(db: SQLiteDatabase?) {
        super.onConfigure(db)
        db?.setForeignKeyConstraintsEnabled(true)
    }

    /**
     * Method for saving new article.
     * */
    fun saveArticle(article: Article, articleBody: String): Int {
        val db = writableDatabase
        db.beginTransaction()

        try {
            /**
             * Check if article with passed title already exists.
             * */
            val stmCheckIfExists = "SELECT COUNT(*) FROM $DB_ARTICLE_TABLE_NAME " +
                    "WHERE $COL_ARTICLE_TITLE = ?"
            val cursor = db.rawQuery(stmCheckIfExists, arrayOf(article.articleTitle))
            val articleCount: Int

            try {
                cursor.moveToFirst()
                articleCount = cursor.getInt(0)
                db.setTransactionSuccessful()
            } finally {
                if (cursor!=null&&!cursor.isClosed){
                    cursor.close()
                }
            }

            /**
             * Save article if it doesn't exist in db.
             * */
            if (articleCount==0){
                val values = ContentValues()
                values.put(COL_ARTICLE_TITLE, article.articleTitle)
                values.put(COL_ARTICLE_AUTHOR, article.authorName)
                values.put(COL_ARTICLE_CREATION_DATE, article.creationDate)
                values.put(COL_ARTICLE_URL, article.articleUrl)
                values.put(COL_ARTICLE_BODY, articleBody)

                db.insertOrThrow(DB_ARTICLE_TABLE_NAME, null, values)
                return 1
            } else {
                Log.i("ArticleDataBaseHelper", "Article already exists!")
            }

        } catch (e: SQLiteException){
            Log.e("ArticleDataBaseHelper", "Error while saving article!")
            Log.e("ArticleDataBaseHelper", "${e.printStackTrace()}")
            return 0
        } finally {
            db.endTransaction()
        }
        return 0
    }

    fun readAllArticles(): ArrayList<Article> {
        val articles = ArrayList<Article>()
        val db = readableDatabase
        val stmSelectArticles = "SELECT * FROM $DB_ARTICLE_TABLE_NAME "

        val cursor = db.rawQuery(stmSelectArticles, null)
        try {
            if (cursor.moveToFirst()){
                do {
                    val articleTitle = cursor.getString(cursor.getColumnIndex(COL_ARTICLE_TITLE))
                    val authorName = cursor.getString(cursor.getColumnIndex(COL_ARTICLE_AUTHOR))
                    val creationDate = cursor.getInt(cursor.getColumnIndex(COL_ARTICLE_CREATION_DATE))
                    val articleUrl = cursor.getString(cursor.getColumnIndex(COL_ARTICLE_URL))
                    val articleBody = cursor.getString(cursor.getColumnIndex(COL_ARTICLE_BODY))

                    articles.add(Article(articleTitle, authorName, creationDate, articleUrl, articleBody))
                } while (cursor.moveToNext())
            }
        } catch (e: SQLiteException){
            Log.e("ArticleDataBaseHelper", "Error while reading newest articles!")
            Log.e("ArticleDataBaseHelper", "${e.printStackTrace()}")
        } finally {
            if (cursor!=null&&!cursor.isClosed){
                cursor.close()
            }
        }

        Log.i("ArticleDataBaseHelper", "Returning ${articles.size} articles.")
        return articles
    }

}