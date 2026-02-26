package com.altercard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.graphics.toColorInt
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class ScannerOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#99000000".toColorInt()
    }

    private val clearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 3f * resources.displayMetrics.density
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 14f * resources.displayMetrics.density
        textAlign = Paint.Align.CENTER
    }

    private val textBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }

    private val textBgRect = RectF()
    private val textBgCorner = 12f * resources.displayMetrics.density
    private val textPadH = 16f * resources.displayMetrics.density
    private val textPadV = 8f * resources.displayMetrics.density

    private val promptText = context.getString(R.string.scanner_prompt)
    private val frameRect = RectF()
    private val cornerRadius = 12f * resources.displayMetrics.density
    private val cornerLength = 6f * resources.displayMetrics.density

    private var textX = 0f
    private var textY = 0f

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val frameWidth = w * 0.8f
        val frameHeight = frameWidth * 0.85f
        val left = (w - frameWidth) / 2f
        val top = (h - frameHeight) / 2f
        frameRect.set(left, top, left + frameWidth, top + frameHeight)

        textX = w / 2f
        textY = top * 0.65f - (textPaint.ascent() + textPaint.descent()) / 2f
        val textHalfWidth = textPaint.measureText(promptText) / 2f
        textBgRect.set(
            textX - textHalfWidth - textPadH,
            textY + textPaint.ascent() - textPadV,
            textX + textHalfWidth + textPadH,
            textY + textPaint.descent() + textPadV
        )
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)

        canvas.drawRoundRect(frameRect, cornerRadius, cornerRadius, clearPaint)

        val l = frameRect.left
        val t = frameRect.top
        val r = frameRect.right
        val b = frameRect.bottom
        val cr = cornerRadius
        val cl = cornerLength

        // Top-left
        canvas.drawArc(l, t, l + 2 * cr, t + 2 * cr, 180f, 90f, false, borderPaint)
        canvas.drawLine(l + cr, t, l + cr + cl, t, borderPaint)
        canvas.drawLine(l, t + cr, l, t + cr + cl, borderPaint)

        // Top-right
        canvas.drawArc(r - 2 * cr, t, r, t + 2 * cr, 270f, 90f, false, borderPaint)
        canvas.drawLine(r - cr - cl, t, r - cr, t, borderPaint)
        canvas.drawLine(r, t + cr, r, t + cr + cl, borderPaint)

        // Bottom-right
        canvas.drawArc(r - 2 * cr, b - 2 * cr, r, b, 0f, 90f, false, borderPaint)
        canvas.drawLine(r - cr - cl, b, r - cr, b, borderPaint)
        canvas.drawLine(r, b - cr - cl, r, b - cr, borderPaint)

        // Bottom-left
        canvas.drawArc(l, b - 2 * cr, l + 2 * cr, b, 90f, 90f, false, borderPaint)
        canvas.drawLine(l + cr, b, l + cr + cl, b, borderPaint)
        canvas.drawLine(l, b - cr - cl, l, b - cr, borderPaint)

        canvas.drawRoundRect(textBgRect, textBgCorner, textBgCorner, textBgPaint)
        canvas.drawText(promptText, textX, textY, textPaint)
    }
}
