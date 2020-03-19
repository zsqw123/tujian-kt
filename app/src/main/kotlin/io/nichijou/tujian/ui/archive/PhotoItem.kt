package io.nichijou.tujian.ui.archive

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.zzhoujay.richtext.RichText
import io.nichijou.tujian.R
import io.nichijou.tujian.common.entity.Picture
import io.nichijou.tujian.common.entity.setWallpaper
import io.nichijou.tujian.common.ext.ViewHolder
import io.nichijou.tujian.common.ext.makeGone
import io.nichijou.tujian.common.ext.makeVisible
import io.nichijou.tujian.common.ext.shareString
import io.nichijou.tujian.ext.target
import io.nichijou.tujian.isDark
import kotlinx.android.synthetic.main.photo_item_layout.view.*
import kotlinx.android.synthetic.main.photo_item_viewpager_layout.*
import kotlinx.android.synthetic.main.photo_item_viewpager_layout.view.*
import me.yokeyword.fragmentation.SupportFragment
import org.jetbrains.anko.isSelectable

class Viewpager2Adapter(private val data: ArrayList<Picture>, private val parentView: View, private val photoItem: PhotoItem) :
  RecyclerView.Adapter<RecyclerView.ViewHolder>() {
  private var items: ArrayList<Picture> = data
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    val view: View = LayoutInflater.from(parent.context).inflate(R.layout.photo_item_layout, parent, false)
    return ViewHolder<RecyclerView>(view, viewType)
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = holder.itemView
    val photoView = item.photo_item
    photoView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP)
    val pic1080: String = getNewUrl(items[position], 1080)!!
    val progress = parentView.photo_item_progress
    if (photoView.hasImage()) progress.makeGone()
    Glide.with(item.context).asBitmap().load(pic1080).into(object : CustomTarget<Bitmap>() {
      override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
        progress?.makeGone()
        photoView.setImage(ImageSource.bitmap(resource))
      }

      override fun onLoadCleared(placeholder: Drawable?) {}
    })

    photoView.setOnClickListener {
      photoItem.onBackPressedSupport()
    }
    photoView.setOnLongClickListener {
      toolDialog(item.context, items[position])
      return@setOnLongClickListener true
    }
  }

  override fun getItemCount() = data.size
}

class PhotoItem : SupportFragment() {

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    target().window!!.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
    return inflater.inflate(R.layout.photo_item_viewpager_layout, container, false)
  }

  override fun onBackPressedSupport(): Boolean {
    pop()
    target().window!!.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    return true
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val list = requireArguments().getParcelableArrayList<Picture>("list")
    val nowPos = requireArguments().getInt("pos")
    photo_item_viewpager_layout.background = ColorDrawable(Color.TRANSPARENT)
    photo_item_viewpager.adapter = Viewpager2Adapter(list!!, view, this)
    photo_item_viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
      override fun onPageSelected(position: Int) {
        super.onPageSelected(position)
        photo_item_progress.makeVisible()
        photo_item_desc.isSelectable = false
        RichText.fromMarkdown(list[position].desc.replace("\n", "  \n")).linkFix { linkHolder ->
          linkHolder!!.color = if (isDark()) Color.parseColor("#22EB4F") else Color.parseColor("#DD14B0")
          linkHolder.isUnderLine = false
        }.into(photo_item_desc)
      }
    })
    photo_item_viewpager.setCurrentItem(nowPos, false)
  }

  companion object {
    fun newInstance(list: ArrayList<Picture>, nowPos: Int) = PhotoItem().apply {
      arguments = bundleOf("list" to list, "pos" to nowPos)
    }
  }
}

fun toolDialog(context: Context, picture: Picture): MaterialDialog {
  return MaterialDialog(context).title(text = "图鉴日图").icon(R.mipmap.ic_launcher).show {
    listItems(items = listOf("保存原图", "设置壁纸", "分享")) { _, index, _ ->
      when (index) {
        0 -> picture.download(context)
        1 -> setWallpaper(context, picture)
        2 -> context.shareString(picture.share())
      }
    }
  }.cornerRadius(12f)
}
