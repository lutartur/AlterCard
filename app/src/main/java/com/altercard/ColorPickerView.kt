package com.altercard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ComposeShader
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
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

    private val svRect = RectF()
    private val hueRect = RectF()
    private val previewRect = RectF()

    private val svPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val huePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val previewPaint = Paint(Paint.ANTI_ALIAS_FLAG)
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

    private val hueBarHeight = 24f * dp
    private val previewHeight = 32f * dp
    private val gap = 12f * dp
    private val thumbRadius = 10f * dp
    private val cornerRadius = 8f * dp

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
        val totalHeight = h - pad * 2
        val previewTop = pad
        val previewBottom = previewTop + previewHeight
        val hueTop = previewBottom + gap
        val hueBottom = hueTop + hueBarHeight
        val svTop = hueBottom + gap
        val svBottom = pad + totalHeight

        previewRect.set(pad, previewTop, w - pad, previewBottom)
        hueRect.set(pad, hueTop, w - pad, hueBottom)
        svRect.set(pad, svTop, w - pad, svBottom)

        buildHueShader()
    }

    private fun buildHueShader() {
        if (hueRect.isEmpty) return
        val colors = IntArray(7) { i ->
            Color.HSVToColor(floatArrayOf(i * 60f, 1f, 1f))
        }
        huePaint.shader = LinearGradient(
            hueRect.left, 0f, hueRect.right, 0f,
            colors, null, Shader.TileMode.CLAMP
        )
    }

    private fun buildSvShader() {
        if (svRect.isEmpty) return
        val pureColor = Color.HSVToColor(floatArrayOf(hue, 1f, 1f))
        val satShader = LinearGradient(
            svRect.left, 0f, svRect.right, 0f,
            Color.WHITE, pureColor, Shader.TileMode.CLAMP
        )
        val valShader = LinearGradient(
            0f, svRect.top, 0f, svRect.bottom,
            Color.TRANSPARENT, Color.BLACK, Shader.TileMode.CLAMP
        )
        svPaint.shader = ComposeShader(valShader, satShader, PorterDuff.Mode.MULTIPLY)
    }

    override fun onDraw(canvas: Canvas) {
        buildSvShader()

        // Preview
        previewPaint.color = getColor()
        canvas.drawRoundRect(previewRect, cornerRadius, cornerRadius, previewPaint)

        // Hue bar
        canvas.drawRoundRect(hueRect, cornerRadius, cornerRadius, huePaint)

        // SV square
        canvas.drawRoundRect(svRect, cornerRadius, cornerRadius, svPaint)

        // Hue thumb
        val hueThumbX = hueRect.left + (hue / 360f) * hueRect.width()
        val hueThumbY = hueRect.centerY()
        canvas.drawCircle(hueThumbX, hueThumbY, thumbRadius, thumbShadowPaint)
        canvas.drawCircle(hueThumbX, hueThumbY, thumbRadius, thumbPaint)

        // SV thumb
        val svThumbX = svRect.left + saturation * svRect.width()
        val svThumbY = svRect.top + (1f - value) * svRect.height()
        canvas.drawCircle(svThumbX, svThumbY, thumbRadius, thumbShadowPaint)
        canvas.drawCircle(svThumbX, svThumbY, thumbRadius, thumbPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                when {
                    hueRect.contains(x, y) || (event.action == MotionEvent.ACTION_MOVE && isInHueZone(y)) -> {
                        hue = ((x - hueRect.left) / hueRect.width()).coerceIn(0f, 1f) * 360f
                        invalidate()
                        onColorChanged?.invoke(getColor())
                        return true
                    }
                    svRect.contains(x, y) || (event.action == MotionEvent.ACTION_MOVE && isInSvZone(y)) -> {
                        saturation = ((x - svRect.left) / svRect.width()).coerceIn(0f, 1f)
                        value = 1f - ((y - svRect.top) / svRect.height()).coerceIn(0f, 1f)
                        invalidate()
                        onColorChanged?.invoke(getColor())
                        return true
                    }
                }
            }
        }
        return true
    }

    private fun isInHueZone(y: Float) = y in (hueRect.top - thumbRadius)..(hueRect.bottom + thumbRadius)
    private fun isInSvZone(y: Float) = y in (svRect.top - thumbRadius)..(svRect.bottom + thumbRadius)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val svSize = width - thumbRadius * 2
        val height = (thumbRadius * 2 + previewHeight + gap + hueBarHeight + gap + svSize).toInt()
        setMeasuredDimension(width, height)
    }
}
