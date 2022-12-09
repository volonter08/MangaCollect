package com.example.mangacollect

import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.util.Log
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.lang.Exception
import kotlin.properties.Delegates

class PDFDocumentManga {
    companion object{
        fun createMangaPdf(manga:Manga,nameDirectory:String):Boolean{
            var boolean by Delegates.notNull<Boolean>()
            CoroutineScope(Dispatchers.Main).launch {
                var document = PdfDocument()
                val directory = File(Environment.getExternalStorageDirectory(), "$nameDirectory")
                if (!directory.exists()) {
                    directory.mkdirs()
                }
                val rootFile = File(directory, "$nameDirectory.pdf")
                try {
                    rootFile.createNewFile()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                val pageInfo = PdfDocument.PageInfo.Builder(100, 150, 1).create()
                for (bitmap in manga.getListBitmap()) {
                    val pageInfo = PdfDocument.PageInfo.Builder(100, 150, 1).create()
                    val page = document.startPage(pageInfo)
                    page.canvas.drawBitmap(bitmap, null, Rect(0, 0, 100, 150), null)
                    document.finishPage(page)
                }
                boolean = CoroutineScope(Dispatchers.IO).async {
                    try {
                        document.writeTo(rootFile.outputStream());
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    document.close();
                    return@async true
                }.await()
            }
            return boolean
        }
    }
}