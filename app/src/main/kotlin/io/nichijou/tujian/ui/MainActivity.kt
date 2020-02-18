package io.nichijou.tujian.ui

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.assent.Permission
import com.afollestad.assent.askForPermissions
import com.afollestad.materialdialogs.MaterialDialog
import com.larvalabs.boo.BooFragment
import com.yarolegovich.slidingrootnav.SlidingDrawer
import com.yarolegovich.slidingrootnav.menu.*
import com.zzhoujay.richtext.RichText
import io.nichijou.oops.Oops
import io.nichijou.oops.ext.*
import io.nichijou.tujian.R
import io.nichijou.tujian.Settings
import io.nichijou.tujian.base.BaseActivity
import io.nichijou.tujian.common.db.TujianStore
import io.nichijou.tujian.common.ext.asLiveData
import io.nichijou.tujian.ext.addFragmentToActivity
import io.nichijou.tujian.ext.handleBackPress
import io.nichijou.tujian.ext.replaceFragmentInActivity
import io.nichijou.tujian.isDark
import io.nichijou.tujian.ui.about.AboutFragment
import io.nichijou.tujian.ui.archive.ArchiveFragment
import io.nichijou.tujian.ui.settings.SettingsFragment
import io.nichijou.tujian.ui.today.TodayFragment
import io.nichijou.tujian.ui.upload.UploadFragment
import io.nichijou.utils.bodyColor
import io.nichijou.utils.isColorLight
import io.nichijou.utils.titleColor
import kotlinx.android.synthetic.main.menu_left_drawer.*
import org.jetbrains.anko.toast
import org.koin.android.ext.android.inject
import kotlin.system.exitProcess

class MainActivity : BaseActivity() {
  override fun getContentViewId(): Int = R.layout.activity_main

  override fun handleOnCreate(savedInstanceState: Bundle?) {
    translucentStatusBar(true)
    if (Oops.immed().isFirstTime) {
      val def = ContextCompat.getColor(this@MainActivity, R.color.def)
      Oops.bulk {
        theme = R.style.AppTheme
        windowBackground = Color.WHITE
        statusBarColor = Color.TRANSPARENT
        colorAccent = def
        textColorPrimary = Color.BLACK
        textColorSecondary = Color.DKGRAY
        toolbarIconColor = def
        toolbarTitleColor = def
        snackbarBackgroundColor = def
        snackbarTextColor = def.bodyColor()
        snackbarActionColor = def.titleColor()
        bottomNavigationViewNormalColor = Color.BLACK
        bottomNavigationViewSelectedColor = def
        swipeRefreshLayoutBackgroundColor = Color.WHITE
        disableAutoStatusBarColor<MainActivity>(true)
        disableAutoNavBarColor<MainActivity>(true)
      }
    }
    if (Settings.enableFaceDetection) {
      askForPermissions(Permission.CAMERA) {}
    }
    askForPermissions(
      Permission.READ_EXTERNAL_STORAGE,
      Permission.WRITE_EXTERNAL_STORAGE) {}
    if (savedInstanceState == null) {
      replaceFragmentInActivity(TodayFragment.newInstance())
      val newFragment = BooFragment.newInstance(Oops.immed().isDark, isIntro = true, enableFace = enableFaceDetection, enableFuckBoo = enableFuckBoo)
      newFragment.setOnExitedListener {
        resetScreenSaverTimer()
      }
      if (!enableFuckBoo) addFragmentToActivity(newFragment, tag = getString(R.string.boo_tag))
    }
    initDrawer(savedInstanceState)
    bindLifecycle()
    if (!Settings.feiHua) {
      MaterialDialog(this).title(text = "隐私政策提示").icon(R.mipmap.ic_launcher).show {
        cancelOnTouchOutside(false)
        cancelable(false)
        message(R.string.fei_hua) {
          val text = messageTextView
          RichText.fromMarkdown(getString(R.string.fei_hua)).linkFix { holder ->
            holder!!.color = if (isDark()) Color.parseColor("#22EB4F") else Color.parseColor("#DD14B0")
            holder.isUnderLine = false
          }.into(text)
        }

        positiveButton(text = "同意并继续") { Settings.feiHua = true}
        negativeButton(text = "仅浏览")
      }.cornerRadius(12f)
    }
  }

  private var enableFaceDetection: Boolean = Settings.enableFaceDetection
  private var enableFuckBoo: Boolean = Settings.fuckBoo
  private var darkMode: Int = Settings.darkModeInt
  private var screenSaverInterval: Long = Settings.screenSaverInterval

  private val mainViewModel by lazy {
    ViewModelProvider(this).get(MainViewModel::class.java)
  }

  private val tujianStore by inject<TujianStore>()

  private fun updateBarColor() {
    setStatusBarColorCompat(fragmentStatusBarColor)
    setNavBarColorCompat(fragmentStatusBarColor)
    if (fragmentStatusBarColor == Color.TRANSPARENT) {
      val dark = Oops.immed().isDark
      setLightStatusBarCompat(!dark)
      setLightNavBarCompat(!dark)
    } else {
      val light = fragmentStatusBarColor.isColorLight()
      setLightStatusBarCompat(light)
      setLightNavBarCompat(light)
    }
  }

  private var enableScreenSaver = false

  private fun bindLifecycle() {
    mainViewModel.barColor.observe(this, Observer {
      fragmentStatusBarColor = it
      updateBarColor()
    })
    mainViewModel.enableScreenSaver.observe(this, Observer {
      enableScreenSaver = it
      if (!it) {
        stopScreenSaverTimer()
      }
    })
    Settings.asLiveData(Settings::enableFaceDetection).observe(this, Observer {
      enableFaceDetection = it
      resetScreenSaverTimer()
    })
    Settings.asLiveData(Settings::fuckBoo).observe(this, Observer {
      enableFuckBoo = it
      resetScreenSaverTimer()
    })
    Settings.asLiveData(Settings::screenSaverInterval).observe(this, Observer {
      screenSaverInterval = it
      resetScreenSaverTimer()
    })
    Settings.asLiveData(Settings::creatureNum).observe(this, Observer {
      creatureNum = it / 100
      resetScreenSaverTimer()
    })
    Settings.asLiveData(Settings::darkModeInt).observe(this, Observer {
      darkMode = it
      SettingsFragment.switchTheme(darkMode)
    })
    applyOopsThemeStore {
      mediateLiveDataNonNull(
        colorAccent,
        textColorPrimary,
        ThemeColor.live()
      ).observe(this@MainActivity, Observer(::updateDrawerColor))
    }
  }

  private fun updateDrawerColor(color: ThemeColor) {
    adapter.items.forEach { i ->
      updateItemColor(i, color)
    }
    adapter.notifyDataSetChanged()
  }

  private fun updateItemColor(i: DrawerItem<DrawerAdapter.ViewHolder>, color: ThemeColor) {
    when (i::class.java) {
      SimpleItem::class.java -> {
        (i as SimpleItem)
          .withNormalTint(color.primaryText)
          .withSelectedTint(color.accent)
      }
      HeaderItem::class.java -> {
        (i as HeaderItem).withNormalTint(color.primaryText)
      }
      SectionItem::class.java -> {
        (i as SectionItem).withNormalTint(color.primaryText)
      }
    }
  }

  private var countDownTimer: CountDownTimer? = null
  private var creatureNum = 10

  //开始屏保计时
  private fun startScreenSaverTimer() {
    if (countDownTimer == null) {
      countDownTimer = object : CountDownTimer(screenSaverInterval, 1000) {
        override fun onTick(millisUntilFinished: Long) {
        }

        override fun onFinish() {
          //计时结束时调用
          if (!enableFuckBoo) {
            val fragment = supportFragmentManager.findFragmentByTag(getString(R.string.boo_tag))
            if (fragment == null) {
              val newFragment = BooFragment.newInstance(Oops.immed().isDark, isIntro = false, enableFace = enableFaceDetection, enableFuckBoo = enableFuckBoo, creatureNum = creatureNum)
              newFragment.setOnExitedListener {
                resetScreenSaverTimer()
              }
              addFragmentToActivity(newFragment, tag = getString(R.string.boo_tag))
            }
          }
        }
      }
    }
    countDownTimer?.start()
  }

  private fun resetScreenSaverTimer() {
    countDownTimer?.apply {
      cancel()
      start()
    } ?: startScreenSaverTimer()
  }

  private fun stopScreenSaverTimer() {
    countDownTimer?.cancel()
  }

  private var fragmentStatusBarColor: Int = 0

  private lateinit var adapter: DrawerAdapter
  lateinit var drawer: SlidingDrawer
  private fun initDrawer(savedInstanceState: Bundle?) {
    drawer = SlidingDrawer(this)
      .withMenuOpened(false)
      .withContentClickableWhenMenuOpened(false)
      .withSavedState(savedInstanceState)
      .withMenuLayout(R.layout.menu_left_drawer)
      .withRootViewElevation(8)
      .withRootViewScale(.6f)
      .withDrawerListener(object : DrawerLayout.DrawerListener {
        override fun onDrawerStateChanged(newState: Int) = Unit
        override fun onDrawerSlide(drawerView: View, slideOffset: Float) = Unit
        override fun onDrawerClosed(drawerView: View) {
          updateBarColor()
        }

        override fun onDrawerOpened(drawerView: View) {
          setLightStatusBarCompat(!Oops.immed().isDark)
        }
      })
      .inject()
    drawer.setDispatchTouch {
      if (enableScreenSaver) {
        resetScreenSaverTimer()
        if (it?.action == MotionEvent.ACTION_DOWN) {
          val fragment = supportFragmentManager.findFragmentByTag(getString(R.string.boo_tag))
            ?: return@setDispatchTouch
          (fragment as BooFragment).exitBoo()
        }
      }
    }
    adapter = DrawerAdapter(
      mutableListOf(
        HeaderItem(this.drawableRes(R.mipmap.ic_launcher_foreground), getString(R.string.app_name), getString(R.string.app_name_pinyin)),
//        HitokotoItem(-100),
        SimpleItem(R.drawable.ic_twotone_wb_sunny, this.drawableRes(R.drawable.ic_twotone_wb_sunny), getString(R.string.today)).setChecked(true),
        SimpleItem(R.drawable.ic_twotone_loyalty, this.drawableRes(R.drawable.ic_twotone_loyalty), getString(R.string.archive)),
        SimpleItem(R.drawable.ic_twotone_supervisor_account, this.drawableRes(R.drawable.ic_twotone_supervisor_account), getString(R.string.post)),
        SpaceItem(40f),
        SimpleItem(R.drawable.ic_twotone_settings, this.drawableRes(R.drawable.ic_twotone_settings), getString(R.string.settings)),
        SimpleItem(R.drawable.ic_twotone_info, this.drawableRes(R.drawable.ic_twotone_info), getString(R.string.about))
      ) as MutableList<DrawerItem<DrawerAdapter.ViewHolder>>
    )
    adapter.setListener {
      val id = it.id
      if (current == id) return@setListener
      current = id
      when (id) {
        R.drawable.ic_twotone_wb_sunny -> replaceFragmentInActivity(TodayFragment.newInstance())
        R.drawable.ic_twotone_supervisor_account -> replaceFragmentInActivity(UploadFragment.newInstance())
        R.drawable.ic_twotone_loyalty -> replaceFragmentInActivity(ArchiveFragment.newInstance())
        R.drawable.ic_twotone_settings -> replaceFragmentInActivity(SettingsFragment.newInstance())
        R.drawable.ic_twotone_info -> replaceFragmentInActivity(AboutFragment.newInstance())
      }
      drawer.closeMenu()
    }
    menu.isNestedScrollingEnabled = false
    menu.layoutManager = LinearLayoutManager(this)
    menu.adapter = adapter
  }

  private var current = R.drawable.ic_twotone_wb_sunny

  private val mHints = LongArray(2)
  override fun onBackPressed() {
    if (drawer.isMenuOpened()) {
      drawer.closeMenu()
    } else {
      if (!handleBackPress()) {
        System.arraycopy(mHints, 1, mHints, 0, mHints.size - 1)
        mHints[mHints.size - 1] = SystemClock.uptimeMillis()
        toast(R.string.repress_exit)
        if (SystemClock.uptimeMillis() - mHints[0] <= 1600) {
          exitProcess(0)
        }
      }
    }
  }
}

data class ThemeColor(@ColorInt val accent: Int, @ColorInt val primaryText: Int) {
  companion object {
    fun live() = object : Live2NonNull<Int, Int, ThemeColor> {
      override fun apply(a: Int, b: Int): ThemeColor = ThemeColor(a, b)
    }
  }
}
