package pro.dbro.lighting.effects

import com.heroicrobot.dropbit.devices.pixelpusher.Pixel
import com.heroicrobot.dropbit.devices.pixelpusher.Strip
import pro.dbro.lighting.Effect
import pro.dbro.lighting.PixelMap

class Sparkle(
        var pixelSparkle: Pixel,
        var onFraction: Float = 0.5f,
        var periodTicks: Long = 3
) : Effect {

    val pixelSeed = PixelMap {
        return@PixelMap Math.random().toFloat()
    }

    override fun draw(tick: Long, strip: Strip, stripIdx: Int, pixel: Pixel) {
        val seed = pixelSeed.get(strip, stripIdx)

        if (seed < onFraction) {
            pixel.setColor(pixelSparkle)
        } else {
            pixel.setColor(pixelOff)
        }

        if (tick % periodTicks == 0L) { //Math.random() < reseedProbability) {
            pixelSeed.set(strip, stripIdx, (Math.random() * Math.PI * 2).toFloat())
        }
    }
}