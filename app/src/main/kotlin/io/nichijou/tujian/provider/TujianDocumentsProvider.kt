package io.nichijou.tujian.provider

import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.database.MatrixCursor
import android.graphics.Point
import android.os.CancellationSignal
import android.os.Environment
import android.os.Handler
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract.Document
import android.provider.DocumentsContract.Root
import android.provider.DocumentsProvider
import android.webkit.MimeTypeMap
import io.nichijou.tujian.R
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*

class TujianDocumentsProvider : DocumentsProvider() {
  private val baseDir by lazy { context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!! }

  override fun onCreate(): Boolean {
    if (!baseDir.exists()) {
      baseDir.mkdirs()
    }
    return true
  }

  override fun queryRoots(projection: Array<String>?): Cursor {
    val result = MatrixCursor(projection ?: DEFAULT_ROOT_PROJECTION)
    result.newRow().apply {
      add(Root.COLUMN_ROOT_ID, baseDir.absolutePath)
      add(Root.COLUMN_SUMMARY, context?.getString(R.string.provider_root_summary))
      add(Root.COLUMN_FLAGS, Root.FLAG_SUPPORTS_CREATE or Root.FLAG_SUPPORTS_RECENTS or Root.FLAG_SUPPORTS_SEARCH)
      add(Root.COLUMN_TITLE, context?.getString(R.string.app_name))
      add(Root.COLUMN_DOCUMENT_ID, getDocIdForFile(baseDir))
      add(Root.COLUMN_MIME_TYPES, getChildMimeTypes())
      add(Root.COLUMN_AVAILABLE_BYTES, baseDir.freeSpace)
      add(Root.COLUMN_ICON, R.mipmap.ic_launcher)
    }
    return result
  }

  override fun queryRecentDocuments(rootId: String, projection: Array<String>?): Cursor {
    val result = MatrixCursor(projection ?: DEFAULT_DOCUMENT_PROJECTION)
    val parent = getFileForDocId(rootId)
    val lastModifiedFiles = PriorityQueue(5, Comparator<File> { i, j -> i.lastModified().compareTo(j.lastModified()) })
    val pending = LinkedList<File>()
    pending.add(parent)
    while (!pending.isEmpty()) {
      val file = pending.removeFirst()
      if (file.isDirectory) {
        Collections.addAll(pending, *file.listFiles()!!)
      } else {
        lastModifiedFiles.add(file)
      }
    }
    for (i in 0 until (MAX_LAST_MODIFIED + 1).coerceAtMost(lastModifiedFiles.size)) {
      val file = lastModifiedFiles.remove()
      includeFile(result, null, file)
    }
    return result
  }

  override fun querySearchDocuments(rootId: String, query: String, projection: Array<String>?): Cursor {
    val result = MatrixCursor(projection ?: DEFAULT_DOCUMENT_PROJECTION)
    val parent = getFileForDocId(rootId)
    val pending = LinkedList<File>()
    pending.add(parent)
    while (!pending.isEmpty() && result.count < MAX_SEARCH_RESULTS) {
      val file = pending.removeFirst()
      if (file.isDirectory) {
        Collections.addAll(pending, *file.listFiles()!!)
      } else {
        if (file.name.toLowerCase(Locale.getDefault()).contains(query)) {
          includeFile(result, null, file)
        }
      }
    }
    return result
  }

  override fun openDocumentThumbnail(documentId: String, sizeHint: Point, signal: CancellationSignal): AssetFileDescriptor {
    val file = getFileForDocId(documentId)
    val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    return AssetFileDescriptor(pfd, 0, AssetFileDescriptor.UNKNOWN_LENGTH)
  }

  override fun queryDocument(documentId: String, projection: Array<String>?): Cursor {
    val result = MatrixCursor(projection ?: DEFAULT_DOCUMENT_PROJECTION)
    includeFile(result, documentId, null)
    return result
  }

  override fun queryChildDocuments(parentDocumentId: String, projection: Array<String>?, sortOrder: String): Cursor {
    val result = MatrixCursor(projection ?: DEFAULT_DOCUMENT_PROJECTION)
    val parent = getFileForDocId(parentDocumentId)
    for (file in parent.listFiles()!!) {
      includeFile(result, null, file)
    }
    return result
  }

  override fun openDocument(documentId: String, mode: String, signal: CancellationSignal?): ParcelFileDescriptor {
    val file = getFileForDocId(documentId)
    val accessMode = ParcelFileDescriptor.parseMode(mode)
    val isWrite = mode.indexOf('w') != -1
    return if (isWrite) {
      try {
        val handler = Handler(context!!.mainLooper)
        ParcelFileDescriptor.open(file, accessMode, handler) {
        }
      } catch (e: IOException) {
        throw FileNotFoundException("Failed to open document with id " + documentId +
          " and mode " + mode)
      }
    } else {
      ParcelFileDescriptor.open(file, accessMode)
    }
  }

  override fun createDocument(documentId: String, mimeType: String, displayName: String): String {
    val parent = getFileForDocId(documentId)
    val file = File(parent.path, displayName)
    try {
      file.createNewFile()
      file.setWritable(true)
      file.setReadable(true)
    } catch (e: IOException) {
      throw FileNotFoundException("Failed to create document with name " +
        displayName + " and documentId " + documentId)
    }

    return getDocIdForFile(file)
  }

  override fun deleteDocument(documentId: String) {
    val file = getFileForDocId(documentId)
    if (!file.delete()) {
      throw FileNotFoundException("Failed to delete document with id $documentId")
    }
  }

  override fun getDocumentType(documentId: String): String {
    val file = getFileForDocId(documentId)
    return getTypeForFile(file)
  }

  private fun getChildMimeTypes(): String {
    val mimeTypes = HashSet<String>()
    mimeTypes.add("image/*")
    mimeTypes.add("video/*")
    val mimeTypesString = StringBuilder()
    for (mimeType in mimeTypes) {
      mimeTypesString.append(mimeType).append("\n")
    }
    return mimeTypesString.toString()
  }

  private fun getDocIdForFile(file: File): String {
    var path = file.absolutePath
    val rootPath = baseDir.path
    path = when {
      rootPath == path -> ""
      rootPath.endsWith("/") -> path.substring(rootPath.length)
      else -> path.substring(rootPath.length + 1)
    }
    return "root:$path"
  }

  private fun includeFile(result: MatrixCursor, did: String?, f: File?) {
    var docId = did
    var file = f
    if (docId == null) {
      docId = getDocIdForFile(file!!)
    } else {
      file = getFileForDocId(docId)
    }
    var flags = 0

    if (file.isDirectory) {
      if (file.isDirectory && file.canWrite()) {
        flags = flags or Document.FLAG_DIR_SUPPORTS_CREATE
      }
    } else if (file.canWrite()) {
      flags = flags or Document.FLAG_SUPPORTS_WRITE
      flags = flags or Document.FLAG_SUPPORTS_DELETE
    }
    val mimeType = getTypeForFile(file)
    if (mimeType.startsWith("image/")) {
      flags = flags or Document.FLAG_SUPPORTS_THUMBNAIL
    }
    val row = result.newRow()
    row.add(Document.COLUMN_DOCUMENT_ID, docId)
    row.add(Document.COLUMN_DISPLAY_NAME, file.name)
    row.add(Document.COLUMN_SIZE, file.length())
    row.add(Document.COLUMN_MIME_TYPE, mimeType)
    row.add(Document.COLUMN_LAST_MODIFIED, file.lastModified())
    row.add(Document.COLUMN_FLAGS, flags)
    row.add(Document.COLUMN_ICON, R.mipmap.ic_launcher)
  }

  private fun getFileForDocId(docId: String): File {
    var target = baseDir
    if (docId == baseDir.absolutePath) {
      return target
    }
    val splitIndex = docId.indexOf(':', 1)
    if (splitIndex < 0) {
      throw FileNotFoundException("Missing root for $docId")
    } else {
      val path = docId.substring(splitIndex + 1)
      target = File(target, path)
      if (!target.exists()) {
        throw FileNotFoundException("Missing file for $docId at $target")
      }
      return target
    }
  }

  companion object {
    private val DEFAULT_ROOT_PROJECTION = arrayOf(Root.COLUMN_ROOT_ID, Root.COLUMN_MIME_TYPES, Root.COLUMN_FLAGS, Root.COLUMN_ICON, Root.COLUMN_TITLE, Root.COLUMN_SUMMARY, Root.COLUMN_DOCUMENT_ID, Root.COLUMN_AVAILABLE_BYTES)
    private val DEFAULT_DOCUMENT_PROJECTION = arrayOf(Document.COLUMN_DOCUMENT_ID, Document.COLUMN_MIME_TYPE, Document.COLUMN_DISPLAY_NAME, Document.COLUMN_LAST_MODIFIED, Document.COLUMN_FLAGS, Document.COLUMN_SIZE)
    private const val MAX_SEARCH_RESULTS = 20
    private const val MAX_LAST_MODIFIED = 5

    private fun getTypeForFile(file: File): String {
      return if (file.isDirectory) {
        Document.MIME_TYPE_DIR
      } else {
        getTypeForName(file.name)
      }
    }

    private fun getTypeForName(name: String): String {
      val lastDot = name.lastIndexOf('.')
      if (lastDot >= 0) {
        val extension = name.substring(lastDot + 1)
        val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        if (mime != null) {
          return mime
        }
      }
      return "application/octet-stream"
    }
  }
}
