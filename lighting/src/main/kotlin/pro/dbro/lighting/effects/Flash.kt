package pro.dbro.lighting.effects

import com.heroicrobot.dropbit.devices.pixelpusher.Pixel
import com.heroicrobot.dropbit.devices.pixelpusher.Strip
import pro.dbro.lighting.Effect
import pro.dbro.lighting.ceil
import pro.dbro.lighting.tween

class Flash : Effect {

    var flashPixel: Pixel? = null
    var flashDurationTicks = 0L
    var flashStartTick = 0L
    var flashMaxIntensity = 0f
    var flashCurIntensity = 0f

    fun flash(intensity: Float, durationTicks: Long = 60, flashPixel: Pixel? = null) {
        flashMaxIntensity = intensity
        flashCurIntensity = intensity
        flashDurationTicks = durationTicks
        this.flashPixel = flashPixel
    }

    override fun draw(tick: Long, strip: Strip, stripIdx: Int, pixel: Pixel) {
        if (flashMaxIntensity > 0) {
            if (flashStartTick == 0L) {
                // First flash draw
                flashStartTick = tick

                if (flashPixel == null) {
                    flashPixel = pixel.ceil()
                }
            }

            flashPixel?.let {
                pixel.tween(pixel, 1f - flashCurIntensity, it, flashCurIntensity)
            }

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