package com.example.bejibx.morphingbutton

import android.graphics.RectF

fun RectF.equalsWithInset(rect: RectF, inset: Float) =
    left + inset == rect.left &&
            top + inset == rect.top &&
            right - inset == rect.right &&
            bottom - inset == rect.bottom