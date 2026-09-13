package com.tosin.musicplayer.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.util.Size
import java.io.ByteArrayOutputStream

/**
 * Produces memory-safe artwork bitmaps for RemoteViews and extracts artwork
 * from content URIs, system thumbnails, and embedded media tags.
 */
object WidgetArtworkHelper {

    private const val MAX_DIM = 512

    fun extractArtworkBytes(
        context: Context,
        albumArtUriStr: String?,
        songUriStr: String?
    ): ByteArray? {
        // 1. Try loading directly from the album art URI if provided
        if (!albumArtUriStr.isNullOrBlank()) {
            val bitmap = loadBitmapFromUri(context, albumArtUriStr)
            if (bitmap != null) {
                return compressAndRecycle(bitmap)
            }
        }

        // 2. Try loading via ContentResolver.loadThumbnail on API 29+ using the audio URI
        if (!songUriStr.isNullOrBlank() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            runCatching {
                val uri = Uri.parse(songUriStr)
                context.contentResolver.loadThumbnail(uri, Size(MAX_DIM, MAX_DIM), null)
            }.getOrNull()?.let { thumbnail ->
                return compressAndRecycle(thumbnail)
            }
        }

        // 3. Try loading embedded album art from the audio file using MediaMetadataRetriever
        if (!songUriStr.isNullOrBlank()) {
            runCatching {
                val retriever = MediaMetadataRetriever()
                val uri = Uri.parse(songUriStr)
                if (songUriStr.startsWith("content://")) {
                    retriever.setDataSource(context, uri)
                } else if (songUriStr.startsWith("file://")) {
                    retriever.setDataSource(uri.path)
                } else {
                    retriever.setDataSource(songUriStr)
                }
                val art = retriever.embeddedPicture
                retriever.release()
                if (art != null && art.isNotEmpty()) {
                    val decoded = BitmapFactory.decodeByteArray(art, 0, art.size)
                    if (decoded != null) {
                        return compressAndRecycle(decoded)
                    }
                }
            }
        }

        return null
    }

    private fun loadBitmapFromUri(context: Context, uriStr: String): Bitmap? {
        return runCatching {
            val uri = Uri.parse(uriStr)
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        }.getOrNull()
    }

    private fun compressAndRecycle(bitmap: Bitmap): ByteArray {
        val scaled = scaleDownIfNeeded(bitmap, MAX_DIM)
        val stream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.PNG, 90, stream)
        if (scaled != bitmap && !bitmap.isRecycled) {
            bitmap.recycle()
        }
        return stream.toByteArray()
    }

    private fun scaleDownIfNeeded(src: Bitmap, maxDim: Int): Bitmap {
        if (src.width <= maxDim && src.height <= maxDim) return src
        val ratio = src.width.toFloat() / src.height.toFloat()
        val (w, h) = if (ratio > 1) {
            maxDim to (maxDim / ratio).toInt()
        } else {
            (maxDim * ratio).toInt() to maxDim
        }
        return Bitmap.createScaledBitmap(src, w.coerceAtLeast(1), h.coerceAtLeast(1), true)
    }

    fun createCircularBitmap(src: Bitmap, size: Int = 128): Bitmap {
        val safeSize = size.coerceAtLeast(1)
        val output = Bitmap.createBitmap(safeSize, safeSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = Rect(0, 0, safeSize, safeSize)
        val rectF = RectF(rect)

        val scaledSrc = Bitmap.createScaledBitmap(src, safeSize, safeSize, true)
        canvas.drawOval(rectF, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(scaledSrc, rect, rect, paint)
        if (scaledSrc != src && !scaledSrc.isRecycled) {
            scaledSrc.recycle()
        }
        return output
    }

    fun createRoundedBitmap(src: Bitmap, width: Int, height: Int, cornerRadiusDp: Float, density: Float): Bitmap {
        val safeWidth = width.coerceAtLeast(1)
        val safeHeight = height.coerceAtLeast(1)
        val radiusPx = (cornerRadiusDp * density).coerceAtLeast(0f)
        val output = Bitmap.createBitmap(safeWidth, safeHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = Rect(0, 0, safeWidth, safeHeight)
        val rectF = RectF(0f, 0f, safeWidth.toFloat(), safeHeight.toFloat())

        val scaledSrc = Bitmap.createScaledBitmap(src, safeWidth, safeHeight, true)
        canvas.drawRoundRect(rectF, radiusPx, radiusPx, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(scaledSrc, rect, rect, paint)
        if (scaledSrc != src && !scaledSrc.isRecycled) {
            scaledSrc.recycle()
        }
        return output
    }

    fun createDimmedBackgroundBitmap(src: Bitmap, targetWidth: Int = 300, targetHeight: Int = 300): Bitmap {
        val safeWidth = targetWidth.coerceAtLeast(1)
        val safeHeight = targetHeight.coerceAtLeast(1)
        val scaled = Bitmap.createScaledBitmap(src, safeWidth, safeHeight, true)
        val output = Bitmap.createBitmap(safeWidth, safeHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        canvas.drawBitmap(scaled, 0f, 0f, paint)
        // Darken overlay
        canvas.drawColor(0x99000000.toInt())
        if (scaled != src && !scaled.isRecycled) {
            scaled.recycle()
        }
        return output
    }
}