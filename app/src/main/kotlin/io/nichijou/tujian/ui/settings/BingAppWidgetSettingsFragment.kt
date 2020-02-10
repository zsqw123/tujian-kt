package io.nichijou.tujian.ui.settings

import android.widget.*
import androidx.lifecycle.*
import io.nichijou.oops.ext.*
import io.nichijou.tujian.R
import io.nichijou.tujian.base.*
import io.nichijou.tujian.common.ext.*
import io.nichijou.tujian.ext.*
import io.nichijou.tujian.func.appwidget.*
import kotlinx.android.synthetic.main.fragment_settings_appwidget_bing.*
import kotlinx.coroutines.*


class BingAppWidgetSettingsFragment : BaseFragment(), CompoundButton.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener {
  companion object {
    fun newInstance() = BingAppWidgetSettingsFragment()
  }

  override fun getFragmentViewId(): Int = R.layout.fragment_settings_appwidget_bing

  override fun handleOnViewCreated() {
    menu_wrapper.setPaddingTopPlusStatusBarHeight()
    BingAppWidgetConfig.asLiveData(BingAppWidgetConfig::requiresBatteryNotLow).observe(this, Observer {
      view_requires_battery_not_low?.isChecked = it
      val drawableRes = if (it) R.drawable.ic_twotone_battery_90 else R.drawable.ic_twotone_battery_20
      icon_requires_battery_not_low?.setImageDrawable(target().drawableRes(drawableRes))
    })
    BingAppWidgetConfig.asLiveData(BingAppWidgetConfig::requiresCharging).observe(this, Observer {
      view_requires_charging?.isChecked = it
      val drawableRes = if (it) R.drawable.ic_twotone_battery_charging_50 else R.drawable.ic_twotone_battery_std
      icon_requires_charging?.setImageDrawable(target().drawableRes(drawableRes))
    })
    BingAppWidgetConfig.asLiveData(BingAppWidgetConfig::requiresDeviceIdle).observe(this, Observer {
      view_requires_device_idle?.isChecked = it
      val drawableRes = if (it) R.drawable.ic_twotone_mobile_friendly else R.drawable.ic_twotone_videogame_asset
      icon_requires_device_idle?.setImageDrawable(target().drawableRes(drawableRes))
    })
    BingAppWidgetConfig.asLiveData(BingAppWidgetConfig::requiresStorageNotLow).observe(this, Observer {
      view_requires_storage_not_low?.isChecked = it
      val drawableRes = if (it) R.drawable.ic_twotone_sd_storage else R.drawable.ic_twotone_disc_full
      icon_requires_storage_not_low?.setImageDrawable(target().drawableRes(drawableRes))
    })
    BingAppWidgetConfig.asLiveData(BingAppWidgetConfig::blur).observe(this, Observer {
      view_enable_blur?.isChecked = it
      val drawableRes = if (it) R.drawable.ic_twotone_blur_on else R.drawable.ic_twotone_blur_off
      icon_enable_blur?.setImageDrawable(target().drawableRes(drawableRes))
    })
    BingAppWidgetConfig.asLiveData(BingAppWidgetConfig::pixel).observe(this, Observer {
      view_enable_pixel?.isChecked = it
      val drawableRes = if (it) R.drawable.ic_twotone_gradient else R.drawable.ic_twotone_image
      icon_enable_pixel?.setImageDrawable(target().drawableRes(drawableRes))
    })
    BingAppWidgetConfig.asLiveData(BingAppWidgetConfig::notification).observe(this, Observer {
      view_enable_notification?.isChecked = it
      val drawableRes = if (it) R.drawable.ic_twotone_notifications_active else R.drawable.ic_twotone_notifications_off
      icon_enable_notification?.setImageDrawable(target().drawableRes(drawableRes))
    })
    TujianAppWidgetConfig.asLiveData(TujianAppWidgetConfig::autoTextColor).observe(this, Observer {
      view_auto_text_color?.isChecked = it
      val drawableRes = if (it) R.drawable.ic_twotone_invert_colors else R.drawable.ic_twotone_invert_colors_off
      icon_auto_text_color?.setImageDrawable(target().drawableRes(drawableRes))
    })
    view_requires_battery_not_low.setOnCheckedChangeListener(this)
    view_requires_charging.setOnCheckedChangeListener(this)
    view_requires_device_idle.setOnCheckedChangeListener(this)
    view_requires_storage_not_low.setOnCheckedChangeListener(this)
    view_enable_blur.setOnCheckedChangeListener(this)
    view_enable_pixel.setOnCheckedChangeListener(this)
    view_enable_notification.setOnCheckedChangeListener(this)
    view_auto_text_color.setOnCheckedChangeListener(this)
    lifecycleScope.launch(Dispatchers.IO) {
      val blurValue = BingAppWidgetConfig.blurValue
      val pixelValue = BingAppWidgetConfig.pixelValue
      val textSize = BingAppWidgetConfig.textSize
      val textLines = BingAppWidgetConfig.textLines
      withContext(Dispatchers.Main) {
        0.animateTo(blurValue) {
          view_blur_value?.progress = it
        }
        0.animateTo(pixelValue) {
          view_pixel_value?.progress = it
        }
        0.animateTo(textSize) {
          view_text_size?.progress = it
        }
        0.animateTo(textLines) {
          view_text_lines?.progress = it
        }
      }
    }
    view_blur_value.setOnSeekBarChangeListener(this)
    view_pixel_value.setOnSeekBarChangeListener(this)
    view_text_size.setOnSeekBarChangeListener(this)
    view_text_lines.setOnSeekBarChangeListener(this)
  }

  override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
    val id = buttonView?.id
    lifecycleScope.launch {
      when (id) {
        R.id.view_requires_battery_not_low -> BingAppWidgetConfig.requiresBatteryNotLow = isChecked
        R.id.view_requires_charging -> BingAppWidgetConfig.requiresCharging = isChecked
        R.id.view_requires_device_idle -> BingAppWidgetConfig.requiresDeviceIdle = isChecked
        R.id.view_requires_storage_not_low -> BingAppWidgetConfig.requiresStorageNotLow = isChecked
        R.id.view_enable_blur -> BingAppWidgetConfig.blur = isChecked
        R.id.view_enable_pixel -> BingAppWidgetConfig.pixel = isChecked
        R.id.view_enable_notification -> BingAppWidgetConfig.notification = isChecked
        R.id.view_auto_text_color -> BingAppWidgetConfig.autoTextColor = isChecked
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
        R.id.view_blur_value -> {
          BingAppWidgetConfig.blurValue = progress
          toast(getString(R.string.blur_value_format).format(progress / 100))
        }
        R.id.view_pixel_value -> {
          BingAppWidgetConfig.pixelValue = progress
          toast(getString(R.string.pixel_value_format).format(progress / 100))
        }
        R.id.view_text_size -> {
          BingAppWidgetConfig.textSize = progress
          toast(getString(R.string.text_size_format).format(progress / 100 + 12))
        }
        R.id.view_text_lines -> {
          BingAppWidgetConfig.textLines = progress
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
