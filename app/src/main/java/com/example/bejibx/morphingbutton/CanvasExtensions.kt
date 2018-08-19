package com.example.bejibx.morphingbutton

import android.graphics.Canvas
import android.graphics.Path
import android.graphics.Region
import android.os.Build

fun Canvas.clipOutPathCompat(path: Path) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        clipOutPath(path)
    } else {
        @Suppress("DEPRECATION")
        clipPath(path, Region.Op.DIFFERENCE)
    }
}