package com.example.mangacollect

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import java.io.File
import java.io.IOException
import java.net.URL


class MainActivity : AppCompatActivity() {
    lateinit var button: MaterialButton
    lateinit var progressBar: ProgressBar
    lateinit var input: TextInputEditText
    var img: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
    }

    fun initViews() {
        val requestPermission =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                } else {
                    val snackbar: Snackbar =
                        Snackbar.make(button, "Permission is't given", Snackbar.LENGTH_LONG)
                    snackbar.show()
                }
            }
        input = findViewById(R.id.text_input)
        progressBar = findViewById(R.id.progressbar)
        button = findViewById(R.id.button)
        button.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()
                    ){
                    try{
                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                        intent.addCategory("android.intent.category.DEFAULT")
                        intent.data = Uri.parse(
                            java.lang.String.format(
                                "package:%s",
                                getPackageName()
                            )
                        )
                        startActivityForResult(intent, 101)
                    }
                    catch(e:Exception) {
                        val intent = Intent()
                        intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                        startActivityForResult(intent, 101)
                    }
                } else {
                    progressBar.visibility = View.VISIBLE
                    CoroutineScope(Dispatchers.Main).launch {
                        val manga = Manga("https://mangapoisk.ru/manga/kono-subarashii-sekai-ni-shukufuku-wo-abs37Zn/chapter/17-100").initManga()
                        if (PDFManga.createMangaPdf(manga, "MyMangaCollection"))
                            progressBar.visibility = View.GONE
                    }
                }
            }
            else{
                if (ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermission.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                } else {
                    progressBar.visibility = View.VISIBLE
                    CoroutineScope(Dispatchers.Main).launch {
                        val manga = Manga("https://mangapoisk.ru/manga/kono-subarashii-sekai-ni-shukufuku-wo-abs37Zn/chapter/17-100").initManga()
                        if (PDFManga.createMangaPdf(manga, "MyMangaCollection"))
                            progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }

    suspend fun name_of_title(url: String): String {
        var src: String? = null
        return CoroutineScope(Dispatchers.IO).async {
            val doc =
                Jsoup.connect(url)
                    .userAgent("Chrome")
                    .get()
            val elems =
                doc.select("body > div.container-fluid.chapter-container > div:nth-child(1) > h1")
            return@async elems.text()
        }.await()
    }

    fun document_creator(url: String) {
        CoroutineScope(Dispatchers.Main).launch {
            documentcreator(url)
        }
    }

    suspend fun documentcreator(url: String) {
        val number = number_of_pages(url)
        val name = name_of_title(url)
        var document = PdfDocument()
        val directory =
            File(Environment.getExternalStorageDirectory().path, "MyMangaCollection")
        if (!directory.exists()) {
            directory.mkdirs();
        }

        val root = File(directory, "$name.pdf")
        try {
            val a = root.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val pageInfo = PdfDocument.PageInfo.Builder(100, 150, 1).create()
        val page = document.startPage(pageInfo)
        get_IMG(url, 1)?.let {
            page.canvas.drawBitmap(it, null, Rect(0, 0, 100, 150), null)
        }
        document.finishPage(page);
        for (count in (2..(number + 1))) {
            val pageInfo_varriable = PdfDocument.PageInfo.Builder(100, 150, count).create()
            val page_varriable = document.startPage(pageInfo_varriable)
            get_IMG(url, count)?.let {
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
            withContext(Dispatchers.Main) {
                progressBar.visibility = View.GONE
            }
        }
        // close the documente
    }

    suspend fun get_IMG(url: String, count: Int): Bitmap? {
        var mIcon11: Bitmap? = null
        return CoroutineScope(Dispatchers.IO).async {
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
}
