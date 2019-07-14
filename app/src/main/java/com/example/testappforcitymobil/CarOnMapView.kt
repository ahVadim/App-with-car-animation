package com.example.testappforcitymobil

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
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

    var wayPath: Path = Path()
    var wayPathMeas: PathMeasure = PathMeasure(wayPath, false)
    var wayPathLength: Float = wayPathMeas.length
    val pos = FloatArray(2)
    val tg = FloatArray(2)

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
        duration = 2000
        interpolator = AccelerateDecelerateInterpolator()

        addUpdateListener {

            if (wayPathMeas.getPosTan(wayPathLength*(it.animatedValue as Float), pos, tg)){
                currCarX = pos[0]
                currCarY = pos[1]
                currCarAngle = atan2(tg[1],tg[0]).toDegrees()
                invalidate()
            }

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
            if (calculatePath()) animator.start()
        }
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.save()
        canvas?.rotate(currCarAngle, currCarX, currCarY)
        canvas?.drawRect(currCarX-20f, currCarY-10f, currCarX+20f, currCarY+10f, bluePaint)
        canvas?.drawCircle(currCarX+20f, currCarY, 5F, strokePaint)
        canvas?.restore()
        canvas?.drawPath(wayPath, strokePaint)
    }

    fun Float.toRadians(): Float {
        return Math.toRadians(this.toDouble()).toFloat()
    }

    fun Float.toDegrees(): Float {
        return Math.toDegrees(this.toDouble()).toFloat()
    }

    fun calculatePath(): Boolean{

        val circleSign = sign(aimCarY - oldCarY - tan(currCarAngle.toRadians())*(aimCarX - oldCarX))*
                sign(cos(currCarAngle.toRadians()))

        val rx = oldCarX + circleSign*radius* cos((PI/2 + currCarAngle.toRadians())).toFloat()
        val ry = oldCarY + circleSign*radius* sin((PI/2 + currCarAngle.toRadians())).toFloat()

        val startAngle = atan2(oldCarY - ry, oldCarX - rx).toDegrees()

        val c = sqrt((rx-aimCarX).pow(2) + (ry-aimCarY).pow(2))

        if (c < radius) {
            wayPath.reset()
            return false
        }

        val b = sqrt(c.pow(2) - radius.pow(2))
        val al = atan2(ry-aimCarY, rx - aimCarX)
        val beta = asin(radius/c)

        val cx = aimCarX + b* cos(al+beta*circleSign)
        val cy = aimCarY + b* sin(al+beta*circleSign)
        var endAngle = atan2(cy-ry, cx-rx).toDegrees()
        if((endAngle-startAngle)*circleSign < 0) endAngle += (2*PI*circleSign).toFloat().toDegrees()

        wayPath.reset()
        val rect = RectF(rx - radius, ry - radius, rx + radius, ry + radius)
        wayPath.addArc(rect, startAngle,endAngle-startAngle)
        wayPath.lineTo(aimCarX, aimCarY)
        wayPathMeas = PathMeasure(wayPath, false)
        wayPathLength = wayPathMeas.length
        return true
    }

}