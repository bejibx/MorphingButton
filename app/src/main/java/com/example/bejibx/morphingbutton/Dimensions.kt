package com.example.bejibx.morphingbutton

import android.content.res.Resources

private val scaledDensity get() = Resources.getSystem().displayMetrics.scaledDensity
private val density get() = Resources.getSystem().displayMetrics.density

data class Dimension(val value: Float = 0f, val unit: DimensionUnit = DimensionUnit.DP) {
    val px = unit.toPx(value)
}

enum class DimensionUnit {

    PX {
        override fun toDp(value: Float) = value / density
        override fun toSp(value: Float) = value / scaledDensity
        override fun toPx(value: Float) = value
    },

    DP {
        override fun toDp(value: Float) = value
        override fun toSp(value: Float) = PX.toSp(toPx(value))
        override fun toPx(value: Float) = value * density
    },

    SP {
        override fun toDp(value: Float) = PX.toDp(toPx(value))
        override fun toSp(value: Float) = value
        override fun toPx(value: Float) = value * scaledDensity
    };

    abstract fun toDp(value: Float): Float
    abstract fun toSp(value: Float): Float
    abstract fun toPx(value: Float): Float

    fun toIntDp(value: Float) = toDp(value).toInt()
    fun toIntSp(value: Float) = toSp(value).toInt()
    fun toIntPx(value: Float) = toPx(value).toInt()
}