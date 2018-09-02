package pro.dbro.lighting.effects

import com.heroicrobot.dropbit.devices.pixelpusher.Pixel
import com.heroicrobot.dropbit.devices.pixelpusher.Strip
import pro.dbro.lighting.Effect
import pro.dbro.lighting.tween

class Twinkle(
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

        val t = seed + 2 * Math.PI * (tick.toFloat() + (flickerIntensity * (2 * Math.random() - 1)) / periodTicks)

        if (seed < 0.5) {
            val sin = Math.max(0.0, (0.75 * Math.sin(t)))
            pixel.tween(pixelA, sin)
        } else {
            val cos = Math.max(0.0, (0.75 * Math.cos(t)))
            pixel.tween(pixelB, cos)
        }

        if (Math.random() < reseedProbability) {
            pixelSeed[key] = (Math.random() * Math.PI * 2).toFloat()
        }
    }
}