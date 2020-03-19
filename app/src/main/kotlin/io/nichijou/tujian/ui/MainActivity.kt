package io.nichijou.tujian.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.os.CountDownTimer
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.afollestad.assent.Permission
import com.afollestad.assent.askForPermissions
import com.afollestad.materialdialogs.MaterialDialog
import com.billy.android.swipe.SmartSwipe
import com.billy.android.swipe.consumer.DrawerConsumer
import com.billy.android.swipe.consumer.SlidingConsumer
import com.larvalabs.boo.BooFragment
import com.zzhoujay.richtext.RichText
import io.nichijou.oops.Oops
import io.nichijou.oops.ext.*
import io.nichijou.tujian.R
import io.nichijou.tujian.Settings
import io.nichijou.tujian.common.db.TujianStore
import io.nichijou.tujian.common.ext.asLiveData
import io.nichijou.tujian.ext.addFragmentToActivity
import io.nichijou.tujian.isDark
import io.nichijou.tujian.ui.about.AboutFragment
import io.nichijou.tujian.ui.archive.ArchiveFragment
import io.nichijou.tujian.ui.settings.SettingsFragment
import io.nichijou.tujian.ui.today.TodayFragment
import io.nichijou.tujian.ui.upload.UploadFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import me.yokeyword.fragmentation.SupportActivity
import me.yokeyword.fragmentation.SupportFragment
import org.jetbrains.anko.dip
import org.jetbrains.anko.isSelectable
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.textColor
import org.koin.android.ext.android.inject


class MainActivity : SupportActivity(), CoroutineScope by MainScope() {
  private var firstTime = true
  override fun onCreate(savedInstanceState: Bundle?) {
    Oops.attach(this)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    if (Oops.immed().isFirstTime) {
      val def = ContextCompat.getColor(this@MainActivity, R.color.def)
      Oops.bulk {
        theme = R.style.AppTheme
        windowBackground = Color.WHITE
        statusBarColor = 0
        colorAccent = def
        textColorPrimary = Color.BLACK
        textColorSecondary = Color.DKGRAY
        toolbarIconColor = def
        toolbarTitleColor = def
        swipeRefreshLayoutBackgroundColor = Color.WHITE
      }
    }
    Oops.bulk {
      statusBarColor = 0
      navBarColor = 0
    }
    bindLifecycle()
    if (!Settings.feiHua) {
      MaterialDialog(this).title(text = "隐私政策提示").icon(R.mipmap.ic_launcher).show {
        cancelOnTouchOutside(false)
        cancelable(false)
        message(R.string.fei_hua) {
          val text = messageTextView
          text.isSelectable = false
          RichText.fromMarkdown(getString(R.string.fei_hua)).linkFix { holder ->
            holder!!.color = if (isDark()) Color.parseColor("#22EB4F") else Color.parseColor("#DD14B0")
            holder.isUnderLine = false
          }.into(text)
        }
        positiveButton(text = "同意并继续") { Settings.feiHua = true }
        negativeButton(text = "仅浏览")
      }.cornerRadius(12f)
    }
    //swipe and fragment
    val point = Point()
    windowManager.defaultDisplay.getRealSize(point)
    slide.layoutParams = FrameLayout.LayoutParams(point.x * 3 / 4, matchParent)
    swipeConsumer = SmartSwipe.wrap(this).addConsumer(SlidingConsumer())
      .setHorizontalDrawerView(slide).setScrimColor(Color.parseColor("#9A000000"))// 侧滑
    swipeConsumer!!.edgeSize = dip(20)
//    translucentStatusBar(true)// 状态栏沉浸
//    setLightStatusBarCompat(isDark())
    if (findFragment(TodayFragment::class.java) == null) {
      mFragments = arrayOf(TodayFragment.newInstance(),
        ArchiveFragment.newInstance(), UploadFragment.newInstance(),
        SettingsFragment.newInstance(), AboutFragment.newInstance())
      nowFragment = mFragments[0]!!
      loadMultipleRootFragment(R.id.container, 0, mFragments[0], mFragments[1], mFragments[2], mFragments[3], mFragments[4])
    } else {
      mFragments[0] = findFragment(TodayFragment::class.java)
      mFragments[1] = findFragment(ArchiveFragment::class.java)
      mFragments[2] = findFragment(UploadFragment::class.java)
      mFragments[3] = findFragment(SettingsFragment::class.java)
      mFragments[4] = findFragment(AboutFragment::class.java)
    }
    slide_today.setOnClickListener { showHideListener(this, mFragments[0]!!) }
    slide_save.setOnClickListener { showHideListener(this, mFragments[1]!!) }
    slide_upload.setOnClickListener { showHideListener(this, mFragments[2]!!) }
    slide_settings.setOnClickListener { showHideListener(this, mFragments[3]!!) }
    slide_info.setOnClickListener { showHideListener(this, mFragments[4]!!) }
    //boo fragment
    if (savedInstanceState == null && !enableFuckBoo) {
      val newFragment = BooFragment.newInstance(Oops.immed().isDark, isIntro = true, enableFace = enableFaceDetection, enableFuckBoo = enableFuckBoo)
      newFragment.setOnExitedListener { resetScreenSaverTimer() }
      addFragmentToActivity(newFragment, tag = getString(R.string.boo_tag))
    }
  }

  override fun onWindowFocusChanged(hasFocus: Boolean) {
    super.onWindowFocusChanged(hasFocus)
    try {
      if (hasFocus && firstTime) {
        if (Settings.enableFaceDetection) {
          askForPermissions(
            Permission.READ_EXTERNAL_STORAGE,
            Permission.WRITE_EXTERNAL_STORAGE,
            Permission.CAMERA) {}
        } else {
          askForPermissions(
            Permission.READ_EXTERNAL_STORAGE,
            Permission.WRITE_EXTERNAL_STORAGE) {}
        }
        firstTime = false
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  // 点击关屏保
  override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
    if (enableScreenSaver) {
      resetScreenSaverTimer()
      if (ev?.action == MotionEvent.ACTION_DOWN) {
        val fragment = supportFragmentManager.findFragmentByTag(getString(R.string.boo_tag))
          ?: return super.dispatchTouchEvent(ev)
        (fragment as BooFragment).exitBoo()
      }
    }
    return super.dispatchTouchEvent(ev)
  }

  private var enableFaceDetection: Boolean = Settings.enableFaceDetection
  private var enableFuckBoo: Boolean = Settings.fuckBoo
  private var darkMode: Int = Settings.darkModeInt
  private var screenSaverInterval: Long = Settings.screenSaverInterval

  private val mainViewModel by lazy {
    ViewModelProvider(this).get(MainViewModel::class.java)
  }

  private val tujianStore by inject<TujianStore>()

  private var enableScreenSaver = false

  private fun bindLifecycle() {
    mainViewModel.enableScreenSaver.observe(this, Observer {
      enableScreenSaver = it
      if (!it) {
        countDownTimer?.cancel()
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
    val accent = color.accent
    val textColor = color.primaryText
    listOf(slide_today_icon, slide_save_icon, slide_settings_icon, slide_upload_icon, slide_info_icon).forEach {
      it.tint(accent)
    }
    listOf(slide_today_text, slide_save_text, slide_upload_text, slide_info_text, slide_settings_text).forEach {
      it.textColor = textColor
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

  override fun onDestroy() {
    cancel()
    super.onDestroy()
  }

  companion object {
    var swipeConsumer: DrawerConsumer? = null
    var nowFragment: SupportFragment = TodayFragment.newInstance()
    var mFragments = arrayOfNulls<SupportFragment>(5)

    fun showHideListener(mainActivity: SupportActivity, fragment: SupportFragment) {
      mainActivity.showHideFragment(fragment, nowFragment)
      nowFragment = fragment
      swipeConsumer!!.smoothClose()
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
