package com.example.bejibx.morphingbutton

import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.Drawable

class MorphingShapeDrawable(
    private val colors: ColorStateList = ColorStateList.valueOf(Color.RED),
    cornerRadius: Dimension = Dimension(2f),
    strokeWidth: Dimension = Dimension(2f),
    initialMorphPercent: Float = 1f,
    transitionMode: TransitionMode = TransitionMode.CLIP,
    fillShape: Boolean = true
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
    private val fillPaint = Paint()
    private val strokeRect = RectF()
    private val boundsRect = RectF()
    private val outerPath = Path()
    private val outerCornerRadii = FloatArray(8) { this.cornerRadius }
    private val innerPath = Path()
    private val hotSpot = PointF()

    private var currentColor = colors.defaultColor
    private var fillRadius = 0f
    private var currentRadius = 0f

    var transitionMode = transitionMode
        set(value) {
            field = value
            invalidateSelf()
        }

    var transitionPercent = initialMorphPercent
        set(value) {
            if (field != value) {
                field = value
                updateTransitionPath()
                invalidateSelf()
            }
        }

    var fillShape = fillShape
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
        fillPaint.apply {
            color = currentColor
            isAntiAlias = true
        }
    }

    fun setHotSpotPosition(x: Float, y: Float) {
        hotSpot.set(x, y)
        if (updateInnerPath()) {
            updateTransitionPath()
            invalidateSelf()
        }
    }

    override fun draw(canvas: Canvas?) {
        canvas?.apply {
            if (fillShape && transitionPercent == 0f) {
                canvas.drawRoundRect(boundsRect, cornerRadius, cornerRadius, fillPaint)
            } else {
                canvas.save()
                canvas.clipPath(outerPath)
                if (transitionMode == TransitionMode.FILL) {
                    applyFillTransition(canvas)
                } else {
                    applyClipTransition(canvas)
                }
                if (fillShape) {
                    canvas.drawColor(currentColor)
                }
                canvas.restore()
                drawRoundRect(strokeRect, cornerRadius, cornerRadius, strokePaint)
            }
        }
    }

    private fun applyFillTransition(canvas: Canvas) {
        if (currentRadius == 0f) {
            return
        }
        when (transitionPercent) {
            0f -> return
            1f -> canvas.drawColor(currentColor)
            else -> canvas.drawCircle(hotSpot.x, hotSpot.y, currentRadius, fillPaint)
        }
    }

    private fun updateTransitionPath() {
        if (fillRadius == 0f) {
            currentRadius = 0f
            return
        }
        currentRadius = mapFromRangeToRange(
            value = transitionPercent,
            sourceRangeStart = 0f,
            sourceRangeEnd = 1f,
            targetRangeStart = 0f,
            targetRangeEnd = fillRadius
        )
        innerPath.reset()
        innerPath.addCircle(hotSpot.x, hotSpot.y, currentRadius, Path.Direction.CW)
    }

    private fun applyClipTransition(canvas: Canvas) {
        if (transitionPercent > 0f && currentRadius > 0f) {
            canvas.clipOutPathCompat(innerPath)
        }
    }

    override fun onBoundsChange(bounds: Rect?) {
        super.onBoundsChange(bounds)
        if (bounds == null) {
            return
        }
        var isChanged = updateOuterPath(bounds)
        isChanged = if (updateInnerPath()) {
            updateTransitionPath()
            true
        } else {
            isChanged
        }
        if (isChanged) {
            invalidateSelf()
        }
    }

    private fun updateOuterPath(newBounds: Rect): Boolean {
        val stroke = strokePaint.strokeWidth
        val halfStroke = stroke / 2
        val newBoundsF = RectF(newBounds)
        if (boundsRect != newBoundsF) {
            boundsRect.set(newBoundsF)
            strokeRect.set(newBoundsF)
            strokeRect.inset(halfStroke, halfStroke)
            outerPath.reset()
            outerPath.addRoundRect(boundsRect, outerCornerRadii, Path.Direction.CW)
            return true
        }
        return false
    }

    private fun updateInnerPath(): Boolean {
        var isChanged = hotSpot.fitIn(boundsRect)

        val topLeftCorner = lineLength(hotSpot.x, hotSpot.y, boundsRect.left, boundsRect.top)
        val topRightCorner = lineLength(hotSpot.x, hotSpot.y, boundsRect.right, boundsRect.top)
        val bottomLeftCorner = lineLength(hotSpot.x, hotSpot.y, boundsRect.left, boundsRect.bottom)
        val bottomRightCorner =
            lineLength(hotSpot.x, hotSpot.y, boundsRect.right, boundsRect.bottom)
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
        fillPaint.color = currentColor
        return currentColor != oldColor
    }

    enum class TransitionMode { FILL, CLIP }
}