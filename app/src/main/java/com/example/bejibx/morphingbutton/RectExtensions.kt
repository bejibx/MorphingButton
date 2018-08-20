package com.example.bejibx.morphingbutton

import android.graphics.PointF
import android.graphics.RectF

fun RectF.equalsWithInset(rect: RectF, inset: Float) =
    left + inset == rect.left &&
            top + inset == rect.top &&
            right - inset == rect.right &&
            bottom - inset == rect.bottom

val RectF.topLeftCorner get() = PointF(left, top)

val RectF.topRightCorner get() = PointF(right, top)

val RectF.bottomLeftCorner get() = PointF(left, bottom)

val RectF.bottomRightCorner get() = PointF(right, bottom)