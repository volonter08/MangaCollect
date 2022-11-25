package com.example.mangacollect

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.*
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.net.URL
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity(){
    lateinit var button: MaterialButton
    lateinit var progressBar: ProgressBar
    var img: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        //doInBackground("https://static2.mangapoisk.ru/pages/4/407588/OaBFqRzwU1MiLMf1Uzoeu1sYciC66J2Pk4gJ6LFd.jpg")
    }

    fun initViews() {
        progressBar= findViewById(R.id.progressbar)
        button= findViewById(R.id.button)
        button.setOnClickListener {
            progressBar.visibility= View.VISIBLE
            document_creator("https://mangapoisk.ru/manga/vanpanchmen/chapter/32-219")
        }
    }
    suspend fun name_of_title(url: String):String{
       var src: String? = null
        return CoroutineScope(Dispatchers.IO).async {
                val doc =
                    Jsoup.connect(url)
                        .userAgent("Chrome")
                        .get()
                val elems = doc.select("body > div.container-fluid.chapter-container > div:nth-child(1) > h1")
                return@async elems.text()
            }.await()
    }
    fun document_creator(url: String){
        CoroutineScope(Dispatchers.Main).launch{
            documentcreator(url)
        }
    }
    fun connection_and_get_img() {
        var src: String? = null
        CoroutineScope(Dispatchers.IO).launch {
            val doc =
                Jsoup.connect("https://mangapoisk.ru/manga/vanpanchmen/chapter/30-290.2")
                    .userAgent("Chrome")
                    .get()
            val elem = doc.select("#page-1")
            val elems = doc.select("img.img-fluid.page-image.lazy.lazy-preload")
            withContext(Dispatchers.Main) {
                src = elem.attr("src")
                val count = elems.size + 1
                elems.forEach {
                    doInBackground(it.attr("data-src"))
                }
                return@withContext
            }
        }
    }

    fun doInBackground(urls: String?) {
        var mIcon11: Bitmap? = null
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val `in` = URL(urls).openStream()
                mIcon11 = BitmapFactory.decodeStream(`in`)
            } catch (e: Exception) {
                Log.d("MainActivity", "FF")
                e.printStackTrace()
            }
            withContext(Dispatchers.Main) {
                pdfcreator(mIcon11)
            }
        }
    }

    suspend fun documentcreator(url: String) {
        val number= number_of_pages(url)
        val name = name_of_title(url)
        var document = PdfDocument()
        val directory = File(Environment.getExternalStorageDirectory().path, "MyMangaCollection")
        if (!directory.exists()) {
            directory.mkdirs();
        }
        val root = File(directory, "$name.pdf")
        try {
            val a = root.createNewFile()
            Log.d("MainActivity", a.toString())
        } catch (e: IOException) {
            Log.d("MainActivity", "F")
            e.printStackTrace()
        }
        val pageInfo = PdfDocument.PageInfo.Builder(100, 150, 1).create()
        val page = document.startPage(pageInfo)
        get_IMG(url,1)?.let {
          page.canvas.drawBitmap(it, null, Rect(0, 0, 100, 150), null)
        }
        document.finishPage(page);
        for ( count in (2..(number+1))){
            val pageInfo_varriable= PdfDocument.PageInfo.Builder(100, 150, count).create()
            val page_varriable = document.startPage(pageInfo_varriable)
            get_IMG(url,count)?.let{
                page_varriable.canvas.drawBitmap(it, null, Rect(0, 0, 100, 150), null)
            }
            document.finishPage(page_varriable)
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                document.writeTo(root.outputStream());
            } catch (e: IOException) {
                Log.d("MainActivity", "FF")
                e.printStackTrace()
            }
            document.close();
            withContext(Dispatchers.Main){
                progressBar.visibility= View.GONE
            }
        }
        // close the documente
    }

    suspend fun get_IMG(url: String, count: Int):Bitmap? {
        var mIcon11: Bitmap? = null
        return CoroutineScope(Dispatchers.IO).async{
            val doc =
                Jsoup.connect(url)
                    .userAgent("Chrome")
                    .get()
            val elem = doc.select("#page-$count")
            if (count == 1) {
                val src1 = elem.attr("src")
                try {
                    val `in` = URL(src1).openStream()
                    mIcon11 = BitmapFactory.decodeStream(`in`)
                } catch (e: Exception) {
                    Log.d("MainActivity", "FF")
                    e.printStackTrace()
                }
            } else {
                val src2 = elem.attr("data-src")
                try {
                    val `in` = URL(src2).openStream()
                    mIcon11 = BitmapFactory.decodeStream(`in`)
                } catch (e: Exception) {
                    Log.d("MainActivity", "FF")
                    e.printStackTrace()
                }
            }
            return@async mIcon11
        }.await()
    }
    suspend fun number_of_pages(url: String): Int {
        var src: String? = null
        return CoroutineScope(Dispatchers.IO).async {
            val doc =
                Jsoup.connect(url)
                    .userAgent("Chrome")
                    .get()
            val elems = doc.select("img.img-fluid.page-image.lazy.lazy-preload")
            elems.size
            return@async elems.size
        }.await()
    }

    fun pdfcreator(img: Bitmap?) {
        val directory = File(Environment.getExternalStorageDirectory().path, "MyMangaCollection")
        if (!directory.exists()) {
            directory.mkdirs();
        }
        val root = File(directory, "Onepuncjj.pdf")
        val x = -20
        val y = -5
        val a = x.toFloat()
        val b = y.toFloat()
        val paint = Paint()
        try {
            val a = root.createNewFile()
            Log.d("MainActivity", a.toString())
        } catch (e: IOException) {
            Log.d("MainActivity", "F")
            e.printStackTrace()
        }
        var document = PdfDocument();

        val pageInfo = PdfDocument.PageInfo.Builder(100, 150, 1).create();

        val page = document.startPage(pageInfo);
        img?.let {
            page.canvas.drawBitmap(it, null, Rect(0, 0, 100, 150), null)
        }
        // draw something on the page

        // finish the page
        document.finishPage(page);
        // write the document content

        try {
            document.writeTo(root.outputStream());
        } catch (e: IOException) {
            Log.d("MainActivity", "FF")
            e.printStackTrace()
        }
        // close the document
        document.close();
    }
}
