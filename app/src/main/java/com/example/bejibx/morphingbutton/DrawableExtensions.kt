package com.example.bejibx.morphingbutton

import android.graphics.drawable.Drawable
import com.example.bejibx.morphingbutton.Drawables.MAX_LEVEL
import com.example.bejibx.morphingbutton.Drawables.MIN_LEVEL

@Suppress("unused")
fun Drawable.checkLevel(level: Int) {
    require(level in (MIN_LEVEL..MAX_LEVEL)) {
        "Level value required to be in range [$MIN_LEVEL..$MAX_LEVEL] but actual value is $level!"
    }
}

object Drawables {
    const val MIN_LEVEL = 0
    const val MAX_LEVEL = 10_000
}