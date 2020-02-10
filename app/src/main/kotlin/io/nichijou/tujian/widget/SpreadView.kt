package io.nichijou.tujian.widget

import android.content.*
import android.graphics.*
import android.util.*
import android.view.*
import io.nichijou.tujian.common.ext.*


class SpreadView(context: Context, attrs: AttributeSet) : View(context, attrs) {

  /**
   * 颜色
   */
  private var color = Color.BLUE
  /**
   * 速度
   */
  private var speed = 20
  /**
   * 圆圈之间最大间距
   */
  private var mGap = 10f
  /**
   * 是否填充
   */
  private var isFill = true
  // View的宽
  private var mWidth: Float = 0.toFloat()
  // View的高
  private var mHeight: Float = 0.toFloat()
  // 声波的圆圈集合
  private var circles = arrayListOf<Circle>()
  //画笔
  private var paint: Paint = Paint()

  init {
    init()
  }

  private fun init() {
    paint.color = color
    if (isFill) {
      paint.style = Paint.Style.FILL
    } else {
      paint.strokeWidth = context.dp2px(1f)
      paint.style = Paint.Style.STROKE
    }
    paint.strokeCap = Paint.Cap.ROUND
    paint.isAntiAlias = true

    //Circle circle = new Circle(0, 255);
    //circles.add(circle);

    mGap = context.dp2px(30f)

    // 设置View的圆为半透明
    setBackgroundColor(Color.TRANSPARENT)
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)

    val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)//宽的测量大小，模式
    val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)

    val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)//高的测量大小，模式
    val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)

    mWidth = widthSpecSize.toFloat()   //定义测量宽，高(不包含测量模式),并设置默认值，查看View#getDefaultSize可知
    mHeight = heightSpecSize.toFloat()

    //处理wrap_content的几种特殊情况
    if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
      mWidth = 200f  //单位是px
      mHeight = 200f
    } else if (widthSpecMode == MeasureSpec.AT_MOST) {
      //只要宽度布局参数为wrap_content， 宽度给固定值200dp
      mWidth = 200f
      mHeight = heightSpecSize.toFloat()
    } else if (heightSpecMode == MeasureSpec.AT_MOST) {
      mWidth = widthSpecSize.toFloat()
      mHeight = 200f
    }

    //给两个字段设置值，完成最终测量
    setMeasuredDimension(mWidth.toInt(), mHeight.toInt())
  }

  override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    super.onLayout(changed, left, top, right, bottom)
    //初始化圆的个数
    val number = mWidth / 2 / mGap
    circles.add(Circle(0, 255))
    var circle: Circle
    for (i in 1..number.toInt()) {
      circle = Circle((mGap * i).toInt(), (255 - mGap * i * (255 / (mWidth.toInt() / 2))).toInt())
      circles.add(circle)
    }
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)

    //drawCircles(canvas);
    drawSpecialCircles(canvas)
  }

  private fun drawSpecialCircles(canvas: Canvas) {
    canvas.save()

    // 处理每个圆的宽度和透明度
    for (i in circles.indices) {
      val c = circles[i]
      paint.alpha = c.alpha// （透明）0-255（不透明）
      canvas.drawCircle(mWidth / 2, mHeight / 2, c.width - paint.strokeWidth, paint)

      if (c.alpha == 0) {
        //到最后一圈后,降透明度降为0，并逐渐缩小至最小的圆
        if (c.width > mGap) {
          c.width--
          c.alpha = 0
        } else {
          // 为了防止抖动，缩小至最小的圆宽度并且为透明的就删除
          circles.removeAt(i)
        }
      } else {
        // 改变透明度
        val alpha = 255 - c.width * (255 / (mWidth.toDouble() / 2))
        c.alpha = alpha.toInt()
        // 宽度逐渐增加
        c.width++
      }
    }

    // 添加圆圈
    if (circles.size > 0) {
      // 控制接下来出来的圆的间距
      if (circles[circles.size - 1].alpha != 0 && circles[circles.size - 1].width > mGap) {
        circles.add(Circle(0, 255))
      }
    }

    postInvalidateDelayed(speed.toLong())

    canvas.restore()
  }

  private fun drawCircles(canvas: Canvas) {
    canvas.save()

    // 处理每个圆的宽度和透明度
    for (i in circles.indices) {
      val c = circles[i]
      paint.alpha = c.alpha// （透明）0-255（不透明）
      canvas.drawCircle(mWidth / 2, mHeight / 2, c.width - paint.strokeWidth, paint)

      if (c.alpha == 0) {
        //到最后一圈后,降透明度降为0，并逐渐缩小至最小的圆
        if (c.width > mGap) {
          c.width--
          c.alpha = 0
        } else {
          // 为了防止抖动，缩小至最小的圆宽度并且为透明的就删除
          circles.removeAt(i)
        }
      } else {
        // 改变透明度
        val alpha = 255 - c.width * (255 / (mWidth.toDouble() / 2))
        c.alpha = alpha.toInt()
        // 宽度逐渐增加
        c.width++
      }
    }

    // 添加圆圈
    if (circles.size > 0) {
      // 控制第二个圆出来的间距
      if (circles[circles.size - 1].width > mGap) {
        circles.add(Circle(0, 255))
      }
    }

    postInvalidateDelayed(speed.toLong())

    canvas.restore()
  }

  class Circle(var width: Int, var alpha: Int)
}
