package com.example.testappforcitymobil

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toRect
import kotlin.math.*

class CarOnMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var shouldShowHintLine: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    private var carAspectRatio: Float = 0.5f

    var carImage: Drawable? = null
        set(value) {
            field = value
            value?.let {
                carAspectRatio = (it.intrinsicHeight.toFloat() / it.intrinsicWidth.toFloat())
            }
        }

    private var carWidth: Float = 40f
        set(value) {
            field = value
            carHeight = value * carAspectRatio
            invalidate()
        }

    var isCarWidthSetup: Boolean = false

    private var carHeight: Float = 20f

    var radius: Float = 100F


    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.CarOnMapView, 0, 0).apply {
            try {
                shouldShowHintLine = getBoolean(R.styleable.CarOnMapView_showHintLine, false)
                carImage = getDrawable(R.styleable.CarOnMapView_carImage)
                if (hasValue(R.styleable.CarOnMapView_carWidth)) {
                    carWidth = getDimension(R.styleable.CarOnMapView_carWidth, 40f)
                    isCarWidthSetup = true
                }
                radius = getDimension(R.styleable.CarOnMapView_turningRadius, 100f)
            } finally {
                recycle()
            }
        }

    }

    private var mWidth: Float = 100F
    private var mHeight: Float = 100F

    private var oldCarX: Float = 0F
    private var oldCarY: Float = 0F

    private var aimCarX: Float = 0F
    private var aimCarY: Float = 0F

    private var currCarX: Float = 0F
    private var currCarY: Float = 0F
    private var currCarAngle: Float = 0F

    private var wayPath: Path = Path()
    private var wayPathMeas: PathMeasure = PathMeasure(wayPath, false)
    private var wayPathLength: Float = wayPathMeas.length
    private val pos = FloatArray(2)
    private val tg = FloatArray(2)

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
        if (!isCarWidthSetup) carWidth = mWidth * 0.1f
        oldCarX = mWidth / 2
        oldCarY = mHeight / 2
        currCarX = oldCarX
        currCarY = oldCarY
    }

    val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 2000
        interpolator = AccelerateDecelerateInterpolator()

        addUpdateListener {

            if (wayPathMeas.getPosTan(wayPathLength * (it.animatedValue as Float), pos, tg)) {
                currCarX = pos[0]
                currCarY = pos[1]
                currCarAngle = atan2(tg[1], tg[0]).toDegrees()
                invalidate()
            }

        }

        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                oldCarX = currCarX
                oldCarY = currCarY
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_UP && !animator.isRunning) {
            aimCarX = event.x
            aimCarY = event.y
            if (calculatePath()) animator.start()
        }
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            it.save()
            it.rotate(currCarAngle, currCarX, currCarY)
            if (carImage != null) {
                carImage?.bounds = getCarRect(currCarX, currCarY)
                carImage?.draw(it)
            } else {
                it.drawRect(getCarRect(currCarX, currCarY), bluePaint)
                it.drawCircle(currCarX + carWidth/2, currCarY, carHeight/2, bluePaint)
            }

            it.restore()
            it.drawPath(wayPath, strokePaint)
        }
    }

    private fun getCarRect(centerX: Float, centerY: Float): Rect {
        return RectF(
            centerX - carWidth / 2,
            centerY - carHeight / 2,
            centerX + carWidth / 2,
            centerY + carHeight / 2
        ).toRect()
    }

    fun Float.toRadians(): Float {
        return Math.toRadians(this.toDouble()).toFloat()
    }

    fun Float.toDegrees(): Float {
        return Math.toDegrees(this.toDouble()).toFloat()
    }

    fun calculatePath(): Boolean {

        val circleSign = sign(aimCarY - oldCarY - tan(currCarAngle.toRadians()) * (aimCarX - oldCarX)) *
                sign(cos(currCarAngle.toRadians()))

        val rx = oldCarX + circleSign * radius * cos((PI / 2 + currCarAngle.toRadians())).toFloat()
        val ry = oldCarY + circleSign * radius * sin((PI / 2 + currCarAngle.toRadians())).toFloat()

        val startAngle = atan2(oldCarY - ry, oldCarX - rx).toDegrees()

        val c = sqrt((rx - aimCarX).pow(2) + (ry - aimCarY).pow(2))

        if (c < radius) {
            wayPath.reset()
            return false
        }

        val b = sqrt(c.pow(2) - radius.pow(2))
        val al = atan2(ry - aimCarY, rx - aimCarX)
        val beta = asin(radius / c)

        val cx = aimCarX + b * cos(al + beta * circleSign)
        val cy = aimCarY + b * sin(al + beta * circleSign)
        var endAngle = atan2(cy - ry, cx - rx).toDegrees()
        if ((endAngle - startAngle) * circleSign < 0) endAngle += (2 * PI * circleSign).toFloat().toDegrees()

        wayPath.reset()
        val rect = RectF(rx - radius, ry - radius, rx + radius, ry + radius)
        wayPath.addArc(rect, startAngle, endAngle - startAngle)
        wayPath.lineTo(aimCarX, aimCarY)
        wayPathMeas = PathMeasure(wayPath, false)
        wayPathLength = wayPathMeas.length
        return true
    }

}