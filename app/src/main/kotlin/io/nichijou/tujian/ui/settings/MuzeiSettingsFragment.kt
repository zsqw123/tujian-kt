package io.nichijou.tujian.ui.settings

import android.widget.CompoundButton
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import io.nichijou.oops.ext.drawableRes
import io.nichijou.oops.ext.setPaddingTopPlusStatusBarHeight
import io.nichijou.tujian.R
import io.nichijou.tujian.base.BaseFragment
import io.nichijou.tujian.common.ext.asLiveData
import io.nichijou.tujian.common.ext.setMarginTopPlusStatusBarHeight
import io.nichijou.tujian.ext.target
import io.nichijou.tujian.common.muzei.MuzeiConfig
import kotlinx.android.synthetic.main.fragment_settings_muzei.*
import kotlinx.coroutines.launch

class MuzeiSettingsFragment : BaseFragment(), CompoundButton.OnCheckedChangeListener {
  companion object {
    fun newInstance() = MuzeiSettingsFragment()
  }

  override fun getFragmentViewId(): Int = R.layout.fragment_settings_muzei

  override fun handleOnViewCreated() {
    setupDrawerWithToolbar(toolbar)
    top_bar.setMarginTopPlusStatusBarHeight()
    menu_wrapper.setPaddingTopPlusStatusBarHeight()
    initView()
  }

  private fun initView() {
    MuzeiConfig.asLiveData(MuzeiConfig::requiresBatteryNotLow).observe(this, Observer {
      view_requires_battery_not_low?.isChecked = it
      val drawableRes = if (it) R.drawable.ic_twotone_battery_90 else R.drawable.ic_twotone_battery_20
      icon_requires_battery_not_low?.setImageDrawable(target().drawableRes(drawableRes))
    })
    MuzeiConfig.asLiveData(MuzeiConfig::requiresCharging).observe(this, Observer {
      view_requires_charging?.isChecked = it
      val drawableRes = if (it) R.drawable.ic_twotone_battery_charging_50 else R.drawable.ic_twotone_battery_std
      icon_requires_charging?.setImageDrawable(target().drawableRes(drawableRes))
    })
    MuzeiConfig.asLiveData(MuzeiConfig::requiresDeviceIdle).observe(this, Observer {
      view_requires_device_idle?.isChecked = it
      val drawableRes = if (it) R.drawable.ic_twotone_mobile_friendly else R.drawable.ic_twotone_videogame_asset
      icon_requires_device_idle?.setImageDrawable(target().drawableRes(drawableRes))
    })
    MuzeiConfig.asLiveData(MuzeiConfig::requiresStorageNotLow).observe(this, Observer {
      view_requires_storage_not_low?.isChecked = it
      val drawableRes = if (it) R.drawable.ic_twotone_sd_storage else R.drawable.ic_twotone_disc_full
      icon_requires_storage_not_low?.setImageDrawable(target().drawableRes(drawableRes))
    })
    view_requires_battery_not_low.setOnCheckedChangeListener(this)
    view_requires_charging.setOnCheckedChangeListener(this)
    view_requires_device_idle.setOnCheckedChangeListener(this)
    view_requires_storage_not_low.setOnCheckedChangeListener(this)
  }

  override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
    val id = buttonView?.id
    lifecycleScope.launch {
      when (id) {
        R.id.view_requires_battery_not_low -> MuzeiConfig.requiresBatteryNotLow = isChecked
        R.id.view_requires_charging -> MuzeiConfig.requiresCharging = isChecked
        R.id.view_requires_device_idle -> MuzeiConfig.requiresDeviceIdle = isChecked
        R.id.view_requires_storage_not_low -> MuzeiConfig.requiresStorageNotLow = isChecked
      }
    }
  }
}
