package com.example.bejibx.morphingbutton

import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.Drawable

class MorphingShapeDrawable(
    private val colors: ColorStateList = ColorStateList.valueOf(Color.RED),
    cornerRadius: Dimension = Dimension(2f),
    strokeWidth: Dimension = Dimension(2f),
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

    override fun setHotspot(x: Float, y: Float) {
        val (oldX, oldY) = hotSpot
        val isHotSpotChanged = if (oldX != x || oldY != y) {
            hotSpot.set(x, y)
            hotSpot.fitIn(boundsRect)
            hotSpot.x != oldX || hotSpot.y != oldY
        } else {
            false
        }
        if (isHotSpotChanged) {
            invalidateMaxTransitionRadius()
            invalidateTransition()
            invalidateSelf()
        }
    }

    override fun draw(canvas: Canvas?) {
        canvas?.apply {
            if (fillShape && level == 0) {
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
        when (level) {
            0 -> return
            1 -> canvas.drawColor(currentColor)
            else -> canvas.drawCircle(hotSpot.x, hotSpot.y, currentRadius, fillPaint)
        }
    }

    private fun invalidateTransition(): Boolean {
        if (fillRadius == 0f) {
            currentRadius = 0f
            return false
        }
        val radius = mapFromRangeToRange(
            value = level.toFloat(),
            sourceRangeStart = Drawables.MIN_LEVEL.toFloat(),
            sourceRangeEnd = Drawables.MAX_LEVEL.toFloat(),
            targetRangeStart = 0f,
            targetRangeEnd = fillRadius
        )
        return if (currentRadius != radius) {
            currentRadius = radius
            innerPath.reset()
            innerPath.addCircle(hotSpot.x, hotSpot.y, currentRadius, Path.Direction.CW)
            true
        } else {
            false
        }
    }

    private fun applyClipTransition(canvas: Canvas) {
        if (level > 0f && currentRadius > 0f) {
            canvas.clipOutPathCompat(innerPath)
        }
    }

    override fun onBoundsChange(bounds: Rect?) {
        super.onBoundsChange(bounds)
        if (bounds == null) {
            return
        }
        if (invalidateBounds(bounds)) {
            hotSpot.fitIn(boundsRect)
            invalidateMaxTransitionRadius()
            invalidateTransition()
        }
    }

    private fun invalidateBounds(newBounds: Rect): Boolean {
        val newBoundsF = RectF(newBounds)
        if (boundsRect != newBoundsF) {
            boundsRect.set(newBoundsF)
            strokeRect.set(newBoundsF)
            val halfStroke = strokePaint.strokeWidth / 2
            strokeRect.inset(halfStroke, halfStroke)
            outerPath.reset()
            outerPath.addRoundRect(boundsRect, outerCornerRadii, Path.Direction.CW)
            return true
        }
        return false
    }

    private fun invalidateMaxTransitionRadius() {
        val lineToTopLeft = lineLength(hotSpot, boundsRect.topLeftCorner)
        val lineToTopRight = lineLength(hotSpot, boundsRect.topRightCorner)
        val lineToBottomLeft = lineLength(hotSpot, boundsRect.bottomLeftCorner)
        val lineToBottomRight = lineLength(hotSpot, boundsRect.bottomRightCorner)
        val newRadius = maxOf(lineToTopLeft, lineToTopRight, lineToBottomLeft, lineToBottomRight)
        fillRadius = newRadius
    }

    override fun setAlpha(alpha: Int) {
        // no-op
    }

    override fun getOpacity() = PixelFormat.TRANSLUCENT

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

    override fun onLevelChange(level: Int): Boolean {
        checkLevel(level)
        return invalidateTransition()
    }

    enum class TransitionMode { FILL, CLIP }
}