package com.larvalabs.boo

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.Color
import android.hardware.Camera
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import java.io.IOException

class BooFragment : Fragment(), SurfaceHolder.Callback {

  private var camera: Camera? = null
  private lateinit var surfaceHolder: SurfaceHolder
  private var preview = false
  private var foundFaces = false

  private var lastHideTime: Long = 0

  private lateinit var introView: CreaturesView
  private lateinit var creaturesView: CreaturesView
  private var booWrapper: View? = null

  private var introRunning = true
  private var startTime: Long = 0

  private val faceDetectionListener: Camera.FaceDetectionListener = Camera.FaceDetectionListener { faces, _ ->
    // Don't do this until the intro is done, and after intro wait a bit before detecting faces
    if (introRunning) {
      return@FaceDetectionListener
    } else if (System.currentTimeMillis() - startTime < FIRST_HIDE_BUFFER) {
      return@FaceDetectionListener
    }
    val found = faces.isNotEmpty()
    if (foundFaces != found && HIDE_FROM_FACE) {
      val time = System.currentTimeMillis()
      when {
        found -> {
          creaturesView.setFaceVisible(true)
          foundFaces = true
          lastHideTime = time
          exitBoo()
        }
        time - lastHideTime > HIDE_BACKOFF_TIME -> {
          creaturesView.setFaceVisible(false)
          foundFaces = false
        }
        else -> {
          val delay = HIDE_BACKOFF_TIME - (time - lastHideTime)
          creaturesView.postDelayed({
            if (foundFaces) {
              creaturesView.setFaceVisible(false)
              foundFaces = false
            }
          }, delay)
        }
      }
    }
  }

  private var animateRunning = false
  private var onExited: (() -> Unit)? = null

  fun setOnExitedListener(onEnd: () -> Unit) {
    this.onExited = onEnd
  }

  fun exitBoo() {
    if (!animateRunning) {
      animateRunning = true
      booWrapper?.animate()?.alpha(0f)?.setDuration(360)?.setListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator?) {
          activity?.supportFragmentManager?.popBackStack()
          animateRunning = false
          onExited?.invoke()
        }
      })?.start()
    }
  }

  private fun enterBoo() {
    booWrapper?.animate()?.alpha(1f)?.setDuration(480)?.start()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_boo, container, false).apply {
      setOnTouchListener { _, _ -> true }// 拦截事件，防止点击穿透
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    val enableFuckBoo = arguments?.getBoolean(ENABLE_FUCK_BOO)!!
    if (enableFuckBoo) return
    val isDark = arguments?.getBoolean(IS_DARK)!!
    val isIntro = arguments?.getBoolean(IS_INTRO)!!
    val enableFace = arguments?.getBoolean(ENABLE_FACE)!!
    val enableBackground = arguments?.getBoolean(ENABLE_BACKGROUND)!!
    val creaturesNum = arguments?.getInt(CREATURE_NUM)!!
    val bgRes: Int
    val bodyColor: Int
    val eyeColor: Int
    if (isDark) {
      bodyColor = Color.WHITE
      eyeColor = Color.BLACK
      bgRes = R.drawable.bg_dark_gradient
    } else {
      bodyColor = Color.BLACK
      eyeColor = Color.WHITE
      bgRes = R.drawable.bg_light_gradient
    }

    if (enableFace) {
      val surfaceView = view.findViewById<SurfaceView>(R.id.preview)
      surfaceHolder = surfaceView.holder
      surfaceHolder.addCallback(this)
    }
    introView = view.findViewById(R.id.intro)
    creaturesView = view.findViewById(R.id.creatures)
    if (isIntro) {
      introView.setIntroMode(true)
      introView.setCreatureColor(bodyColor, eyeColor)
    } else {
      creaturesView.visibility = View.VISIBLE
      introView.visibility = View.GONE
      introRunning = false
      startTime = System.currentTimeMillis()
    }
    creaturesView.setCreatureColor(bodyColor, eyeColor)
    creaturesView.setCreatureNum(creaturesNum)
    booWrapper = view.findViewById(R.id.boo_wrapper)
    if (enableBackground) {
      if (booWrapper != null) {
        booWrapper!!.background = ContextCompat.getDrawable(view.context, bgRes)
      }
    }
    enterBoo()
  }

  override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    if (preview) {
      camera?.apply {
        stopFaceDetection()
        stopPreview()
      }
      preview = false
    }
    try {
      camera?.apply {
        setPreviewDisplay(surfaceHolder)
        setDisplayOrientation(90)
        startPreview()
        startFaceDetection()
      }
      preview = true
    } catch (e: IOException) {
      e.printStackTrace()
    }
  }

  override fun surfaceCreated(holder: SurfaceHolder) {
    val n = Camera.getNumberOfCameras()
    val cameraInfo = Camera.CameraInfo()
    for (i in 0 until n) {
      Camera.getCameraInfo(i, cameraInfo)
      if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
        camera = Camera.open(i)
        camera!!.setFaceDetectionListener(faceDetectionListener)
        return
      }
    }
  }

  override fun surfaceDestroyed(holder: SurfaceHolder) {
    try {
      camera?.apply {
        stopFaceDetection()
        stopPreview()
        release()
      }
      camera = null
      preview = false
    } catch (e: Exception) {
      e.printStackTrace()
    }

  }

  fun endIntro() {
    introView.postDelayed({
      creaturesView.visibility = View.VISIBLE
      introView.visibility = View.GONE
      introRunning = false
      startTime = System.currentTimeMillis()
    }, 2000)
  }

  companion object {
    private const val HIDE_FROM_FACE = true
    private const val FIRST_HIDE_BUFFER: Long = 2500
    private const val HIDE_BACKOFF_TIME: Long = 1500
    private const val IS_DARK = "is_dark"
    private const val IS_INTRO = "is_intro"
    private const val ENABLE_FACE = "enable_face"
    private const val ENABLE_FUCK_BOO = "enable_fuck_boo"
    private const val ENABLE_BACKGROUND = "enable_background"
    private const val CREATURE_NUM = "creature_num"
    fun newInstance(
      isDark: Boolean,
      isIntro: Boolean = false,
      enableFace: Boolean = true,
      enableBackground: Boolean = true,
      creatureNum: Int = 10,
      enableFuckBoo: Boolean = true
    ): BooFragment = BooFragment().apply {
      arguments = bundleOf(IS_DARK to isDark, IS_INTRO to isIntro, ENABLE_FACE to enableFace, ENABLE_FUCK_BOO to enableFuckBoo, ENABLE_BACKGROUND to enableBackground, CREATURE_NUM to creatureNum)
    }
  }
}
