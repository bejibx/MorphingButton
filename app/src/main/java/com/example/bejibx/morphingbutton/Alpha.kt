package com.example.bejibx.morphingbutton

object Alpha {

    const val TRANSPARENT = 0f
    const val OPAQUE = 1f
    const val OPAQUE_INT = 255
    const val TRANSPARENT_INT = 0

    fun toInt(value: Float): Int {
        require(value in (TRANSPARENT..OPAQUE)) {
            "Expected value from range [$TRANSPARENT..$OPAQUE], but actual value is $value!"
        }
        return mapFromRangeToRange(
            value = value,
            sourceRangeStart = TRANSPARENT,
            sourceRangeEnd = OPAQUE,
            targetRangeStart = TRANSPARENT_INT.toFloat(),
            targetRangeEnd = OPAQUE_INT.toFloat()
        ).toInt()
    }
}