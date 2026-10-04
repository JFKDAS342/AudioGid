package com.example.audiogid.src

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import java.lang.ref.WeakReference

object AnimationHelper {

    // Храним слабые ссылки, чтобы анимация не держала View после onDestroy
    private val runningAnims = mutableMapOf<Int, ObjectAnimator>()

    fun startGradientAnimation(view: View): ObjectAnimator {
        // Останавливаем предыдущую анимацию для этого View, чтобы не плодить дубли
        stopAnimation(view)

        val anim = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f).apply {
            duration = 4000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
        }
        anim.start()
        runningAnims[System.identityHashCode(view)] = anim
        return anim
    }

    fun stopAnimation(view: View) {
        val key = System.identityHashCode(view)
        runningAnims.remove(key)?.let {
            it.cancel()
            // Возвращаем нормальную прозрачность, иначе View может остаться полупрозрачным
            view.alpha = 1f
        }
    }

    @Suppress("unused")
    fun stopAll() {
        runningAnims.values.forEach { it.cancel() }
        runningAnims.clear()
    }
}
