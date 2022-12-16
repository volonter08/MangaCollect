package com.example.mangacollect

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.pm.PackageManager
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException


class PDFManga {
    companion object {
        suspend fun createMangaPdf(manga: Manga, nameDirectory: String): Boolean {
            return CoroutineScope(Dispatchers.IO).async {
                var document = PdfDocument()
                val directory = File(Environment.getExternalStorageDirectory(), "$nameDirectory")
                if (!directory.exists()) {
                    directory.mkdirs()
                }
                val rootFile = File(directory, "${manga.getNameManga()}.pdf")
                try {
                    rootFile.createNewFile()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                for (bitmap in manga.getListBitmap()) {
                    val pageInfo =
                        PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
                    val page = document.startPage(pageInfo)
                    page.canvas.drawBitmap(
                        bitmap,
                        null,
                        Rect(0, 0, bitmap.width, bitmap.height),
                        null
                    )
                    document.finishPage(page)
                }

                try {
                    document.writeTo(rootFile.outputStream());
                    document.close()
                    return@async true
                } catch (e: IOException) {
                    e.printStackTrace()
                    return@async false
                }
            }.await()
        }
    }
}
