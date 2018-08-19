package com.example.bejibx.morphingbutton

import android.graphics.PointF
import android.graphics.RectF

fun PointF.fitIn(rect: RectF): Boolean {
    val normalizedX = normalize(x, rect.left, rect.right)
    val normalizedY = normalize(y, rect.top, rect.bottom)
    if (normalizedX != x || normalizedY != y) {
        x = normalizedX
        y = normalizedY
        return true
    }
    return false
}