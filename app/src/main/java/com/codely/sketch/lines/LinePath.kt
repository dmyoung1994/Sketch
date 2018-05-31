package com.codely.sketch.lines

import android.graphics.Paint
import android.graphics.Path
import java.util.concurrent.atomic.AtomicBoolean

class LinePath() {
    private var canAnimate: AtomicBoolean = AtomicBoolean(false)
    private var paint: Paint = Paint()
    private var path: Path = Path()
    private var alpha: Int = 100

    fun getPath(): Path {
        return path
    }

    fun getPaint(): Paint {
        return paint
    }

    fun canAnimate(): Boolean {
        return canAnimate.get()
    }

    fun setAlpha(newAlpha: Int) {
        alpha = newAlpha
    }

    fun setPath(newPath: Path) {
        path = newPath
    }

    fun setPaint(newPaint: Paint) {
        paint = newPaint
    }

    fun setCanAnimate(newAnimate: Boolean) {
        canAnimate.set(newAnimate)
    }
}