package io.nichijou.tujian.ui.settings

import android.widget.CompoundButton
import android.widget.SeekBar
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import io.nichijou.oops.ext.drawableRes
import io.nichijou.oops.ext.setPaddingTopPlusStatusBarHeight
import io.nichijou.tujian.R
import io.nichijou.tujian.base.BaseFragment
import io.nichijou.tujian.common.ext.animateTo
import io.nichijou.tujian.common.ext.asLiveData
import io.nichijou.tujian.common.ext.toDateStr
import io.nichijou.tujian.ext.target
import io.nichijou.tujian.common.appwidget.HitokotoAppWidgetConfig
import kotlinx.android.synthetic.main.fragment_settings_appwidget_hitokoto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.support.v4.toast
import java.util.concurrent.TimeUnit

class HitokotoAppWidgetSettingsFragment : BaseFragment(), CompoundButton.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener {
  companion object {
    private val MINUTES_15 = TimeUnit.MINUTES.toMillis(15)// 15分钟
    fun newInstance() = HitokotoAppWidgetSettingsFragment()
  }

  override fun getFragmentViewId(): Int = R.layout.fragment_settings_appwidget_hitokoto

  override fun handleOnViewCreated() {
    menu_wrapper.setPaddingTopPlusStatusBarHeight()
    HitokotoAppWidgetConfig.asLiveData(HitokotoAppWidgetConfig::requiresBatteryNotLow).observe(this, Observer {
      view_requires_battery_not_low?.isChecked = it
      val drawableRes = if (it) R.drawable.ic_twotone_battery_90 else R.drawable.ic_twotone_battery_20
      icon_requires_battery_not_low?.setImageDrawable(target().drawableRes(drawableRes))
    })
    HitokotoAppWidgetConfig.asLiveData(HitokotoAppWidgetConfig::requiresCharging).observe(this, Observer {
      view_requires_charging?.isChecked = it
      val drawableRes = if (it) R.drawable.ic_twotone_battery_charging_50 else R.drawable.ic_twotone_battery_std
      icon_requires_charging?.setImageDrawable(target().drawableRes(drawableRes))
    })
    HitokotoAppWidgetConfig.asLiveData(HitokotoAppWidgetConfig::requiresDeviceIdle).observe(this, Observer {
      view_requires_device_idle?.isChecked = it
      val drawableRes = if (it) R.drawable.ic_twotone_mobile_friendly else R.drawable.ic_twotone_videogame_asset
      icon_requires_device_idle?.setImageDrawable(target().drawableRes(drawableRes))
    })
    HitokotoAppWidgetConfig.asLiveData(HitokotoAppWidgetConfig::notification).observe(this, Observer {
      view_enable_notification?.isChecked = it
      val drawableRes = if (it) R.drawable.ic_twotone_notifications_active else R.drawable.ic_twotone_notifications_off
      icon_enable_notification?.setImageDrawable(target().drawableRes(drawableRes))
    })
    HitokotoAppWidgetConfig.asLiveData(HitokotoAppWidgetConfig::autoTextColor).observe(this, Observer {
      view_auto_text_color?.isChecked = it
      val drawableRes = if (it) R.drawable.ic_twotone_invert_colors else R.drawable.ic_twotone_invert_colors_off
      icon_auto_text_color?.setImageDrawable(target().drawableRes(drawableRes))
    })
    view_auto_text_color.setOnCheckedChangeListener(this)
    view_requires_battery_not_low.setOnCheckedChangeListener(this)
    view_requires_charging.setOnCheckedChangeListener(this)
    view_requires_device_idle.setOnCheckedChangeListener(this)
    view_enable_notification.setOnCheckedChangeListener(this)
    lifecycleScope.launch(Dispatchers.IO) {
      val interval = (HitokotoAppWidgetConfig.interval - MINUTES_15).toInt()
      val hitokotoTextSize = HitokotoAppWidgetConfig.hitokotoTextSize
      val sourceTextSize = HitokotoAppWidgetConfig.sourceTextSize
      val lines = HitokotoAppWidgetConfig.hitokotoLines
      withContext(Dispatchers.Main) {
        0.animateTo(interval) {
          view_interval?.progress = it
        }
        0.animateTo(hitokotoTextSize) {
          view_hitokoto_text_size?.progress = it
        }
        0.animateTo(lines) {
          view_hitokoto_lines?.progress = it
        }
        0.animateTo(sourceTextSize) {
          view_source_text_size?.progress = it
        }
      }
    }
    view_interval.setOnSeekBarChangeListener(this)
    view_hitokoto_text_size.setOnSeekBarChangeListener(this)
    view_source_text_size.setOnSeekBarChangeListener(this)
    view_hitokoto_lines.setOnSeekBarChangeListener(this)
  }

  override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
    val id = buttonView?.id
    lifecycleScope.launch {
      when (id) {
        R.id.view_requires_battery_not_low -> HitokotoAppWidgetConfig.requiresBatteryNotLow = isChecked
        R.id.view_requires_charging -> HitokotoAppWidgetConfig.requiresCharging = isChecked
        R.id.view_requires_device_idle -> HitokotoAppWidgetConfig.requiresDeviceIdle = isChecked
        R.id.view_enable_notification -> HitokotoAppWidgetConfig.notification = isChecked
        R.id.view_auto_text_color -> HitokotoAppWidgetConfig.autoTextColor = isChecked
      }
    }
  }

  override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) = Unit
  override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

  override fun onStopTrackingTouch(seekBar: SeekBar) {
    val id = seekBar.id
    val progress = seekBar.progress
    lifecycleScope.launch {
      when (id) {
        R.id.view_interval -> {
          val value = progress + MINUTES_15
          HitokotoAppWidgetConfig.interval = value
          toast(value.toDateStr())
        }
        R.id.view_hitokoto_text_size -> {
          HitokotoAppWidgetConfig.hitokotoTextSize = progress
          toast(getString(R.string.text_size_format).format(progress / 100 + 12))
        }
        R.id.view_source_text_size -> {
          HitokotoAppWidgetConfig.sourceTextSize = progress
          toast(getString(R.string.text_size_format).format(progress / 100 + 12))
        }
        R.id.view_hitokoto_lines -> {
          HitokotoAppWidgetConfig.hitokotoLines = progress
          val value = when {
            progress == 0 -> -1
            progress < 100 -> 1
            else -> progress / 100
          }
          toast(if (value == -1) getString(R.string.no_limit) else getString(R.string.lines_format).format(value))
        }
      }
    }
  }
}
