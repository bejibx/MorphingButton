package com.example.bejibx.morphingbutton

import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.Drawable

class MorphingShapeDrawable(
    private val colors: ColorStateList = ColorStateList.valueOf(Color.RED),
    cornerRadius: Dimension = Dimension(2f),
    strokeWidth: Dimension = Dimension(2f),
    initialMorphPercent: Float = 1f
) : Drawable() {

    init {
        require(cornerRadius.value >= 0) {
            "Corner radius should be greater or equal to zero but actual value is $cornerRadius!"
        }
        require(strokeWidth.value > 0) {
            "Stroke width should be greater than zero but actual value is $strokeWidth!"
        }
        require(initialMorphPercent in (0f..1f)) {
            "Morph percent value should be in range [0..1] but actual value is " +
                    "$initialMorphPercent!"
        }
    }

    private val cornerRadius = cornerRadius.px
    private val strokePaint = Paint()
    private val outerRect = RectF()
    private val boundsRect = RectF()
    private val outerPath = Path()
    private val outerCornerRadii = FloatArray(8) { this.cornerRadius }
    private val innerPath = Path()
    private val hotSpot = PointF()

    private var currentColor = colors.defaultColor
    private var fillRadius = 0f

    var morphPercent = initialMorphPercent
        set(value) {
            if (field != value) {
                field = value
                invalidateSelf()
            }
        }

    init {
        strokePaint.apply {
            color = currentColor
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth.px
            isAntiAlias = true
        }
    }

    fun setHotSpotPosition(x: Float, y: Float) {
        hotSpot.set(x, y)
        if (updateInnerPath()) {
            invalidateSelf()
        }
    }

    override fun draw(canvas: Canvas?) {
        canvas?.apply {
            drawFill(canvas)
            drawRoundRect(outerRect, cornerRadius, cornerRadius, strokePaint)
        }
    }

    private fun drawFill(canvas: Canvas) {
        if (morphPercent <= 0) {
            return
        }
        canvas.save()
        canvas.clipPath(outerPath)
        if (morphPercent < 1) {
            val radius = mapFromRangeToRange(
                value = morphPercent,
                sourceRangeStart = 0f,
                sourceRangeEnd = 1f,
                targetRangeStart = fillRadius,
                targetRangeEnd = 0f
            )
            innerPath.reset()
            innerPath.addCircle(hotSpot.x, hotSpot.y, radius, Path.Direction.CW)
            canvas.clipOutPathCompat(innerPath)
        }
        canvas.drawColor(currentColor)
        canvas.restore()
    }

    override fun onBoundsChange(bounds: Rect?) {
        super.onBoundsChange(bounds)
        if (bounds == null) {
            return
        }
        if (updateOuterPath(bounds) || updateInnerPath()) {
            invalidateSelf()
        }
    }

    private fun updateOuterPath(newBounds: Rect): Boolean {
        val inset = strokePaint.strokeWidth / 2
        boundsRect.set(newBounds)
        if (!outerRect.equalsWithInset(boundsRect, inset)) {
            outerRect.set(newBounds)
            outerRect.inset(inset, inset)
            outerPath.reset()
            outerPath.addRoundRect(outerRect, outerCornerRadii, Path.Direction.CW)
            return true
        }
        return false
    }

    private fun updateInnerPath(): Boolean {
        var isChanged = hotSpot.normalize(outerRect)

        val topLeftCorner = lineLength(hotSpot.x, hotSpot.y, outerRect.left, outerRect.top)
        val topRightCorner = lineLength(hotSpot.x, hotSpot.y, outerRect.right, outerRect.top)
        val bottomLeftCorner = lineLength(hotSpot.x, hotSpot.y, outerRect.left, outerRect.bottom)
        val bottomRightCorner = lineLength(hotSpot.x, hotSpot.y, outerRect.right, outerRect.bottom)
        val newRadius = maxOf(topLeftCorner, topRightCorner, bottomLeftCorner, bottomRightCorner)
        if (newRadius != fillRadius) {
            fillRadius = newRadius
            isChanged = true
        }

        return isChanged
    }

    override fun setAlpha(alpha: Int) {
        // no-op
    }

    override fun getOpacity() = PixelFormat.OPAQUE

    override fun setColorFilter(colorFilter: ColorFilter?) {
        // no-op
    }

    override fun isStateful() = true

    override fun onStateChange(state: IntArray?): Boolean {
        val oldColor = currentColor
        currentColor = colors.getColorForState(state, oldColor)
        strokePaint.color = currentColor
        return currentColor != oldColor
    }
}