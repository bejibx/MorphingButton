package com.example.bejibx.morphingbutton

import android.animation.Animator

fun Animator.doOnEnd(action: (Animator) -> Unit) = addListener(onEnd = action)

fun Animator.doOnStart(action: (Animator) -> Unit) = addListener(onStart = action)

fun Animator.addListener(
    onEnd: ((animator: Animator) -> Unit)? = null,
    onStart: ((animator: Animator) -> Unit)? = null,
    onCancel: ((animator: Animator) -> Unit)? = null,
    onRepeat: ((animator: Animator) -> Unit)? = null
) {
    addListener(createAnimatorListener(onEnd, onStart, onCancel, onRepeat))
}

fun createAnimatorListener(
    onEnd: ((animator: Animator) -> Unit)? = null,
    onStart: ((animator: Animator) -> Unit)? = null,
    onCancel: ((animator: Animator) -> Unit)? = null,
    onRepeat: ((animator: Animator) -> Unit)? = null
) = object : Animator.AnimatorListener {

    override fun onAnimationRepeat(animation: Animator) {
        onRepeat?.invoke(animation)
    }

    override fun onAnimationEnd(animation: Animator) {
        onEnd?.invoke(animation)

    }

    override fun onAnimationCancel(animation: Animator) {
        onCancel?.invoke(animation)
    }

    override fun onAnimationStart(animation: Animator) {
        onStart?.invoke(animation)
    }
}