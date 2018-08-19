package com.example.bejibx.morphingbutton

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.view.animation.Interpolator

private interface AnimationBuilder {
    fun build(): Animator
}

private class AnimatorWrapper(private val animator: Animator) : AnimationBuilder {
    override fun build() = animator
}

class AnimationGroup(
    private val applyChildAnimations: (set: AnimatorSet, animators: List<Animator>) -> Unit
) : AnimationBuilder {

    private val animations = ArrayList<AnimationBuilder>()
    private val listeners = ArrayList<Animator.AnimatorListener>()
    private var _duration: Long? = null

    var interpolator: Interpolator? = null

    var duration = 300L
        set(value) {
            require(value >= 0) {
                "Duration should be greater or equal to zero, but actual value is $value!"
            }
            _duration = value
        }

    fun together(configureGroup: AnimationGroup.() -> Unit) {
        val group = playTogether()
        configureGroup(group)
        animations.add(group)
    }

    fun sequentially(configureGroup: AnimationGroup.() -> Unit) {
        val group = playSequentially()
        configureGroup(group)
        animations.add(group)
    }

    fun ofFloat(range: ClosedRange<Float>, configure: ValueAnimator.() -> Unit) {
        ofFloat(range.start, range.endInclusive, configure)
    }

    fun ofFloat(from: Float, to: Float, configure: ValueAnimator.() -> Unit) {
        val animator = ValueAnimator.ofFloat(from, to)
        configure(animator)
        animations.add(AnimatorWrapper(animator))
    }

    fun ofInt(from: Int, to: Int, configure: ValueAnimator.() -> Unit) {
        val animator = ValueAnimator.ofInt(from, to)
        configure(animator)
        animations.add(AnimatorWrapper(animator))
    }

    fun intAlphaFadeIn(configure: ValueAnimator.() -> Unit) {
        ofInt(Alpha.OPAQUE_INT, Alpha.TRANSPARENT_INT, configure)
    }

    fun intAlphaFadeOut(configure: ValueAnimator.() -> Unit) {
        ofInt(Alpha.TRANSPARENT_INT, Alpha.OPAQUE_INT, configure)
    }

    fun addListener(
        onEnd: ((animator: Animator) -> Unit)? = null,
        onStart: ((animator: Animator) -> Unit)? = null,
        onCancel: ((animator: Animator) -> Unit)? = null,
        onRepeat: ((animator: Animator) -> Unit)? = null
    ) {
        listeners.add(createAnimatorListener(onEnd, onStart, onCancel, onRepeat))
    }

    override fun build() = when {
        animations.isEmpty() -> emptyAnimator
        animations.size == 1 -> configureAnimator(animations[0].build())
        else -> AnimatorSet().also { set ->
            configureAnimator(set)
            applyChildAnimations(set, animations.map { it.build() })
        }
    }

    private fun configureAnimator(animator: Animator): Animator {
        interpolator?.let { animator.interpolator = it }
        _duration?.let { animator.duration = it }
        listeners.forEach {
            animator.addListener(it)
        }
        return animator
    }
}

private fun playSequentially() = AnimationGroup { set, animators ->
    set.playSequentially(animators)
}

private fun playTogether() = AnimationGroup { set, animators ->
    set.playTogether(animators)
}

fun buildAnimation(configureGroup: AnimationGroup.() -> Unit) =
    playTogether().also(configureGroup).build()

val emptyAnimator
    get() = object : Animator() {

        override fun isRunning() = false

        override fun getDuration() = 0L

        override fun getStartDelay() = 0L

        override fun setStartDelay(startDelay: Long) {
            // no-op
        }

        override fun setInterpolator(value: TimeInterpolator?) {
            // no-op
        }

        override fun setDuration(duration: Long) = this
    }