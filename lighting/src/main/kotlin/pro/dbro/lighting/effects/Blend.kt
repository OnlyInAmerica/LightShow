package pro.dbro.lighting.effects

import com.heroicrobot.dropbit.devices.pixelpusher.Pixel
import com.heroicrobot.dropbit.devices.pixelpusher.Strip
import pro.dbro.lighting.Effect
import pro.dbro.lighting.tween

class Blend(
        var pixelA: Pixel,
        var pixelB: Pixel,
        var reseedProbability: Float = 0.03f,
        var flickerIntensity: Float = 0.5f,
        var periodTicks: Long = 60
) : Effect {

    val pixelSeed = HashMap<String, Float>(240)

    private fun keyForStrip(strip: Strip, pos: Int): String {
        return "${strip.macAddress}-${strip.stripNumber}-$pos"
    }

    private fun getOrMakeSeed(key: String): Float {
        return if (!pixelSeed.contains(key)) {
            val rand = Math.random().toFloat()
            pixelSeed[key] = rand
            rand
        } else {
            pixelSeed[key]!!
        }
    }

    override fun draw(tick: Long, strip: Strip, stripIdx: Int, pixel: Pixel) {
        val key = keyForStrip(strip, stripIdx)
        val seed = getOrMakeSeed(key)

        val t = 2 * Math.PI * (seed + (tick / periodTicks.toFloat()) + (flickerIntensity * (2 * Math.random() - 1)))

        val sin = 0.5f + 0.50f * Math.sin(t).toFloat()

        pixel.tween(pixelA,
                sin,
                pixelB,
                1 - sin)

        if (Math.random() < reseedProbability) {
            pixelSeed[key] = (Math.random() * Math.PI * 2).toFloat()
        }
    }
}