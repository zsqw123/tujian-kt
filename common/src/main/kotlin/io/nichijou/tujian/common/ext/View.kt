package io.nichijou.tujian.common.ext

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Environment.DIRECTORY_PICTURES
import android.provider.MediaStore
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ListAdapter
import androidx.core.view.setMargins
import com.bm.library.PhotoView
import com.google.android.material.snackbar.Snackbar
import io.nichijou.tujian.common.R
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import java.io.*
import kotlin.math.hypot
import kotlin.math.max

fun View.setMargin(size: Int) {
  val layoutParams = this.layoutParams
  if (layoutParams is ViewGroup.MarginLayoutParams) {
    layoutParams.setMargins(size)
    this.layoutParams = layoutParams
  }
}

fun View.makeGone() {
  this.visibility = View.GONE
}

fun View.makeVisible() {
  this.visibility = View.VISIBLE
}

inline fun <T : View> T.postApply(crossinline block: T.() -> Unit) {
  post { apply(block) }
}

fun View.setMarginTopPlusStatusBarHeight() {
  if (this.layoutParams is ViewGroup.MarginLayoutParams) {
    val lp = this.layoutParams as ViewGroup.MarginLayoutParams
    lp.topMargin = context.getStatusBarHeight() + lp.topMargin
  }
}

fun View.setMarginBottomPlusNavBarHeight() {
  if (this.layoutParams is ViewGroup.MarginLayoutParams) {
    val lp = this.layoutParams as ViewGroup.MarginLayoutParams
    lp.bottomMargin = context.getNavigationBarHeight() + lp.bottomMargin
  }
}

fun View.setPaddingBottomPlusNavBarHeight() {
  setPadding(this.paddingLeft, this.paddingTop, this.paddingRight, this.paddingBottom + context.getNavigationBarHeight())
}


fun View.sbl(msg: String, action: String? = null, done: (() -> Unit)? = null, dismissed: ((Int) -> Unit)? = null) {
  sb(msg, Snackbar.LENGTH_LONG, action, done, dismissed)
}

fun View.sbs(msg: String, action: String? = null, done: (() -> Unit)? = null, dismissed: ((Int) -> Unit)? = null) {
  sb(msg, Snackbar.LENGTH_SHORT, action, done, dismissed)
}

fun View.sb(msg: String, duration: Int, action: String? = null, done: (() -> Unit)? = null, dismissed: ((Int) -> Unit)? = null) {
  val sb = Snackbar.make(this, msg, duration)
  sb.addCallback(object : Snackbar.Callback() {
    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
      dismissed?.invoke(event)
    }
  })
  if (action != null && done != null) {
    sb.setAction(action) {
      done()
    }
  }
  sb.show()
}

inline fun <T : View> T.doPost(crossinline block: T.() -> Unit) {
  this.post {
    block()
  }
}

fun ListAdapter.measureContentWidth(context: Context): Int {
  var mMeasureParent: ViewGroup? = null
  var maxWidth = 0
  var itemView: View? = null
  var itemType = 0
  val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
  val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
  val count = this.count
  for (i in 0 until count) {
    val positionType = this.getItemViewType(i)
    if (positionType != itemType) {
      itemType = positionType
      itemView = null
    }
    if (mMeasureParent == null) {
      mMeasureParent = FrameLayout(context)
    }
    itemView = this.getView(i, itemView, mMeasureParent)
    itemView!!.measure(widthMeasureSpec, heightMeasureSpec)
    val itemWidth = itemView.measuredWidth
    if (itemWidth > maxWidth) {
      maxWidth = itemWidth
    }
  }
  return maxWidth
}

fun View.getCenterPointOnScreen(): Point {
  val loc = IntArray(2)
  this.getLocationOnScreen(loc)
  val point = Point()
  point.x = loc[0] + measuredWidth / 2
  point.y = loc[1] + measuredHeight / 2
  return point
}

fun Context.getRadiusByCenterPoint(point: Point): Float {
  val sw = getScreenWidth().toDouble()
  val sh = getScreenHeight().toDouble()
  val x = point.x.toDouble()
  val y = point.y.toDouble()
  val r1 = hypot(x, y)
  val r2 = hypot(sw - x, y)
  val r3 = hypot(sw - x, sh - y)
  val r4 = hypot(x, sh - y)
  return max(max(r1, r2), max(r3, r4)).toFloat()
}

// 保存bitmap
fun Bitmap.saveToAlbum(context: Context, fileName: String) {
  val bitmap: Bitmap = this
  context.doAsync {
    bitmap.addToTujianProvider(context, fileName)
    @Suppress("DEPRECATION")
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
      //系统相册目录
      val galleryPath = File(Environment.getExternalStorageDirectory().absolutePath + File.separator +
        DIRECTORY_PICTURES + File.separator + "图鉴日图")
      var file: File? = null
      var fos: FileOutputStream? = null
      try {
        file = File(galleryPath, "$fileName.jpg")
        if (!file.exists()) {
          file.parentFile!!.mkdirs()
          file.createNewFile()
        }
        fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
      } catch (e: Exception) {
        e.printStackTrace()
      } finally {
        fos?.close()
      }
      val values = ContentValues().apply {
        put(MediaStore.Images.Media.DATA, file!!.absolutePath)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.DESCRIPTION, "保存自: 图鉴日图")
      }
      val uri: Uri? = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
      val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
      intent.data = uri
      context.sendBroadcast(intent)
      uiThread { context.toast("图片保存成功") }
    } else { //Android Q把文件插入到系统图库
      val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.TITLE, fileName)
        put(MediaStore.Images.Media.DISPLAY_NAME, "$fileName.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.IS_PENDING, 1)
        put(MediaStore.Images.Media.RELATIVE_PATH, "$DIRECTORY_PICTURES/图鉴日图")
      }

      val resolver = context.contentResolver
      val collection = MediaStore.Images.Media
        .getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
      val item = resolver.insert(collection, contentValues)

      resolver.openFileDescriptor(item!!, "w", null).use { pfd ->
        val out = FileOutputStream(pfd!!.fileDescriptor)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
      }

      contentValues.clear()
      contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
      resolver.update(item, contentValues, null, null)
      uiThread { context.toast("图片保存成功") }
    }
  }
}

// 添加图片到tujianProviders
fun Bitmap.addToTujianProvider(context: Context, fileName: String, folderName: String = "") {
  val bitmap: Bitmap = this
  context.doAsync {
    val tujianPicProviderRoot = context.getExternalFilesDir(DIRECTORY_PICTURES)
    val picFile =if (folderName != "") File(tujianPicProviderRoot, "$fileName.jpg")
    else File(tujianPicProviderRoot, "$fileName.jpg")
    if (!picFile.exists()) {
      picFile.parentFile!!.mkdirs()
      picFile.createNewFile()
    }
    val fos = FileOutputStream(picFile)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
  }
}

// 宽高比imageView
class AspectRatioImageView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : PhotoView(context, attrs, defStyleAttr) {
  var ratio: Float = DEFAULT_RATIO

  init {
    attrs?.let {
      context.obtainStyledAttributes(it, R.styleable.AspectRatioImageView).apply {
        ratio = getFloat(R.styleable.AspectRatioImageView_aspect_ratio, DEFAULT_RATIO)
        recycle()
      }
    }
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    var width = measuredWidth
    var height = measuredHeight
    when {
      width > 0 -> height = (width * ratio).toInt()
      height > 0 -> width = (height / ratio).toInt()
      else -> return
    }
    setMeasuredDimension(width, height)
  }

  companion object {
    const val DEFAULT_RATIO = 1F
  }
}
