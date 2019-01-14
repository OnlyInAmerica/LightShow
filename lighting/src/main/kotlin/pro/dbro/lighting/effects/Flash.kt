package pro.dbro.lighting.effects

import com.heroicrobot.dropbit.devices.pixelpusher.Pixel
import com.heroicrobot.dropbit.devices.pixelpusher.Strip
import pro.dbro.lighting.*

class Flash : Effect {

    var flashDurationTicks = 0L
    var flashStartTick = 0L
    var flashMaxIntensity = 0f
    var flashCurIntensity = 0f
    var boostPixel: Pixel? = null

    fun flash(intensity: Float, durationTicks: Long = 60, boostPixel: Pixel? = null) {
        reset()
        flashMaxIntensity = intensity
        flashCurIntensity = intensity
        flashDurationTicks = durationTicks
        this.boostPixel = boostPixel
    }

    override fun draw(tick: Long, strip: Strip, stripIdx: Int, pixel: Pixel) {
        if (flashMaxIntensity > 0) {
            if (flashStartTick == 0L) {
                // First flash draw
                flashStartTick = tick
            }

            pixel.tween(pixel, 1f - flashCurIntensity, pixel.ceil(boostPixel), flashCurIntensity)

            flashCurIntensity = decay(tick - flashStartTick, flashMaxIntensity, -flashMaxIntensity, flashDurationTicks)
            if (flashCurIntensity < 0.01f) {
                // Last flash draw
                reset()
            }
        }
    }

    private fun decay(elapsedTime: Long, start: Float, delta: Float, duration: Long): Float {
        val time = (elapsedTime / duration.toFloat()) - 1
        return delta * (time * time * time * time * time + 1) + start
    }

    private fun reset() {
        flashMaxIntensity = 0f
        flashStartTick = 0L
    }
}