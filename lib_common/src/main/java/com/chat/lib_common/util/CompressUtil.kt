package com.chat.lib_common.util

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.util.Log
import com.blankj.utilcode.util.ImageUtils
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.collections.contains
import kotlin.io.copyTo
import kotlin.io.readBytes
import kotlin.io.use
import kotlin.math.roundToLong
import kotlin.ranges.contains
import kotlin.text.endsWith
import kotlin.text.equals
import kotlin.text.format
import kotlin.text.toLong

object CompressUtil {


    fun compressImage(uri: Uri, context: Context, quality: Int = 50): ByteArray {
        val originalSize = getOriginalImageSize(uri, context)
        Log.i("CompressUtil", "start size: ${formatSize(originalSize)}")

        var bitmap: Bitmap? = null
        try {
            bitmap = uriToBitmap(uri, context)
                ?: return readOriginalBytes(uri, context)

            val mimeType = context.contentResolver.getType(uri)
            val format = when {
                mimeType?.equals("image/png", ignoreCase = true) == true ||
                        uri.toString().endsWith(".png", true) -> Bitmap.CompressFormat.PNG

                else -> Bitmap.CompressFormat.JPEG
            }

            val compressedData = ByteArrayOutputStream().use { output ->
                val success = bitmap.compress(format, quality, output)
                if (!success) {

                    return readOriginalBytes(uri, context)
                }
                output.toByteArray()
            }

            Log.i(
                "CompressUtil",
                "compresse size: ${formatSize(compressedData.size.toLong())}，: ${
                    String.format("%.2f%%", compressedData.size.toFloat() / originalSize * 100)
                }"
            )
            return compressedData

        } catch (e: Exception) {
            e.printStackTrace()
            return readOriginalBytes(uri, context)
        } finally {
            if (bitmap != null && !bitmap.isRecycled) {
                bitmap.recycle()
            }
            System.gc()
        }
    }


    fun compressImage(bitmap: Bitmap, quality: Int = 50): ByteArray {

        try {
            val format = Bitmap.CompressFormat.JPEG

            val compressedData = ByteArrayOutputStream().use { output ->
                val success = bitmap.compress(format, quality, output)

                output.toByteArray()
            }

            Log.i(
                "CompressUtil",
                "compresse size: ${formatSize(compressedData.size.toLong())}，"
            )
            return compressedData

        } catch (e: Exception) {
            e.printStackTrace()
            return ImageUtils.bitmap2Bytes(bitmap)

        } finally {
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
            System.gc()
        }
    }

    private fun uriToBitmap(uri: Uri, context: Context): Bitmap? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.isMutableRequired = true
            }
        } else {
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    private fun readOriginalBytes(uri: Uri, context: Context): ByteArray {
        return context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)
    }

    fun saveCompressedImage(context: Context, data: ByteArray): File? = try {
        val dir = context.getExternalFilesDir("compressed_images")
        if (dir?.exists() == false) dir.mkdirs()
        val fileName =
            "compressed_${
                SimpleDateFormat(
                    "yyyyMMddHHmmss",
                    Locale.getDefault()
                ).format(Date())
            }.jpg"
        val file = File(dir, fileName)
        FileOutputStream(file).use { it.write(data) }
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    @SuppressLint("Recycle")
    private fun getOriginalImageSize(uri: Uri, context: Context): Long {
        return try {
            when (uri.scheme) {
                ContentResolver.SCHEME_FILE -> File(uri.path ?: return 0).length()
                ContentResolver.SCHEME_CONTENT ->
                    context.contentResolver.openFileDescriptor(uri, "r")?.statSize ?: 0L

                else -> 0L
            }
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        }
    }

    private fun formatSize(bytes: Long): String {

        try {

            return when {
                bytes >= 1024 * 1024 -> String.format("%.2f MB", bytes / (1024f * 1024))
                bytes >= 1024 -> String.format("%.2f KB", bytes / 1024f)
                else -> "$bytes B"
            }
        } catch (e: Exception) {
            e.printStackTrace()

        }
        return "$bytes B"

    }


    private fun copyUriToCache(context: Context, uri: Uri): File {
        val outFile = File(context.cacheDir, "src_${System.currentTimeMillis()}.mp4")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(outFile).use { output -> input.copyTo(output) }
        }
        return outFile
    }


    fun getVideoThumbnail(context: Context, uri: Uri): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
//            ImageUtils.bitmap2Bytes(retriever.getFrameAtTime(0))
            retriever.getFrameAtTime(0)
        } catch (e: Exception) {
            e.printStackTrace(); null
        } finally {
            retriever.release()
        }
    }

    fun getVideoDuration(context: Context, uri: Uri?): Long {

        if (null == uri) return 0L

        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            val time =
                (retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong())
                    ?: 0L

            (time / 1000.0).roundToLong()

        } catch (e: Exception) {
            e.printStackTrace(); 0L
        } finally {
            retriever.release()
        }
    }


    private fun getFileSize(context: Context, uri: Uri): Long {
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex != -1) {
                    cursor.moveToFirst()
                    return cursor.getLong(sizeIndex) // ：Byte
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }


    fun isValidMedia(context: Context, uri: Uri?, isImage: Boolean): Boolean {

        try {
            if (null == uri) return false

            val fileType = context.contentResolver.getType(uri)

            val fileSize = getFileSize(context, uri)

            Log.i("TAG", "isValidMedia: ${fileSize / 1024 / 1024}")

            return if (isImage) {
                val maxSize = 4 * 1024 * 1024 //
                val validType = fileType != null && imageMimeTypes.contains(fileType)
                val validSize = fileSize in 1..maxSize
                validType && validSize
            } else {
                val maxSize = 30 * 1024 * 1024 // 30MB
                val validType = fileType != null && videoMimeTypes.contains(fileType)
                val validSize = fileSize in 1..maxSize
                val duration = getVideoDuration(context, uri) //
                val validDuration = duration in 1..30
                validType && validSize && validDuration
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false

    }
}