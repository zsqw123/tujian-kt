package io.nichijou.tujian.widget

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.util.AttributeSet
import androidx.lifecycle.Observer
import androidx.palette.graphics.Palette
import com.facebook.drawee.drawable.ProgressBarDrawable
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import io.nichijou.oops.ext.activity
import io.nichijou.oops.widget.MaterialCardView
import io.nichijou.tujian.StyleViewModel
import io.nichijou.tujian.common.entity.Bing
import io.nichijou.tujian.common.entity.Picture
import io.nichijou.tujian.common.ext.*
import io.nichijou.tujian.common.fresco.getPaletteSwatches
import io.nichijou.tujian.common.fresco.load
import io.nichijou.tujian.ui.ColorAdapter
import io.nichijou.utils.randomColor
import jp.wasabeef.recyclerview.animators.LandingAnimator
import kotlinx.android.synthetic.main.item_history_bing.view.*
import kotlinx.android.synthetic.main.item_history_bing.view.colors
import kotlinx.android.synthetic.main.item_history_bing.view.desc
import kotlinx.android.synthetic.main.item_history_picture.view.*
import java.util.WeakHashMap
import kotlin.collections.List

class Card(context: Context, attrs: AttributeSet) : MaterialCardView(context, attrs) {

  override fun liveInOops() {
    super.liveInOops()
    val activity = this.activity()
    val live = StyleViewModel.live(activity)
    live.cardRadius.observe(this, Observer {
      radius = (it / 100).toFloat()
    })
    live.cardElevation.observe(this, Observer {
      cardElevation = (it / 100).toFloat()
    })
    live.cardSpace.observe(this, Observer {
      setMargin(context.dp2px(it / 100f).toInt())
    })
  }

  fun setDataBing(item: Bing, cacheColors: WeakHashMap<String, List<Palette.Swatch>>) {
    val cColors = cacheColors[item.url]
    if (cColors.isNullOrEmpty()) {
      colors.makeGone()
      if (cColors == null) {
        item.url.getPaletteSwatches { s, c ->
          cacheColors[s] = c
        }
      }
    } else {
      bindColors(cColors)
    }

    desc.text = item.copyright
    date.text = item.date
    if (drawee != null) {
      drawee.aspectRatio = 1920f / 1080f
      drawee.load(item.url, progressDrawable = ProgressBarDrawable().apply {
        barWidth = context.dp2px(8f).toInt()
        setPadding(0)
        color = randomColor()
      })
      item.download(context)
    }
  }

  fun setDataPicture(item: Picture, cacheColors: WeakHashMap<String, List<Palette.Swatch>>) {
    val cColors = cacheColors[item.local]
    if (cColors.isNullOrEmpty()) {
      colors.makeGone()
      if (cColors == null) {
        item.local.getPaletteSwatches { s, c ->
          bindColors(c)
          cacheColors[s] = c
        }
      }
    } else {
      bindColors(cColors)
    }
    title.text = item.title
    desc.text = create(item)
    if (drawee != null) {
      drawee.aspectRatio = item.width.toFloat() / item.height.toFloat()
      drawee.load(item.local, progressDrawable = ProgressBarDrawable().apply {
        barWidth = context.dp2px(8f).toInt()
        setPadding(0)
        color = randomColor()
      })
      item.download(context)
    }
  }

  private fun bindColors(cColors: List<Palette.Swatch>) {
    colors.apply {
      makeVisible()
      itemAnimator = LandingAnimator()
      layoutManager = FlexboxLayoutManager(context, FlexDirection.ROW, FlexWrap.NOWRAP)
      adapter = ColorAdapter(cColors, -1, context.dp2px(8f).toInt())
    }
  }

  private fun create(picture: Picture): CharSequence {
    val desc = picture.desc
    val user = "\nvia ${picture.user}"
    val date = "\n${picture.date}"
    val sb = SpannableStringBuilder(desc + user + date)
    sb.setSpan(RelativeSizeSpan(0.9f), desc.length, desc.length + user.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    sb.setSpan(RelativeSizeSpan(0.8f), desc.length + user.length, desc.length + user.length + date.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    return sb
  }
}
