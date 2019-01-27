package pro.dbro.lighting.effects

import com.heroicrobot.dropbit.devices.pixelpusher.Pixel
import com.heroicrobot.dropbit.devices.pixelpusher.Strip
import pro.dbro.lighting.Effect
import pro.dbro.lighting.ceil
import pro.dbro.lighting.tween

class HorizontalWalkFlash(var pixelMod: Int = 9) : Effect {

    var flashPixel: Pixel? = null
    var flashDurationTicks = 0L
    var flashPeriodTicks = 0L
    var flashStartTick = 0L
    var flashMaxIntensity = 0f
    var flashCurIntensity = 0f
    var stripIdMask: Int = 0b11111111

    /**
     * @param durationTicks if is 0, flash should not begin release until [unFlash] called
     */
    fun flash(intensity: Float,
              durationTicks: Long = 60,
              periodTicks: Long = 60,
              flashPixel: Pixel? = null,
              stripIdxMask: Int) {

        reset()
        flashMaxIntensity = intensity
        flashCurIntensity = intensity
        flashDurationTicks = durationTicks
        flashPeriodTicks = periodTicks
        this.flashPixel = flashPixel
        this.stripIdMask = stripIdxMask
    }

    fun unFlash(durationTicks: Long = 60) {
        this.flashStartTick = 0
        this.flashDurationTicks = durationTicks
    }

    override fun draw(tick: Long, strip: Strip, stripIdx: Int, pixel: Pixel) {
        if (stripIdMask and (1 shl strip.stripNumber) == 0) {
            return
        }

        if (flashMaxIntensity > 0) {
            if (flashStartTick == 0L) {
                // First flash draw
                flashStartTick = tick

                if (flashPixel == null) {
                    flashPixel = pixel.ceil()
                }
            }

            flashPixel?.let {
                val group = stripIdx % pixelMod

                val groupId = ((group + (tick / (flashPeriodTicks.toFloat() / pixelMod))) % pixelMod).toInt()

                if (groupId == 0) {
                    pixel.tween(pixel, 1f - flashCurIntensity, it, flashCurIntensity)
                }
            }

            if (flashDurationTicks > 0) {
                flashCurIntensity = decay(tick - flashStartTick, flashMaxIntensity, -flashMaxIntensity, flashDurationTicks)
                if (flashCurIntensity < 0.01f) {
                    // Last flash draw
                    reset()
                }
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