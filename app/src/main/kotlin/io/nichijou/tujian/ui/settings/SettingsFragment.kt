package io.nichijou.tujian.ui.settings

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.afollestad.assent.AssentResult
import com.afollestad.assent.Callback
import com.afollestad.assent.Permission
import com.afollestad.assent.askForPermissions
import io.nichijou.oops.Oops
import io.nichijou.oops.ext.applyOopsThemeStore
import io.nichijou.oops.ext.drawableRes
import io.nichijou.oops.ext.setPaddingTopPlusStatusBarHeight
import io.nichijou.tujian.App
import io.nichijou.tujian.R
import io.nichijou.tujian.Settings
import io.nichijou.tujian.base.BaseFragment
import io.nichijou.tujian.common.ext.animateTo
import io.nichijou.tujian.common.ext.dp2px
import io.nichijou.tujian.common.ext.setMarginTopPlusStatusBarHeight
import io.nichijou.tujian.ext.addFragmentToActivity
import io.nichijou.tujian.ext.target
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.configuration
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.support.v4.selector
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.uiThread

class SettingsFragment : BaseFragment(), View.OnClickListener, SeekBar.OnSeekBarChangeListener {

  override fun onClick(v: View?) {
    when (v?.id) {
      R.id.view_wallpaper_settings -> addFragmentToActivity(WallpaperSettingsFragment.newInstance(), hideBefore = true)
      R.id.view_appwidget_settings -> addFragmentToActivity(AppWidgetSettingsFragment.newInstance(), hideBefore = true)
      R.id.view_muzei_settings -> addFragmentToActivity(MuzeiSettingsFragment.newInstance(), hideBefore = true)
    }
  }

  companion object {
    fun newInstance() = SettingsFragment()
    fun switchTheme(darkInt: Int) {
      var dark = false
      when (darkInt) {
        0 -> dark = true
        1 -> dark = false
        else -> {
          when (App.context!!.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> {
              dark = false
            } // Night mode is not active, we're using the light theme
            Configuration.UI_MODE_NIGHT_YES -> {
              dark = true
            } // Night mode is active, we're using dark theme
          }
        }
      }
      doAsync {
        uiThread {
          if (dark) {
            Oops.bulk {
              theme = R.style.AppThemeDark
              isDark = true
              windowBackground = Color.BLACK
              statusBarColor = 0
              textColorPrimary = Color.WHITE
              textColorSecondary = Color.LTGRAY
              bottomNavigationViewNormalColor = Color.WHITE
              swipeRefreshLayoutBackgroundColor = Color.BLACK
            }
          } else {
            Oops.bulk {
              theme = R.style.AppTheme
              isDark = false
              windowBackground = Color.WHITE
              statusBarColor = 0
              textColorPrimary = Color.BLACK
              textColorSecondary = Color.DKGRAY
              bottomNavigationViewNormalColor = Color.BLACK
              swipeRefreshLayoutBackgroundColor = Color.WHITE
            }
          }
        }
      }
    }
  }

  override fun getFragmentViewId(): Int = R.layout.fragment_settings

  override fun onHiddenChanged(hidden: Boolean) {
    super.onHiddenChanged(hidden)
    if (!hidden) {
      setupDrawerWithToolbar(toolbar)
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupDrawerWithToolbar(toolbar)
    top_bar.setMarginTopPlusStatusBarHeight()
    menu_wrapper.setPaddingTopPlusStatusBarHeight()
    initView()
  }

  @SuppressLint("SetTextI18n")
  private fun initView() {
    applyOopsThemeStore {
      isDark.observe(viewLifecycleOwner, Observer {
        icon_dark?.setImageDrawable(if (!it) target().drawableRes(R.drawable.ic_twotone_brightness_5) else target().drawableRes(R.drawable.ic_twotone_brightness_2))
      })
    }
    layout_dark_setting.setOnClickListener {
      selector("选择暗色模式配置", listOf("暗色", "亮色", "跟随系统")) { _, i ->
        when (i) {
          0 -> Settings.darkModeInt = 0
          1 -> Settings.darkModeInt = 1
          2 -> Settings.darkModeInt = 2
        }
      }
    }
    lifecycleScope.launch(Dispatchers.IO) {
      val enableFaceDetection = Settings.enableFaceDetection
      val enableFuckBoo = Settings.fuckBoo
      val cardRadius = Settings.cardRadius
      val cardElevation = Settings.cardElevation
      val cardSpace = Settings.cardSpace
      val topBarRadius = Settings.topBarRadius
      val topBarElevation = Settings.topBarElevation
      val creatureNum = Settings.creatureNum
      val interval = (Settings.screenSaverInterval - 5000).toInt()
      withContext(Dispatchers.Main) {
        view_face_detection?.isChecked = enableFaceDetection
        view_fuck_boo?.isChecked = enableFuckBoo
        0.animateTo(interval) {
          view_screen_saver_interval?.progress = it
        }
        0.animateTo(cardRadius) {
          view_card_radius?.progress = it
        }
        0.animateTo(cardElevation) {
          view_card_elevation?.progress = it
        }
        0.animateTo(cardSpace) {
          view_card_space?.progress = it
        }
        0.animateTo(topBarRadius) {
          view_top_bar_radius?.progress = it
        }
        0.animateTo(topBarElevation) {
          view_top_bar_elevation?.progress = it
        }
        0.animateTo(creatureNum) {
          view_creature_num?.progress = it
        }
      }
    }
    view_face_detection.setOnCheckedChangeListener { _, isChecked ->
      Settings.enableFaceDetection = isChecked
      if (isChecked) askForPermissions(Permission.CAMERA, callback = object : Callback {
        override fun invoke(result: AssentResult) {
          try {
            if (PackageManager.PERMISSION_GRANTED != requireContext().packageManager.checkPermission(Manifest.permission.CAMERA, requireContext().packageName)) {
              if (result.isAllDenied()) {
                view_face_detection.isChecked = false
                Settings.enableFaceDetection = false
                toast("必须允许相机权限进行人脸识别")
              }
            }
          } catch (e: Exception) {
            e.printStackTrace()
          }
        }
      })
    }
    view_fuck_boo.setOnCheckedChangeListener { _, isChecked ->
      Settings.fuckBoo = isChecked
    }
    view_top_bar_radius.max = target().dp2px(25f).toInt() * 100
    view_top_bar_elevation.max = target().dp2px(16f).toInt() * 100
    view_card_radius.max = target().dp2px(25f).toInt() * 100
    view_card_elevation.max = target().dp2px(16f).toInt() * 100
    view_card_space.max = 1600
    view_screen_saver_interval.setOnSeekBarChangeListener(this)
    view_top_bar_radius.setOnSeekBarChangeListener(this)
    view_top_bar_elevation.setOnSeekBarChangeListener(this)
    view_card_radius.setOnSeekBarChangeListener(this)
    view_card_elevation.setOnSeekBarChangeListener(this)
    view_card_space.setOnSeekBarChangeListener(this)
    view_creature_num.setOnSeekBarChangeListener(this)
    view_wallpaper_settings.setOnClickListener(this)
    view_appwidget_settings.setOnClickListener(this)
    view_muzei_settings.setOnClickListener(this)
    //暗黑模式设置标题
    val darkTextEx = when (Settings.darkModeInt) {
      0 -> "暗色"
      1 -> "亮色"
      else -> "跟随系统"
    }
    val darkTextBefore = getString(R.string.dark_theme_desc)
    sub_dark.text = darkTextBefore + darkTextEx
  }

  override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) = Unit

  override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

  override fun onStopTrackingTouch(seekBar: SeekBar) {
    val id = seekBar.id
    val progress = seekBar.progress
    lifecycleScope.launch {
      when (id) {
        R.id.view_screen_saver_interval -> Settings.screenSaverInterval = (progress + 5000).toLong()
        R.id.view_top_bar_radius -> Settings.topBarRadius = progress
        R.id.view_top_bar_elevation -> Settings.topBarElevation = progress
        R.id.view_card_radius -> Settings.cardRadius = progress
        R.id.view_card_elevation -> Settings.cardElevation = progress
        R.id.view_card_space -> Settings.cardSpace = progress
        R.id.view_creature_num -> {
          Settings.creatureNum = progress
          if (progress / 100 < 1) Settings.fuckBoo = true
          view_fuck_boo.isChecked = true
          toast(getString(R.string.creature_num_format).format(progress / 100))
        }
      }
    }
  }
}
