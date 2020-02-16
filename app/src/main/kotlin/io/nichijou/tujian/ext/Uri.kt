package io.nichijou.tujian.ext

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import io.nichijou.tujian.BuildConfig
import java.io.File


fun Uri.toFile(context: Context): File? {
  val path = toFilePath(context)
  if (path != null) {
    return File(path)
  }
  return null
}

fun Uri.isLocalStorageDocument(): Boolean = BuildConfig.DOCUMENTS_PROVIDER_AUTHORITY == this.authority

fun Uri.isExternalStorageDocument(): Boolean = "com.android.externalstorage.documents" == this.authority

fun Uri.isDownloadsDocument(): Boolean = "com.android.providers.downloads.documents" == this.authority

fun Uri.isMediaDocument(): Boolean = "com.android.providers.media.documents" == this.authority

fun Uri.isGooglePhotosUri(): Boolean = "com.google.android.apps.photos.content" == this.authority

fun Uri.toFilePath(context: Context): String? {
  var result: String? = null
  if (DocumentsContract.isDocumentUri(context, this)) {
    when {
      isLocalStorageDocument() -> result = DocumentsContract.getDocumentId(this)
      isExternalStorageDocument() -> {
        val docId = DocumentsContract.getDocumentId(this)
        val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val type = split[0]
        if ("primary".equals(type, ignoreCase = true)) {
          result = Environment.getExternalStorageDirectory().toString() + "/" + split[1]
        }
      }
      isDownloadsDocument() -> {
        val id = DocumentsContract.getDocumentId(this)
        val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), id.toLong())
        result = contentUri.getDataColumn(context, null, null)
      }
      isMediaDocument() -> {
        val docId = DocumentsContract.getDocumentId(this)
        val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val type = split[0]
        var contentUri: Uri? = null
        when (type) {
          "image" -> contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
          "video" -> contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
          "audio" -> contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        val selection = "_id=?"
        val selectionArgs = arrayOf(split[1])
        result = contentUri?.getDataColumn(context, selection, selectionArgs)
      }
    }
  }
  if (result == null) {
    if (ContentResolver.SCHEME_CONTENT.equals(this.scheme!!, ignoreCase = true)) {
      result = if (isGooglePhotosUri()) {
        this.lastPathSegment
      } else {
        getDataColumn(context, null, null)
      }
    } else if (ContentResolver.SCHEME_FILE.equals(this.scheme!!, ignoreCase = true)) {
      result = this.path
    }
  }
  return result
}

fun Uri.getDataColumn(context: Context, selection: String?, selectionArgs: Array<String>?): String? {
  var cursor: Cursor? = null
  val column = "_data"
  val projection = arrayOf(column)
  try {
    cursor = context.contentResolver.query(this, projection, selection, selectionArgs, null)
    if (cursor != null && cursor.moveToFirst()) {
      return cursor.getString(cursor.getColumnIndexOrThrow(column))
    }
  } finally {
    cursor?.close()
  }
  return null
}
