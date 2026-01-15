package com.aacomposetest

import android.app.Application
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.StringRes
import androidx.paging.Config
import com.aacomposetest.MyApplication.Companion.crashLogger
import java.io.File
import java.io.IOException

class CrashReporter(val foldername:String? = null) {

    val folder = if(foldername!=null){
        File(Environment.DIRECTORY_DOWNLOADS,foldername).path
    }else{
        File(Environment.DIRECTORY_DOWNLOADS)
    }

    fun writeCrashToFile(throwable: Throwable,context: Context, folderName: String? = foldername) {
        val crashText = """
        ===== CRASH REPORT =====
        Time: ${System.currentTimeMillis()}
        
        Exception: ${throwable.javaClass.name}
        Message: ${throwable.message ?: "No message"}
        
        Stack Trace:
        ${throwable.stackTraceToString()}
        
        ===== CAUSE =====
        ${getCauseChain(throwable)}
        
        ===== THREAD INFO =====
        Thread: ${Thread.currentThread().name}
        
        ===== DEVICE INFO =====
        ${getDeviceInfo()}
    """.trimIndent()

        writeToDownloads(
            text = crashText,
            context = context,
            folderName = folderName,
            baseFilename = throwable::class.simpleName.toString()
        ).getOrNull()?.path
    }

    private fun getCauseChain(throwable: Throwable): String {
        val causes = StringBuilder()
        var current: Throwable? = throwable
        var depth = 0

        while (current != null && depth < 10) {
            causes.append("Caused by: ${current.javaClass.name}: ${current.message}\n")
            current.stackTrace.take(10).forEach {
                causes.append("    at $it\n")
            }
            current = current.cause
            depth++
        }

        return causes.toString()
    }

    private fun getDeviceInfo(): String {
        return """
        Android Version: ${Build.VERSION.SDK_INT}
        Device: ${Build.MANUFACTURER} ${Build.MODEL}
    """.trimIndent()
    }

    // TODO DO NOT USE OUTSIDE OF HERE. There's a lot of hardcoded shit
    private fun writeToDownloads(
        text: String,
        folderName: String?,
        baseFilename: String,
        context: Context,
        mimeType: String = "text/plain"
    ): Result<Uri> {


        val contentResolver = context.contentResolver
        var filename = baseFilename

        var tries = 0
        val maxTries = 1000

        var exception:Exception? = null

        // Prepare content values
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        }

        while (tries < maxTries){

            try {
                contentValues.apply {
                    // For Android 10+, we can specify relative path
                    val relativePath = if (!folderName.isNullOrBlank()) {
                        "${Environment.DIRECTORY_DOWNLOADS}/${folderName}"
                    } else {
                        Environment.DIRECTORY_DOWNLOADS
                    }
                    put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                }


                // Insert file into MediaStore
                val uri = contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    contentValues
                )
                if(uri == null){
                    throw IOException("Failed to create file")
                }

                // Write content
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(text.toByteArray())
                }

                return Result.success(uri)

            } catch (e: Exception) {
                filename = baseFilename+"_${tries}"
                tries++

                exception = e
            }



        }

        return Result.failure(exception!!)



    }

    private fun countFilesInFolderInDownloads(contentResolver: ContentResolver, folderName: String): Int {
        return try {
            val collection =
                MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL)

            val projection = arrayOf(MediaStore.Downloads._ID)

            // Query for files in the folder
            val selection =
                "${MediaStore.Downloads.RELATIVE_PATH} = ?"

            val selectionArgs = arrayOf(
                "Download/$folderName"
            )

            contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                cursor.count
            } ?: 0

        } catch (e: SecurityException) {
            // Permission error
            0
        } catch (e: Exception) {
            0
        }
    }
}