package com.github.leodan11.customview.widget.tools

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import java.lang.Math.toRadians

internal class Snowflake(private val randomizer: Randomizer, private val params: Params) {

    private var size: Int = 0
    private var alpha: Int = 255
    private var bitmap: Bitmap? = null
    private var speedX: Double = 0.0
    private var speedY: Double = 0.0
    private var positionX: Double = 0.0
    private var positionY: Double = 0.0

    private var _paint: Paint? = null
    private val paint: Paint
        get() {
            if (_paint == null) {
                _paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    style = Paint.Style.FILL
                }
            }

            return _paint!!
        }

    var shouldRecycleFalling = true
    private var stopped = false

    init {
        reset()
    }

    internal fun reset(positionY: Double? = null) {
        shouldRecycleFalling = true
        size = randomizer.randomInt(params.sizeMinInPx, params.sizeMaxInPx, gaussian = true)
        if (params.image != null) {
            bitmap = Bitmap.createScaledBitmap(params.image, size, size, false)
        }

        val speed =
            ((size - params.sizeMinInPx).toFloat() / (params.sizeMaxInPx - params.sizeMinInPx) *
                    (params.speedMax - params.speedMin) + params.speedMin)
        val angle = toRadians(randomizer.randomDouble(params.angleMax) * randomizer.randomSigNum())
        speedX = speed * kotlin.math.sin(angle)
        speedY = speed * kotlin.math.cos(angle)

        alpha = randomizer.randomInt(params.alphaMin, params.alphaMax)
        paint.alpha = alpha

        positionX = randomizer.randomDouble(params.parentWidth)
        if (positionY != null) {
            this.positionY = positionY
        } else {
            this.positionY = randomizer.randomDouble(params.parentHeight)
            if (!params.alreadyFalling) {
                this.positionY = this.positionY - params.parentHeight - size
            }
        }
    }

    fun isStillFalling(): Boolean {
        return (shouldRecycleFalling || (positionY > 0 && positionY < params.parentHeight))
    }

    fun update() {
        positionX += speedX
        positionY += speedY
        if (positionY > params.parentHeight) {
            if (shouldRecycleFalling) {
                if (stopped) {
                    stopped = false
                    reset()
                } else {
                    reset(positionY = -size.toDouble())
                }
            } else {
                positionY = params.parentHeight + size.toDouble()
                stopped = true
            }
        }
        if (params.fadingEnabled) {
            paint.alpha =
                (alpha * ((params.parentHeight - positionY).toFloat() / params.parentHeight)).toInt()
        }
    }

    fun draw(canvas: Canvas) {
        val localBitmap = bitmap
        if (localBitmap != null) {
            canvas.drawBitmap(localBitmap, positionX.toFloat(), positionY.toFloat(), paint)
        } else {
            canvas.drawCircle(positionX.toFloat(), positionY.toFloat(), size.toFloat(), paint)
        }
    }


    internal class Params(
        val parentWidth: Int,
        val parentHeight: Int,
        val image: Bitmap?,
        val alphaMin: Int,
        val alphaMax: Int,
        val angleMax: Int,
        val sizeMinInPx: Int,
        val sizeMaxInPx: Int,
        val speedMin: Int,
        val speedMax: Int,
        val fadingEnabled: Boolean,
        val alreadyFalling: Boolean,
    )

}