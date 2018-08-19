package com.example.bejibx.morphingbutton

import kotlin.math.abs
import kotlin.math.hypot

fun lineLength(x1: Float, y1: Float, x2: Float, y2: Float) =
    hypot(abs(x1 - x2), abs(y1 - y2))