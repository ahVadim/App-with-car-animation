package com.example.testappforcitymobil

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.max

class CarOnMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var mWidth: Float = 100F
    var mHeight: Float = 100F
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
        currCarX = mWidth/2
        currCarY = mHeight/2
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            currCarX = it.x
            currCarY = it.y
            invalidate()
        }

        return true
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.drawCircle(currCarX, currCarY, 20F, bluePaint)
    }

}