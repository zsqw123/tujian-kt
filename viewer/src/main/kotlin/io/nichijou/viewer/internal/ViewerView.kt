package io.nichijou.viewer.internal

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.drawToBitmap
import com.facebook.drawee.view.SimpleDraweeView
import io.nichijou.viewer.R
import io.nichijou.viewer.ViewerItemView
import io.nichijou.viewer.ext.*
import io.nichijou.viewer.gestures.SimpleOnGestureListener
import io.nichijou.viewer.gestures.SwipeDirection
import io.nichijou.viewer.gestures.SwipeDirection.*
import io.nichijou.viewer.gestures.SwipeDirectionDetector
import io.nichijou.viewer.gestures.SwipeToDismissHandler
import kotlinx.android.synthetic.main.view_viewer_wapper.view.*
import org.jetbrains.anko.*
import java.io.OutputStream

internal class ViewerView<T>(context: Context) : FrameLayout(context) {
  var currentPosition: Int
    get() = viewPager.currentItem
    set(value) {
      viewPager.currentItem = value
    }
  var onDismiss: (() -> Unit)? = null
  var onPageChange: ((position: Int) -> Unit)? = null
  val isScaled
    get() = viewAdapter?.isScaled(currentPosition) ?: false
  var containerPadding = intArrayOf(0, 0, 0, 0)
  var overlayView: View? = null
    set(value) {
      field = value
      if (value != null) {
        rootContainer.addView(value)
      }
    }
  private var rootContainer: ViewGroup
  private var backgroundView: View
  private var dismissContainer: ViewGroup
  private val transitionImageContainer: FrameLayout
  private var externalTransitionImageView: View? = null
  private var viewPager: MultiTouchViewPager
  private var viewAdapter: ViewPagerAdapter<T>? = null
  private var directionDetector: SwipeDirectionDetector
  private var gestureDetector: GestureDetectorCompat
  private var scaleDetector: ScaleGestureDetector
  private lateinit var swipeDismissHandler: SwipeToDismissHandler
  private var wasScaled: Boolean = false
  private var wasDoubleTapped = false
  private var isOverlayWasClicked: Boolean = false
  private var swipeDirection: SwipeDirection? = null
  private var images: List<T> = listOf()
  private var bgLoader: ((view: SimpleDraweeView, data: T) -> Unit)? = null
  private lateinit var transitionImageAnimator: TransitionImageAnimator
  private var startPosition: Int = 0
    set(value) {
      field = value
      currentPosition = value
    }
  private val shouldDismissToBottom: Boolean
    get() = externalTransitionImageView == null
      || !externalTransitionImageView.isRectVisible
      || !isAtStartPosition
  private val isAtStartPosition: Boolean
    get() = currentPosition == startPosition
  private var hasTouched = false
  var onOpenStart: ((duration: Long) -> Unit)? = null
  var onOpenEnd: (() -> Unit)? = null
  var onCloseStart: ((duration: Long) -> Unit)? = null
  var onCloseEnd: (() -> Unit)? = null
  var onSingleTap: ((MotionEvent) -> Unit)? = null
  var onViewSwipe: ((translationY: Float, translationLimit: Int) -> Unit)? = null

  init {
    inflate(context, R.layout.view_viewer_wapper, this)

    rootContainer = findViewById(R.id.rootContainer)
    backgroundView = findViewById(R.id.backgroundView)
    dismissContainer = findViewById(R.id.dismissContainer)

    transitionImageContainer = findViewById(R.id.transitionImageContainer)
    viewPager = findViewById(R.id.viewPager)
    viewPager.offscreenPageLimit = 4
    viewPager.addOnPageChangeListener(
      onPageSelected = {
        externalTransitionImageView?.apply {
          if (isAtStartPosition) makeInvisible() else makeVisible()
        }
        onPageChange?.invoke(it)
      })

    directionDetector = createSwipeDirectionDetector()
    gestureDetector = createGestureDetector()
    scaleDetector = createScaleGestureDetector()
  }

  fun currentItem(): T = images[currentPosition]

  private fun removeOverlayView() {
    if (overlayView != null) {
      rootContainer.removeView(overlayView)
    }
  }

  override fun dispatchTouchEvent(event: MotionEvent): Boolean {
    if (!hasTouched) {
      hasTouched = true
      transitionImageContainer.makeGone()
    }
    if (overlayView.isVisible && overlayView?.dispatchTouchEvent(event) == true) {
      return true
    }
    if (transitionImageAnimator.isAnimating) {
      return true
    }
    //one more tiny kludge to prevent single tap a one-finger zoom which is broken by the SDK
    if (wasDoubleTapped && event.action == MotionEvent.ACTION_MOVE && event.pointerCount == 1) {
      return true
    }

    handleUpDownEvent(event)

    if (swipeDirection == null && (scaleDetector.isInProgress || event.pointerCount > 1 || wasScaled)) {
      wasScaled = true
      return viewPager.dispatchTouchEvent(event)
    }

    return if (isScaled) super.dispatchTouchEvent(event) else handleTouchIfNotScaled(event)
  }

  override fun setBackgroundColor(color: Int) {
    backgroundView.setBackgroundColor(color)
  }

  fun setImages(images: List<T>, startPosition: Int, pagerLoader: (view: ViewerItemView, data: T) -> Unit, bgLoader: (view: SimpleDraweeView, data: T) -> Unit) {
    this.images = images
    this.bgLoader = bgLoader
    this.viewAdapter = ViewPagerAdapter(images, pagerLoader)
    this.viewPager.adapter = viewAdapter
    this.startPosition = startPosition
  }

  fun open(transitionImageView: View?) {
    viewPager.makeGone()

    externalTransitionImageView = transitionImageView
    bgLoader?.invoke(this.transitionImageView, images[startPosition])

    transitionImageAnimator = createTransitionImageAnimator(transitionImageView)
    swipeDismissHandler = createSwipeToDismissHandler()
    rootContainer.setOnTouchListener(swipeDismissHandler)

    animateOpen()
  }

  fun close() {
    if (shouldDismissToBottom) {
      swipeDismissHandler.initiateDismissToBottom()
    } else {
      animateClose()
    }
  }

  fun updateImages(images: List<T>) {
    this.images = images
    viewAdapter?.updateImages(images)
  }

  fun updateTransitionImage(imageView: ImageView?) {
    externalTransitionImageView?.makeVisible()
    imageView?.makeInvisible()

    externalTransitionImageView = imageView
    startPosition = currentPosition
    transitionImageAnimator = createTransitionImageAnimator(imageView)
    bgLoader?.invoke(this.transitionImageView, images[startPosition])
  }

  fun resetScale() {
    viewAdapter?.resetScale(currentPosition)
  }

  private fun animateOpen() {
    transitionImageAnimator.animateOpen(
      containerPadding = containerPadding,
      onTransitionStart = { duration ->
        onOpenStart?.invoke(duration)
        backgroundView.animateAlpha(0f, 1f, duration)
      },
      onTransitionEnd = {
        viewPager.makeVisible()
        onOpenEnd?.invoke()
      })
  }

  private fun animateClose() {
    viewPager.makeGone()
    transitionImageContainer.makeVisible()
    dismissContainer.applyMargin(0, 0, 0, 0)
    transitionImageAnimator.animateClose(
      shouldDismissToBottom = shouldDismissToBottom,
      onTransitionStart = { duration ->
        onCloseStart?.invoke(duration)
        backgroundView.animateAlpha(backgroundView.alpha, 0f, duration)
      },
      onTransitionEnd = {
        removeOverlayView()
        onDismiss?.invoke()
        onCloseEnd?.invoke()
      })
  }

  private fun handleTouchIfNotScaled(event: MotionEvent): Boolean {
    directionDetector.handleTouchEvent(event)

    return when (swipeDirection) {
      UP, DOWN -> {
        if (!wasScaled && viewPager.isIdle) {
          swipeDismissHandler.onTouch(rootContainer, event)
        } else true
      }
      LEFT, RIGHT -> {
        viewPager.dispatchTouchEvent(event)
      }
      else -> true
    }
  }

  private fun handleUpDownEvent(event: MotionEvent) {
    if (event.action == MotionEvent.ACTION_UP) {
      handleEventActionUp(event)
    }

    if (event.action == MotionEvent.ACTION_DOWN) {
      handleEventActionDown(event)
    }

    scaleDetector.onTouchEvent(event)
    gestureDetector.onTouchEvent(event)
  }

  private fun handleEventActionDown(event: MotionEvent) {
    swipeDirection = null
    wasScaled = false
    viewPager.dispatchTouchEvent(event)

    swipeDismissHandler.onTouch(rootContainer, event)
    isOverlayWasClicked = dispatchOverlayTouch(event)
  }

  private fun handleEventActionUp(event: MotionEvent) {
    wasDoubleTapped = false
    swipeDismissHandler.onTouch(rootContainer, event)
    viewPager.dispatchTouchEvent(event)
    isOverlayWasClicked = dispatchOverlayTouch(event)
  }

  private fun handleSingleTap(event: MotionEvent, isOverlayWasClicked: Boolean) {
    if (overlayView != null && !isOverlayWasClicked) {
      onSingleTap?.invoke(event)
      super.dispatchTouchEvent(event)
    }
  }

  private fun handleSwipeViewMove(translationY: Float, translationLimit: Int) {
    val alpha = calculateTranslationAlpha(translationY, translationLimit)
    backgroundView.alpha = alpha
    onViewSwipe?.invoke(translationY, translationLimit)
  }

  private fun dispatchOverlayTouch(event: MotionEvent): Boolean {
    return overlayView.isVisible && overlayView?.dispatchTouchEvent(event) == true
  }

  private fun calculateTranslationAlpha(translationY: Float, translationLimit: Int): Float = 1.0f - 1.0f / translationLimit.toFloat() / 4f * Math.abs(translationY)
  private fun createSwipeDirectionDetector() = SwipeDirectionDetector(context) { swipeDirection = it }
  private fun createGestureDetector() = GestureDetectorCompat(context, SimpleOnGestureListener(
    onSingleTap = {
      if (viewPager.isIdle) {
        handleSingleTap(it, isOverlayWasClicked)
      }
      false
    },
    onDoubleTap = {
      wasDoubleTapped = !isScaled
      false
    }
  ))

  private fun createScaleGestureDetector() = ScaleGestureDetector(context, ScaleGestureDetector.SimpleOnScaleGestureListener())
  private fun createSwipeToDismissHandler(): SwipeToDismissHandler = SwipeToDismissHandler(
    swipeView = dismissContainer,
    shouldAnimateDismiss = { shouldDismissToBottom },
    onDismiss = { animateClose() },
    onSwipeViewMove = ::handleSwipeViewMove)

  private fun createTransitionImageAnimator(transitionImageView: View?) = TransitionImageAnimator(
    externalImage = transitionImageView,
    internalImage = this.transitionImageView,
    internalImageContainer = this.transitionImageContainer)
}

//保存view
fun View.saveView(context: Context, fileName: String): Boolean {
  val contentValues = ContentValues()
  contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
  contentValues.put(MediaStore.Images.Media.DESCRIPTION, fileName)
  contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
  val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
  val outputStream: OutputStream? = context.contentResolver.openOutputStream(uri!!)
  try {
    val bitmap = this.drawToBitmap()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    outputStream!!.close()
    context.toast("已保存到相册")
  } catch (e: Exception) {
    e.printStackTrace()
    return false
  }
  return true
}

//view附加长按事件
fun View.attachLongClick(context: Context = this.context, savedName: String = "888") {
  this.setOnLongClickListener {
    val view: View = this
    context.alert {
      customView {
        verticalLayout {
          //标题
          toolbar {
            lparams(width = matchParent, height = wrapContent)
            title = "是否保存此图片"
          }
          negativeButton("否") {}
          positiveButton("是") {
            view.saveView(context, savedName)
          }
        }
      }
    }.show()
    true
  }
}
