package com.utp.basurapp.recolectorapp.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream

object ImageCompressor {

    private const val MAX_SIZE = 1024
    private const val JPEG_QUALITY = 80

    fun comprimir(context: Context, archivoOriginal: File): File {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(archivoOriginal.absolutePath, options)

        val width = options.outWidth
        val height = options.outHeight
        var sampleSize = 1

        while (width / sampleSize > MAX_SIZE || height / sampleSize > MAX_SIZE) {
            sampleSize *= 2
        }

        options.inJustDecodeBounds = false
        options.inSampleSize = sampleSize

        val bitmap = BitmapFactory.decodeFile(archivoOriginal.absolutePath, options)
            ?: return archivoOriginal

        val archivoComprimido = File(context.cacheDir, "reporte_comprimido.jpg")

        FileOutputStream(archivoComprimido).use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
        }

        if (bitmap.isRecycled.not()) {
            bitmap.recycle()
        }

        return archivoComprimido
    }
}
