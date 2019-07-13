package com.example.testappforcitymobil

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.view.View
import kotlin.math.max

class CarOnMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var mWidth: Int = 100
    var mHeight: Int = 100

    val bluePaint = Paint().apply {
        color = Color.BLUE
        isAntiAlias = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)

        mWidth = max(widthSpecSize, suggestedMinimumWidth)
        mHeight = max(heightSpecSize, suggestedMinimumHeight)

        setMeasuredDimension(mWidth, mHeight)

    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.drawCircle(width/2.toFloat(), height/2.toFloat(), 20F, bluePaint)
    }

}