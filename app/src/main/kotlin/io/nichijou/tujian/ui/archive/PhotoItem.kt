package io.nichijou.tujian.ui.archive

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.zzhoujay.richtext.RichText
import io.nichijou.tujian.R
import io.nichijou.tujian.common.entity.Picture
import io.nichijou.tujian.common.entity.setWallpaper
import io.nichijou.tujian.common.ext.ViewHolder
import io.nichijou.tujian.common.ext.makeGone
import io.nichijou.tujian.common.ext.makeVisible
import io.nichijou.tujian.common.ext.shareString
import io.nichijou.tujian.isDark
import kotlinx.android.synthetic.main.photo_item_layout.view.*
import kotlinx.android.synthetic.main.photo_item_viewpager_layout.*
import kotlinx.android.synthetic.main.photo_item_viewpager_layout.view.*
import org.jetbrains.anko.isSelectable

class Viewpager2Adapter(private val data: ArrayList<Picture>, val parentView: View) :
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
    val pic1080: String = getNewUrl(items[position]) + "!w1080"

    Glide.with(item.context).load(pic1080).listener(object : RequestListener<Drawable> {
      override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean) = false
      override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
        val view = parentView.photo_item_progress
        view?.makeGone()
        return false
      }
    }).into(photoView)

    photoView.setOnLongClickListener {
      toolDialog(item.context, items[position])
      return@setOnLongClickListener true
    }
  }

  override fun getItemCount() = data.size
}

class PhotoItem(val list: ArrayList<Picture>, private val nowPos: Int) : DialogFragment() {
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.photo_item_viewpager_layout, container, false)
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    dialog!!.window!!.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
    dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
    super.onActivityCreated(savedInstanceState)
    dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.BLACK))
    dialog!!.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    photo_item_viewpager_layout.background = ColorDrawable(Color.TRANSPARENT)
    photo_item_viewpager.adapter = Viewpager2Adapter(list, view)
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
//    photo_item_viewpager.layoutParams = FrameLayout.LayoutParams(matchParent, wrapContent)
    photo_item_viewpager.currentItem = nowPos
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
