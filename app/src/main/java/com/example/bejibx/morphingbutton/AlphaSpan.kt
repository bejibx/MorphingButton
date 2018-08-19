package com.example.bejibx.morphingbutton

import android.text.TextPaint
import android.text.style.CharacterStyle
import android.text.style.UpdateAppearance

class AlphaSpan : CharacterStyle(), UpdateAppearance {

    var isEnabled = true

    var alpha = Alpha.OPAQUE_INT
        set(value) {
            require(value in (0..255)) {
                "Require value from 0 to 255 but actual value is $value!"
            }
            field = value
        }

    override fun updateDrawState(tp: TextPaint?) {
        if (isEnabled) {
            tp?.alpha = alpha
        }
    }
}