package com.example.mangacollect

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import org.jsoup.Jsoup
import java.net.URL
import java.util.LinkedList
import kotlinx.coroutines.*
import kotlin.properties.Delegates

class Manga(url: String) {
    private var listBitmap: MutableList<Bitmap> = LinkedList<Bitmap>();
    private val url: String = url;
    private lateinit var nameManga:String;
    private var numberPages by Delegates.notNull<Int>()
    init {
        CoroutineScope(Dispatchers.IO).launch {
            val doc = Jsoup.connect(url)
                .userAgent("Chrome")
                .get()
            nameManga = doc.select("body > div.container-fluid.chapter-container > div:nth-child(1) > h1").text()
            numberPages = doc.select("img.img-fluid.page-image.lazy.lazy-preload").size
            for (count in (1 .. numberPages)) {
                val elem = doc.select("#page-$count")
                if (count == 1) {
                    val src = elem.attr("src")
                    try {
                        val `in` = URL(src).openStream()
                        listBitmap.add(BitmapFactory.decodeStream(`in`))
                    } catch (e: Exception) {
                        Log.d("MainActivity", "FF")
                        e.printStackTrace()
                    }
                } else {
                    val dataSrc = elem.attr("data-src")
                    try {
                        val `in` = URL(dataSrc).openStream()
                        listBitmap.add(BitmapFactory.decodeStream(`in`))
                    } catch (e: Exception) {
                        Log.d("MainActivity", "FF")
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun getListBitmap():List<Bitmap>{
        return listBitmap
    }
    fun getBitmapPage(number: Int): Bitmap? {
        return this.listBitmap[number]
    }
    fun getNameManga():String{
        return nameManga
    }
    fun getNumberPageManga(): Int{
        return numberPages
    }
}