package com.example.testappforcitymobil

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import kotlin.math.*

class CarOnMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var mWidth: Float = 100F
    var mHeight: Float = 100F

    var radius: Float = 100F

    var oldCarX: Float = 0F
    var oldCarY: Float = 0F

    var aimCarX: Float = 0F
    var aimCarY: Float = 0F

    var currCarX: Float = 0F
    var currCarY: Float = 0F
    var currCarAngle: Float = 0F

    var hintPath: Path = Path()

    val bluePaint = Paint().apply {
        color = Color.BLUE
        isAntiAlias = true
    }

    val strokePaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 5F
        isAntiAlias = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)

        val resultWidth = max(widthSpecSize, suggestedMinimumWidth)
        val resultHeight = max(heightSpecSize, suggestedMinimumHeight)

        setMeasuredDimension(resultWidth, resultHeight)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        mWidth = w.toFloat()
        mHeight = h.toFloat()
        oldCarX = mWidth/2
        oldCarY = mHeight/2
        currCarX = oldCarX
        currCarY = oldCarY
    }

    val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 1000
        interpolator = AccelerateDecelerateInterpolator()

        addUpdateListener {
            currCarX = oldCarX + (aimCarX - oldCarX)*(it.animatedValue as Float)
            currCarY = oldCarY + (aimCarY - oldCarY)*(it.animatedValue as Float)
            invalidate()
        }

        addListener(object: AnimatorListenerAdapter(){
            override fun onAnimationEnd(animation: Animator?) {
                oldCarX = currCarX
                oldCarY = currCarY
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_UP && !animator.isRunning){
            aimCarX = event.x
            aimCarY = event.y
//            animator.start()
            calculatePath()
        }
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.drawCircle(currCarX, currCarY, 20F, bluePaint)
        canvas?.drawPath(hintPath, strokePaint)
    }

    fun Float.toRadians(): Float {
        return Math.toRadians(this.toDouble()).toFloat()
    }

    fun Float.toDegrees(): Float {
        return Math.toDegrees(this.toDouble()).toFloat()
    }

    fun calculatePath(){

        currCarAngle+=15F

        val circleSign = sign(aimCarY - oldCarY - tan(currCarAngle.toRadians())*(aimCarX - oldCarX))*sign(cos(currCarAngle.toRadians()))

        val rx = oldCarX + circleSign*radius* cos((PI/2 + currCarAngle.toRadians())).toFloat()
        val ry = oldCarY + circleSign*radius* sin((PI/2 + currCarAngle.toRadians())).toFloat()

        val startAngle = atan2(oldCarY - ry, oldCarX - rx).toDegrees()

        val c = sqrt((rx-aimCarX).pow(2) + (ry-aimCarY).pow(2))

        if (c < radius) {
            hintPath.reset()
            return
        }

        val b = sqrt(c.pow(2) - radius.pow(2))
        val al = atan2(ry-aimCarY, rx - aimCarX)
        val beta = asin(radius/c)

        val cx = aimCarX + b* cos(al+beta*circleSign)
        val cy = aimCarY + b* sin(al+beta*circleSign)
        var endAngle = atan2(cy-ry, cx-rx).toDegrees()
        if((endAngle-startAngle)*circleSign < 0) endAngle += (2*PI*circleSign).toFloat().toDegrees()

        hintPath.reset()
        val rect = RectF(rx - radius, ry - radius, rx + radius, ry + radius)
        hintPath.addArc(rect, startAngle,endAngle-startAngle)
        hintPath.moveTo(cx, cy)
        hintPath.lineTo(aimCarX, aimCarY)
        invalidate()
    }

}