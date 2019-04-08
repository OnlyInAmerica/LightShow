package pro.dbro.lighting.effects

import com.heroicrobot.dropbit.devices.pixelpusher.Pixel
import com.heroicrobot.dropbit.devices.pixelpusher.Strip
import pro.dbro.lighting.Effect
import pro.dbro.lighting.PixelMap
import pro.dbro.lighting.tween

class Blend(
        var pixelA: Pixel,
        var pixelB: Pixel,
        var reseedProbability: Float = 0.03f,
        var flickerIntensity: Float = 0.5f,
        var periodTicks: Long = 60
) : Effect {

    val pixelSeed = PixelMap {
        return@PixelMap Math.random().toFloat()
    }

    override fun draw(tick: Long, strip: Strip, stripIdx: Int, pixel: Pixel) {
        val seed = pixelSeed.get(strip, stripIdx)

        val t = 2 * Math.PI * (seed + (tick / periodTicks.toFloat()) + (flickerIntensity * (2 * Math.random() - 1)))

        val sin = 0.5f + 0.50f * Math.sin(t).toFloat()

        pixel.tween(pixelA,
                sin,
                pixelB,
                1 - sin)

        if (Math.random() < reseedProbability) {
            pixelSeed.set(strip, stripIdx, (Math.random() * Math.PI * 2).toFloat())
        }
    }
}