package com.example.bejibx.morphingbutton

import android.graphics.PointF
import kotlin.math.abs
import kotlin.math.hypot

fun lineLength(x1: Float, y1: Float, x2: Float, y2: Float) =
    hypot(abs(x1 - x2), abs(y1 - y2))

fun lineLength(point1: PointF, point2: PointF) = lineLength(point1.x, point1.y, point2.x, point2.y)

