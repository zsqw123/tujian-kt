package io.nichijou.tujian.common.ext

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File

fun File.toURI(context: Context): Uri? {
  return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    FileProvider.getUriForFile(context, context.packageName + ".FileProvider", this)
  } else {
    Uri.fromFile(this)
  }
}
