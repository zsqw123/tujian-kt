package io.nichijou.tujian.common.ext

import java.io.*
import java.net.HttpURLConnection
import java.net.URL

/**
 * Created by tarek on 9/13/17.
 */
class SimpleUpload
/**
 * This constructor initializes a new HTTP POST request with content type
 * is set to multipart/form-data

 * @param url
 * *
 * @throws IOException
 */
@Throws(IOException::class)
constructor(url: URL) {

  companion object {
    private const val LINE_FEED = "\r\n"
    private const val MAX_BUFFER_SIZE = 1024 * 1024
    private const val CHARSET = "UTF-8"
  }

  // creates a unique boundary based on time stamp
  private val boundary: String = "===" + System.currentTimeMillis() + "==="
  private val httpConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
  private val outputStream: OutputStream
  private val writer: PrintWriter

  init {
    httpConnection.setRequestProperty("Accept", "application/json")
    httpConnection.setRequestProperty("Accept-Charset", "UTF-8")
    httpConnection.setRequestProperty("Connection", "Keep-Alive")
    httpConnection.setRequestProperty("Cache-Control", "no-cache")
    httpConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.80 Safari/537.36")
    httpConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
    httpConnection.setChunkedStreamingMode(MAX_BUFFER_SIZE)
    httpConnection.doInput = true
    httpConnection.doOutput = true    // indicates POST method
    httpConnection.useCaches = false
    outputStream = httpConnection.outputStream
    writer = PrintWriter(OutputStreamWriter(outputStream, CHARSET), true)
  }

  /**
   * Adds a form field to the request
   * @param name  field name
   * *
   * @param value field value
   */
  fun addFormField(name: String, value: String): SimpleUpload {
    writer.append("--").append(boundary).append(LINE_FEED)
    writer.append("Content-Disposition: form-data; name=\"").append(name).append("\"")
      .append(LINE_FEED)
    writer.append(LINE_FEED)
    writer.append(value).append(LINE_FEED)
    writer.flush()
    return this
  }

  /**
   * Adds a upload file section to the request
   * @param fieldName  - name attribute in <input type="file" name="..."></input>
   * *
   * @param uploadFile - a File to be uploaded
   * *
   * @throws IOException
   */
  @Throws(IOException::class)
  fun addFilePart(fieldName: String, inputStream: InputStream, fileName: String, fileType: String): SimpleUpload {
    writer.append("--").append(boundary).append(LINE_FEED)
    writer.append("Content-Disposition: file; name=\"").append(fieldName)
      .append("\"; filename=\"").append(fileName).append("\"").append(LINE_FEED)
    writer.append("Content-Type: ").append(fileType).append(LINE_FEED)
    writer.append(LINE_FEED)
    writer.flush()
    inputStream.copyTo(outputStream, MAX_BUFFER_SIZE)
    outputStream.flush()
    inputStream.close()
    writer.append(LINE_FEED)
    writer.flush()
    return this
  }

  /**
   * Adds a header field to the request.
   * @param name  - name of the header field
   * *
   * @param value - value of the header field
   */
  fun addHeaderField(name: String, value: String): SimpleUpload {
    writer.append("$name: $value").append(LINE_FEED)
    writer.flush()
    return this
  }

  /**
   * Upload the file and receive a response from the server.
   * @param onFileUploadedListener
   * *
   * @throws IOException
   */
  @Throws(IOException::class)
  fun upload(onFileUploadedListener: OnFileUploadedListener?) {
    writer.append(LINE_FEED).flush()
    writer.append("--").append(boundary).append("--")
      .append(LINE_FEED)
    writer.close()

    try {
      // checks server's status code first
      val status = httpConnection.responseCode
      if (status == HttpURLConnection.HTTP_OK) {
        val reader = BufferedReader(InputStreamReader(httpConnection
          .inputStream))
        val response = reader.use(BufferedReader::readText)
        httpConnection.disconnect()
        onFileUploadedListener?.onFileUploadingSuccess(response)
      } else {
        onFileUploadedListener?.onFileUploadingFailed(status)
      }

    } catch (e: IOException) {
      e.printStackTrace()
    }

  }

  interface OnFileUploadedListener {
    fun onFileUploadingSuccess(response: String)

    fun onFileUploadingFailed(responseCode: Int)
  }

}
