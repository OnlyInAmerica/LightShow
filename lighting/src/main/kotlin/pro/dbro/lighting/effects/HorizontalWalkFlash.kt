package pro.dbro.lighting.effects

import com.heroicrobot.dropbit.devices.pixelpusher.Pixel

import com.heroicrobot.dropbit.devices.pixelpusher.Strip
import pro.dbro.lighting.Effect
import pro.dbro.lighting.ceil
import pro.dbro.lighting.forEachInBitMask
import pro.dbro.lighting.tween

typealias StripId = Int

class HorizontalWalkFlash(var pixelMod: Int = 9) : Effect {

    data class Instance(val intensity: Float,
                        var durationTicks: Long = 60,
                        val periodTicks: Long = 60,
                        var pixel: Pixel? = null,
                        var maxIntensity: Float = intensity,
                        var curIntensity: Float = intensity,
                        var startTick: Long = 0)

    var instanceMap = HashMap<StripId, Instance>()

    /**
     * @param durationTicks flash duration. If 0, flash should not begin release until [unFlash] called.
     */
    fun flash(intensity: Float,
              durationTicks: Long = 60,
              periodTicks: Long = 60,
              flashPixel: Pixel? = null,
              stripIdxMask: Int) {

        forEachInBitMask(stripIdxMask) { stripId ->
            instanceMap[stripId] =
                    Instance(intensity,
                            durationTicks,
                            periodTicks,
                            flashPixel)
        }

    }

    fun unFlash(durationTicks: Long = 60,
                stripIdxMask: Int) {

        forEachInBitMask(stripIdxMask) { stripId ->
            val instance = instanceMap[stripId] ?: return@forEachInBitMask
            instance.startTick = 0
            instance.durationTicks = durationTicks
        }
    }

    override fun draw(tick: Long, strip: Strip, stripIdx: Int, pixel: Pixel) {

        val i = instanceMap[strip.stripNumber] ?: return

        if (i.maxIntensity > 0) {
            if (i.startTick == 0L) {
                // First flash draw
                i.startTick = tick

                if (i.pixel == null) {
                    i.pixel = pixel.ceil()
                }
            }

            i.pixel?.let {
                val group = stripIdx % pixelMod

                val groupId = ((group + (tick / (i.periodTicks.toFloat() / pixelMod))) % pixelMod).toInt()

                if (groupId == 0) {
                    pixel.tween(pixel, 1f - i.curIntensity, it, i.curIntensity)
                }
            }

            if (i.durationTicks > 0) {
                i.curIntensity = decay(tick - i.startTick, i.maxIntensity, -i.maxIntensity, i.durationTicks)
                if (i.curIntensity < 0.01f) {
                    // Last flash draw
                    instanceMap.remove(strip.stripNumber)
                }
            }
        }
    }

    private fun decay(elapsedTime: Long, start: Float, delta: Float, duration: Long): Float {
        val time = (elapsedTime / duration.toFloat()) - 1
        return delta * (time * time * time * time * time + 1) + start
    }
}