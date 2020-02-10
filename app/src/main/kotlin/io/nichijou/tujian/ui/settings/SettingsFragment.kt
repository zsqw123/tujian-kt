package io.nichijou.tujian.ui.settings

import android.graphics.*
import android.view.*
import android.widget.*
import androidx.fragment.app.*
import androidx.lifecycle.*
import io.nichijou.oops.*
import io.nichijou.oops.ext.*
import io.nichijou.tujian.*
import io.nichijou.tujian.R
import io.nichijou.tujian.base.*
import io.nichijou.tujian.common.ext.*
import io.nichijou.tujian.common.ext.setMarginTopPlusStatusBarHeight
import io.nichijou.tujian.ext.*
import io.nichijou.tujian.ui.*
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.coroutines.*

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
  }

  override fun getFragmentViewId(): Int = R.layout.fragment_settings

  override fun handleOnViewCreated() {
    setupDrawerWithToolbar(toolbar)
    top_bar.setMarginTopPlusStatusBarHeight()
    menu_wrapper.setPaddingTopPlusStatusBarHeight()
    initView()
  }

  private val mainViewModel by activityViewModels<MainViewModel>()
  private fun initView() {
    applyOopsThemeStore {
      isDark.observe(this@SettingsFragment, Observer {
        mainViewModel.barColor.postValue(if (it) Color.BLACK else Color.WHITE)
        view_dark?.isChecked = it
        icon_dark?.setImageDrawable(if (!it) target().drawableRes(R.drawable.ic_twotone_brightness_5) else target().drawableRes(R.drawable.ic_twotone_brightness_2))
      })
    }
    view_dark?.setOnCheckedChangeListener { _, isChecked ->
      if (isChecked != Oops.immed().isDark) {
        switchTheme(isChecked)
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
  }

  private fun switchTheme(dark: Boolean) {
    lifecycleScope.launch {
      if (dark) {
        Oops.bulk {
          theme = R.style.AppThemeDark
          isDark = true
          windowBackground = Color.BLACK
          statusBarColor = Color.BLACK
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
          statusBarColor = Color.WHITE
          textColorPrimary = Color.BLACK
          textColorSecondary = Color.DKGRAY
          bottomNavigationViewNormalColor = Color.BLACK
          swipeRefreshLayoutBackgroundColor = Color.WHITE
        }
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
        R.id.view_screen_saver_interval -> Settings.screenSaverInterval = (progress + 5000).toLong()
        R.id.view_top_bar_radius -> Settings.topBarRadius = progress
        R.id.view_top_bar_elevation -> Settings.topBarElevation = progress
        R.id.view_card_radius -> Settings.cardRadius = progress
        R.id.view_card_elevation -> Settings.cardElevation = progress
        R.id.view_card_space -> Settings.cardSpace = progress
        R.id.view_creature_num -> {
          Settings.creatureNum = progress
          toast(getString(R.string.creature_num_format).format(progress / 100))
        }
      }
    }
  }
}
