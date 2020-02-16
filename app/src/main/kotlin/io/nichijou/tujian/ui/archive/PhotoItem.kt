package io.nichijou.tujian.ui.archive

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.bm.library.PhotoView
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.zzhoujay.richtext.RichText
import io.nichijou.tujian.App
import io.nichijou.tujian.R
import io.nichijou.tujian.common.entity.Picture
import io.nichijou.tujian.common.ext.ViewHolder
import kotlinx.android.synthetic.main.photo_item_layout.view.*
import kotlinx.android.synthetic.main.photo_item_viewpager_layout.*
import me.jessyan.progressmanager.ProgressListener
import me.jessyan.progressmanager.ProgressManager
import me.jessyan.progressmanager.body.ProgressInfo
import org.jetbrains.anko.toast
import java.io.InputStream


class Viewpager2Adapter(private val data: ArrayList<Picture>) :
  RecyclerView.Adapter<RecyclerView.ViewHolder>() {
  private var items: ArrayList<Picture> = data
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    val view: View = LayoutInflater.from(parent.context).inflate(R.layout.photo_item_layout, parent, false)
    return ViewHolder<RecyclerView>(view, viewType)
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = holder.itemView
    val photoView = item.photo_item
    photoView.enable()
    photoView.scaleType = ImageView.ScaleType.CENTER_CROP
    photoView.setOnClickListener {
      PhotoView(it.context)
    }
    item.photo_item_desc.typeface = Typeface.DEFAULT_BOLD
    RichText.fromMarkdown(items[position].desc.replace("\n", "  \n")).into(item.photo_item_desc)
    Glide.with(item.context).load(getNewUrl(items[position])).into(photoView)

    ProgressManager.getInstance().addResponseListener(getNewUrl(items[position]), object : ProgressListener {
      override fun onProgress(progressInfo: ProgressInfo?) {
        val progress = progressInfo?.percent
        item.photo_item_progress.progress = progress ?: 0
      }

      override fun onError(id: Long, e: java.lang.Exception?) {
        item.context.toast("加载图片失败")
      }

    })
  }

  override fun getItemCount() = data.size

  fun add(newData: ArrayList<Picture>, reset: Boolean = false) {
    if (reset) {
      reset(newData)
    } else {
      val size = data.size
      data.addAll(newData)
      notifyItemRangeInserted(size, newData.size)
    }
  }

  private fun reset(newData: ArrayList<Picture>) {
    data.clear()
    data.addAll(newData)
    notifyDataSetChanged()
  }
}

class PhotoItem(val list: ArrayList<Picture>, private val nowPos: Int) : DialogFragment() {
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.photo_item_viewpager_layout, container, false)
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    dialog!!.window!!.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
    dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
    super.onActivityCreated(savedInstanceState)
    dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog!!.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    photo_item_viewpager_layout.background = ColorDrawable(Color.TRANSPARENT)
    photo_item_viewpager.adapter = Viewpager2Adapter(list)
//    photo_item_viewpager.layoutParams = FrameLayout.LayoutParams(matchParent, wrapContent)
    photo_item_viewpager.currentItem = nowPos
  }
}
