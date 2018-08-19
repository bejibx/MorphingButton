package com.example.bejibx.morphingbutton

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v7.widget.AppCompatButton
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import android.view.MotionEvent

class MorphingButton(
    context: Context,
    attrs: AttributeSet?
) : AppCompatButton(context, attrs) {

    private val textAlphaSpan = AlphaSpan()
    private val morphingDrawable: MorphingShapeDrawable
    private val styleFilled: Style
    private val styleOutlined: Style

    private var currentStyleType = StyleType.FILLED
    private var currentAnimation: Animator? = null

    init {
        setSpannableFactory(object : Spannable.Factory() {
            override fun newSpannable(source: CharSequence?): Spannable {
                return if (source == null)
                    SpannableStringBuilder()
                else
                    SpannableStringBuilder(source).apply {
                        setSpan(textAlphaSpan, 0, length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                    }
            }
        })

        val attributes = readAttributes(attrs)
        setTextColor(attributes.textColor)
        morphingDrawable = MorphingShapeDrawable(
            colors = attributes.backgroundColor,
            strokeWidth = Dimension(attributes.strokeWidthPx, DimensionUnit.PX),
            cornerRadius = Dimension(attributes.cornerRadiusPx, DimensionUnit.PX),
            initialMorphPercent = 0f,
            transitionMode = MorphingShapeDrawable.TransitionMode.CLIP,
            fillShape = true
        )
        background = morphingDrawable
        styleFilled = Style(
            type = StyleType.FILLED,
            textColor = textColors,
            text = text,
            backgroundTransitionMode = MorphingShapeDrawable.TransitionMode.CLIP,
            isBackgroundFilled = true
        )
        styleOutlined = Style(
            type = StyleType.OUTLINED,
            textColor = attributes.textColorOutlined,
            text = attributes.textOutlined,
            backgroundTransitionMode = MorphingShapeDrawable.TransitionMode.FILL,
            isBackgroundFilled = false
        )
        switchToFilled()
    }

    private fun readAttributes(attrs: AttributeSet?): Attributes {
        val attributes = Attributes(
            textOutlined = text
        )
        if (attrs != null) {
            val a = context.obtainStyledAttributes(
                attrs, R.styleable.MorphingButton,
                R.attr.morphingButtonStyle, R.style.MorphingButton
            )
            a.getColorStateList(R.styleable.MorphingButton_backgroundColor)?.also {
                attributes.backgroundColor = it
            }
            attributes.strokeWidthPx = a.getDimension(
                R.styleable.MorphingButton_strokeWidth, attributes.strokeWidthPx
            )
            attributes.cornerRadiusPx = a.getDimension(
                R.styleable.MorphingButton_cornerRadius, attributes.cornerRadiusPx
            )
            attributes.textOutlined = a.getText(
                R.styleable.MorphingButton_textOutlined
            ) ?: attributes.textOutlined
            attributes.textColorOutlined = a.getColorStateList(
                R.styleable.MorphingButton_textColorOutlined
            ) ?: attributes.textColorOutlined
            attributes.textColor = a.getColorStateList(
                R.styleable.MorphingButton_android_textColor
            ) ?: attributes.textColor
            a.recycle()
        }
        return attributes
    }

    fun toggleStateAnimated() {
        if (currentStyleType == StyleType.FILLED) {
            animateToOutlined()
        } else {
            animateToFilled()
        }
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, BufferType.SPANNABLE)
    }

    fun switchToOutlined() {
        applyStyle(styleOutlined)
    }

    fun switchToFilled() {
        applyStyle(styleFilled)
    }

    private fun applyStyle(style: Style) {
        endAnimationIfAny()
        if (currentStyleType == style.type) {
            return
        }
        this.currentStyleType = style.type
        text = style.text
        setTextColor(style.textColor)
        morphingDrawable.transitionMode = style.backgroundTransitionMode
        morphingDrawable.transitionPercent = 0f
        morphingDrawable.fillShape = style.isBackgroundFilled
    }

    private fun animateToFilled() {
        applyStyleAnimated(styleFilled)
    }

    private fun animateToOutlined() {
        applyStyleAnimated(styleOutlined)
    }

    private fun applyStyleAnimated(style: Style) {
        if (currentStyleType == style.type) {
            if (!endAnimationIfAny()) {
                return
            }
        }
        val button = this
        val animation = buildAnimation {
            interpolator = FastOutSlowInInterpolator()

            ofFloat(0f, 1f) {
                duration = 500
                addUpdateListener {
                    morphingDrawable.transitionPercent = it.animatedValue as Float
                }
            }

            sequentially {
                duration = 350

                intAlphaFadeIn {
                    addUpdateListener {
                        textAlphaSpan.alpha = it.animatedValue as Int
                        button.invalidate()
                    }
                    doOnEnd {
                        button.apply {
                            text = style.text
                            setTextColor(style.textColor)
                        }
                    }
                }
                intAlphaFadeOut {
                    addUpdateListener {
                        textAlphaSpan.alpha = it.animatedValue as Int
                        button.invalidate()
                    }
                }
            }

            addListener(
                onStart = {
                    textAlphaSpan.isEnabled = true
                },
                onEnd = {
                    button.apply {
                        currentStyleType = style.type
                        currentAnimation = null
                    }
                    textAlphaSpan.isEnabled = false
                    morphingDrawable.apply {
                        fillShape = style.isBackgroundFilled
                        transitionMode = style.backgroundTransitionMode
                        transitionPercent = 0f
                    }
                }
            )
        }
        currentAnimation = animation
        animation.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        endAnimationIfAny()
    }

    private fun endAnimationIfAny(): Boolean {
        val animation = currentAnimation
        if (animation != null) {
            animation.end()
            currentAnimation = null
            return true
        }
        return false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            if (event.actionMasked == MotionEvent.ACTION_UP) {
                morphingDrawable.setHotSpotPosition(event.x, event.y)
            }
        }
        return super.onTouchEvent(event)
    }

    private enum class StyleType {
        FILLED, OUTLINED
    }

    override fun onSaveInstanceState(): Parcelable {
        val styleType = when {
            currentAnimation == null -> currentStyleType
            currentStyleType == StyleType.FILLED -> StyleType.OUTLINED
            else -> StyleType.FILLED
        }
        return SavedState(styleType, super.onSaveInstanceState())
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            val styleType = state.styleType
            if (styleType == StyleType.FILLED)
                switchToFilled()
            else
                switchToOutlined()
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    private data class Style(
        val type: StyleType,
        val textColor: ColorStateList,
        val text: CharSequence,
        val backgroundTransitionMode: MorphingShapeDrawable.TransitionMode,
        val isBackgroundFilled: Boolean
    )

    private data class Attributes(
        var backgroundColor: ColorStateList = ColorStateList.valueOf(Color.RED),
        var strokeWidthPx: Float = DimensionUnit.DP.toPx(2f),
        var cornerRadiusPx: Float = DimensionUnit.DP.toPx(2f),
        var textOutlined: CharSequence = "",
        var textColorOutlined: ColorStateList = ColorStateList.valueOf(Color.RED),
        var textColor: ColorStateList = ColorStateList.valueOf(Color.WHITE)
    )

    private class SavedState : BaseSavedState {

        val styleType: StyleType?

        constructor(
            styleType: StyleType,
            superState: Parcelable?
        ) : super(superState) {
            this.styleType = styleType
        }

        constructor(parcel: Parcel?) : super(parcel) {
            this.styleType = parcel?.readSerializable() as StyleType?
        }

        override fun writeToParcel(out: Parcel?, flags: Int) {
            super.writeToParcel(out, flags)
            out?.writeSerializable(styleType)
        }

        companion object {

            @Suppress("unused")
            @JvmStatic
            val CREATOR = object : Parcelable.Creator<SavedState> {

                override fun createFromParcel(source: Parcel?) = SavedState(source)

                override fun newArray(size: Int) = arrayOfNulls<SavedState>(size)
            }
        }
    }
}