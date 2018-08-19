package com.example.bejibx.morphingbutton

import kotlin.math.max
import kotlin.math.min

fun mapFromRangeToRange(
    value: Float,
    sourceRangeStart: Float,
    sourceRangeEnd: Float,
    targetRangeStart: Float,
    targetRangeEnd: Float
): Float {
    require(sourceRangeStart != sourceRangeEnd) { "Source range is empty!" }
    require(targetRangeStart != targetRangeEnd) { "Target range is empty!" }
    require(value in (sourceRangeStart..sourceRangeEnd)) {
        "Value should be inside source range, but actual value is $value while range is" +
                " [$sourceRangeStart..$sourceRangeEnd]!"
    }
    return (value - sourceRangeStart) * (targetRangeEnd - targetRangeStart) / (sourceRangeEnd - sourceRangeStart) + targetRangeStart
}

fun maxOf(one: Float, two: Float, three: Float, four: Float) = max(max(max(one, two), three), four)

fun normalize(value: Float, fromInclusive: Float, toInclusive: Float) =
    min(max(value, fromInclusive), toInclusive)