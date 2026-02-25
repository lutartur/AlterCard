package com.altercard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class ColorPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val dp = resources.displayMetrics.density

    private var hue = 0f
    private var saturation = 1f
    private var value = 1f

    private val previewRect = RectF()
    private val hueRect = RectF()
    private val satRect = RectF()
    private val valRect = RectF()

    private val previewPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val huePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val satPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val valPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.WHITE
        strokeWidth = 2f * dp
    }
    private val thumbShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#44000000")
        strokeWidth = 3f * dp
    }

    private val barHeight = 24f * dp
    private val previewHeight = 32f * dp
    private val gap = 12f * dp
    private val thumbRadius = 10f * dp
    private val cornerRadius = 8f * dp

    // Track which bar is being dragged
    private var activeBar = -1

    var onColorChanged: ((Int) -> Unit)? = null

    fun setColor(color: Int) {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hue = hsv[0]
        saturation = hsv[1]
        value = hsv[2]
        invalidate()
    }

    fun getColor(): Int = Color.HSVToColor(floatArrayOf(hue, saturation, value))

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val pad = thumbRadius
        var top = pad

        previewRect.set(pad, top, w - pad, top + previewHeight)
        top += previewHeight + gap

        hueRect.set(pad, top, w - pad, top + barHeight)
        top += barHeight + gap

        satRect.set(pad, top, w - pad, top + barHeight)
        top += barHeight + gap

        valRect.set(pad, top, w - pad, top + barHeight)

        buildHueShader()
    }

    private fun buildHueShader() {
        if (hueRect.isEmpty) return
        val colors = IntArray(7) { i -> Color.HSVToColor(floatArrayOf(i * 60f, 1f, 1f)) }
        huePaint.shader = LinearGradient(
            hueRect.left, 0f, hueRect.right, 0f,
            colors, null, Shader.TileMode.CLAMP
        )
    }

    private fun buildSatShader() {
        if (satRect.isEmpty) return
        val pureColor = Color.HSVToColor(floatArrayOf(hue, 1f, value))
        val grayColor = Color.HSVToColor(floatArrayOf(hue, 0f, value))
        satPaint.shader = LinearGradient(
            satRect.left, 0f, satRect.right, 0f,
            grayColor, pureColor, Shader.TileMode.CLAMP
        )
    }

    private fun buildValShader() {
        if (valRect.isEmpty) return
        val fullColor = Color.HSVToColor(floatArrayOf(hue, saturation, 1f))
        valPaint.shader = LinearGradient(
            valRect.left, 0f, valRect.right, 0f,
            Color.BLACK, fullColor, Shader.TileMode.CLAMP
        )
    }

    override fun onDraw(canvas: Canvas) {
        buildSatShader()
        buildValShader()

        // Preview
        previewPaint.color = getColor()
        canvas.drawRoundRect(previewRect, cornerRadius, cornerRadius, previewPaint)

        // Hue bar + thumb
        canvas.drawRoundRect(hueRect, cornerRadius, cornerRadius, huePaint)
        drawThumb(canvas, hueRect.left + (hue / 360f) * hueRect.width(), hueRect.centerY())

        // Saturation bar + thumb
        canvas.drawRoundRect(satRect, cornerRadius, cornerRadius, satPaint)
        drawThumb(canvas, satRect.left + saturation * satRect.width(), satRect.centerY())

        // Brightness bar + thumb
        canvas.drawRoundRect(valRect, cornerRadius, cornerRadius, valPaint)
        drawThumb(canvas, valRect.left + value * valRect.width(), valRect.centerY())
    }

    private fun drawThumb(canvas: Canvas, x: Float, y: Float) {
        canvas.drawCircle(x, y, thumbRadius, thumbShadowPaint)
        canvas.drawCircle(x, y, thumbRadius, thumbPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                activeBar = when {
                    isNear(y, hueRect) -> 0
                    isNear(y, satRect) -> 1
                    isNear(y, valRect) -> 2
                    else -> -1
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> activeBar = -1
        }

        if (activeBar >= 0 && (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE)) {
            val rect = when (activeBar) { 0 -> hueRect; 1 -> satRect; else -> valRect }
            val fraction = ((x - rect.left) / rect.width()).coerceIn(0f, 1f)
            when (activeBar) {
                0 -> hue = fraction * 360f
                1 -> saturation = fraction
                2 -> value = fraction
            }
            invalidate()
            onColorChanged?.invoke(getColor())
        }

        return true
    }

    private fun isNear(y: Float, rect: RectF) = y in (rect.top - thumbRadius)..(rect.bottom + thumbRadius)

    override fun onMeasure(widthMeasureSpec: Int, @Suppress("UNUSED_PARAMETER") heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = (thumbRadius * 2 + previewHeight + gap + barHeight + gap + barHeight + gap + barHeight).toInt()
        setMeasuredDimension(width, height)
    }
}
