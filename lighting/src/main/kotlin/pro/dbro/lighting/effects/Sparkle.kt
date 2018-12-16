package pro.dbro.lighting.effects

import com.heroicrobot.dropbit.devices.pixelpusher.Pixel
import com.heroicrobot.dropbit.devices.pixelpusher.Strip
import pro.dbro.lighting.Effect
import pro.dbro.lighting.tween

class Sparkle(
        var pixelSparkle: Pixel,
        var onFraction: Float = 0.5f,
        var periodTicks: Long = 3
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

        if (seed < onFraction) {
            pixel.setColor(pixelSparkle)
        } else {
            pixel.setColor(pixelOff)
        }

        if (tick % periodTicks == 0L) { //Math.random() < reseedProbability) {
            pixelSeed[key] = (Math.random() * Math.PI * 2).toFloat()
        }
    }
}