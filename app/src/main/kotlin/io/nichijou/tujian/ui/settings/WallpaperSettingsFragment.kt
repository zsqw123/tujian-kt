package io.nichijou.tujian.ui.settings

import android.widget.*
import androidx.lifecycle.*
import io.nichijou.oops.ext.drawableRes
import io.nichijou.oops.ext.setPaddingTopPlusStatusBarHeight
import io.nichijou.tujian.R
import io.nichijou.tujian.base.*
import io.nichijou.tujian.common.ext.*
import io.nichijou.tujian.common.ext.setMarginTopPlusStatusBarHeight
import io.nichijou.tujian.ext.*
import io.nichijou.tujian.func.wallpaper.*
import kotlinx.android.synthetic.main.fragment_settings_wallpaper.*
import kotlinx.coroutines.*
import org.jetbrains.anko.support.v4.toast
import java.util.concurrent.*


class WallpaperSettingsFragment : BaseFragment(), CompoundButton.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener {
  companion object {
    private val MINUTES_15 = TimeUnit.MINUTES.toMillis(15)// 15分钟
    fun newInstance() = WallpaperSettingsFragment()
  }

  override fun getFragmentViewId(): Int = R.layout.fragment_settings_wallpaper

  override fun handleOnViewCreated() {
    setupDrawerWithToolbar(toolbar)
    top_bar.setMarginTopPlusStatusBarHeight()
    menu_wrapper.setPaddingTopPlusStatusBarHeight()
    initView()
  }

  private fun initView() {
    WallpaperConfig.asLiveData(WallpaperConfig::enable).observe(this, Observer {
      view_enable_wallpaper?.isChecked = it
      val drawableRes = target().drawableRes(if (it) R.drawable.ic_twotone_check_circle else R.drawable.ic_twotone_check_circle_outline)
      icon_enable_wallpaper?.setImageDrawable(drawableRes)
    })
    WallpaperConfig.asLiveData(WallpaperConfig::requiresBatteryNotLow).observe(this, Observer {
      view_requires_battery_not_low?.isChecked = it
      val drawableRes = if (it) R.drawable.ic_twotone_battery_90 else R.drawable.ic_twotone_battery_20
      icon_requires_battery_not_low?.setImageDrawable(target().drawableRes(drawableRes))
    })
    WallpaperConfig.asLiveData(WallpaperConfig::requiresCharging).observe(this, Observer {
      view_requires_charging?.isChecked = it
      val drawableRes = if (it) R.drawable.ic_twotone_battery_charging_50 else R.drawable.ic_twotone_battery_std
      icon_requires_charging?.setImageDrawable(target().drawableRes(drawableRes))
    })
    WallpaperConfig.asLiveData(WallpaperConfig::requiresDeviceIdle).observe(this, Observer {
      view_requires_device_idle?.isChecked = it
      val drawableRes = if (it) R.drawable.ic_twotone_mobile_friendly else R.drawable.ic_twotone_videogame_asset
      icon_requires_device_idle?.setImageDrawable(target().drawableRes(drawableRes))
    })
    WallpaperConfig.asLiveData(WallpaperConfig::requiresStorageNotLow).observe(this, Observer {
      view_requires_storage_not_low?.isChecked = it
      val drawableRes = if (it) R.drawable.ic_twotone_sd_storage else R.drawable.ic_twotone_disc_full
      icon_requires_storage_not_low?.setImageDrawable(target().drawableRes(drawableRes))
    })
    WallpaperConfig.asLiveData(WallpaperConfig::blur).observe(this, Observer {
      view_enable_blur?.isChecked = it
      val drawableRes = if (it) R.drawable.ic_twotone_blur_on else R.drawable.ic_twotone_blur_off
      icon_enable_blur?.setImageDrawable(target().drawableRes(drawableRes))
    })
    WallpaperConfig.asLiveData(WallpaperConfig::pixel).observe(this, Observer {
      view_enable_pixel?.isChecked = it
      val drawableRes = if (it) R.drawable.ic_twotone_gradient else R.drawable.ic_twotone_image
      icon_enable_pixel?.setImageDrawable(target().drawableRes(drawableRes))
    })
    WallpaperConfig.asLiveData(WallpaperConfig::notification).observe(this, Observer {
      view_enable_notification?.isChecked = it
      val drawableRes = if (it) R.drawable.ic_twotone_notifications_active else R.drawable.ic_twotone_notifications_off
      icon_enable_notification?.setImageDrawable(target().drawableRes(drawableRes))
    })
    view_enable_wallpaper.setOnCheckedChangeListener(this)
    view_requires_battery_not_low.setOnCheckedChangeListener(this)
    view_requires_charging.setOnCheckedChangeListener(this)
    view_requires_device_idle.setOnCheckedChangeListener(this)
    view_requires_storage_not_low.setOnCheckedChangeListener(this)
    view_enable_blur.setOnCheckedChangeListener(this)
    view_enable_pixel.setOnCheckedChangeListener(this)
    view_enable_notification.setOnCheckedChangeListener(this)
    lifecycleScope.launch(Dispatchers.IO) {
      val blurValue = WallpaperConfig.blurValue
      val pixelValue = WallpaperConfig.pixelValue
      val interval = (WallpaperConfig.interval - MINUTES_15).toInt()
      withContext(Dispatchers.Main) {
        0.animateTo(blurValue) {
          view_blur_value?.progress = it
        }
        0.animateTo(pixelValue) {
          view_pixel_value?.progress = it
        }
        0.animateTo(interval) {
          view_interval?.progress = it
        }
      }
    }
    view_blur_value.setOnSeekBarChangeListener(this)
    view_pixel_value.setOnSeekBarChangeListener(this)
    view_interval.setOnSeekBarChangeListener(this)
  }

  override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
    val id = buttonView?.id
    lifecycleScope.launch {
      when (id) {
        R.id.view_enable_wallpaper -> {
          if (WallpaperConfig.enable != isChecked) {
            WallpaperConfig.enable = isChecked
            if (isChecked) {
              WallpaperWorker.enqueueLoad()
            } else {
              WallpaperWorker.stopLoad()
            }
          }
        }
        R.id.view_requires_battery_not_low -> WallpaperConfig.requiresBatteryNotLow = isChecked
        R.id.view_requires_charging -> WallpaperConfig.requiresCharging = isChecked
        R.id.view_requires_device_idle -> WallpaperConfig.requiresDeviceIdle = isChecked
        R.id.view_requires_storage_not_low -> WallpaperConfig.requiresStorageNotLow = isChecked
        R.id.view_enable_blur -> WallpaperConfig.blur = isChecked
        R.id.view_enable_pixel -> WallpaperConfig.pixel = isChecked
        R.id.view_enable_notification -> WallpaperConfig.notification = isChecked
      }
    }
  }

  override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) = Unit
  override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

  override fun onStopTrackingTouch(seekBar: SeekBar?) {
    val id = seekBar?.id
    lifecycleScope.launch {
      when (id) {
        R.id.view_blur_value -> WallpaperConfig.blurValue = seekBar.progress
        R.id.view_pixel_value -> WallpaperConfig.pixelValue = seekBar.progress
        R.id.view_interval -> {
          val value = seekBar.progress + MINUTES_15
          WallpaperConfig.interval = value
          toast(value.toDateStr())
        }
      }
    }
  }
}
