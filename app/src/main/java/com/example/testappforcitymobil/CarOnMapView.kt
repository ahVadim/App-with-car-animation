package com.example.testappforcitymobil

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import kotlin.math.max

class CarOnMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var mWidth: Float = 100F
    var mHeight: Float = 100F

    var oldCarX: Float = 0F
    var oldCarY: Float = 0F

    var aimCarX: Float = 0F
    var aimCarY: Float = 0F

    var currCarX: Float = 0F
    var currCarY: Float = 0F

    val bluePaint = Paint().apply {
        color = Color.BLUE
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
            animator.start()
        }
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.drawCircle(currCarX, currCarY, 20F, bluePaint)
    }

}