package pro.dbro.lighting.effects

import com.heroicrobot.dropbit.devices.pixelpusher.Pixel
import com.heroicrobot.dropbit.devices.pixelpusher.Strip
import pro.dbro.lighting.Effect
import pro.dbro.lighting.ceil
import pro.dbro.lighting.tween

class Pulse : Effect {

    var pulsePixel: Pixel? = null
    var flashDurationTicks = 0L
    var flashStartTick = 0L
    var flashMaxIntensity = 0f
    var flashCurIntensity = 0f

    fun pulse(intensity: Float, durationTicks: Long = 60, pulsePixel: Pixel? = null) {
        reset()
        flashMaxIntensity = intensity
        flashCurIntensity = intensity
        flashDurationTicks = durationTicks
        this.pulsePixel = pulsePixel
    }

    override fun draw(tick: Long, strip: Strip, stripIdx: Int, pixel: Pixel) {
        if (flashMaxIntensity > 0) {
            if (flashStartTick == 0L) {
                // First flash draw
                flashStartTick = tick
            }

            pixel.tween(pixel, 1f - flashCurIntensity, pixel.ceil(), flashCurIntensity)

            val time = (2 * Math.PI) * ((tick - flashStartTick) / flashDurationTicks.toFloat())// - (Math.PI / 2f)
            flashCurIntensity = (0.5f * flashMaxIntensity) + (0.5f * flashMaxIntensity) * Math.sin(time).toFloat()//decay(tick - flashStartTick, flashMaxIntensity, -flashMaxIntensity, flashDurationTicks)
            if (flashCurIntensity < 0.01f) {
                // Last flash draw
                reset()
            }
        }
    }

    private fun reset() {
        flashMaxIntensity = 0f
        flashStartTick = 0L
    }
}