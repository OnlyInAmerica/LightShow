package pro.dbro.lighting.effects

import com.heroicrobot.dropbit.devices.pixelpusher.Pixel
import com.heroicrobot.dropbit.devices.pixelpusher.Strip
import pro.dbro.lighting.Effect
import pro.dbro.lighting.PixelMap
import pro.dbro.lighting.tween

class Twinkle(
        var pixelA: Pixel,
        var pixelB: Pixel,
        var reseedProbability: Float = 0.03f,
        var flickerIntensity: Float = 0.5f,
        var flickerReseedMod: Int = 2,
        var periodTicks: Long = 60
) : Effect {

    val pixelSeed = PixelMap {
        return@PixelMap Math.random().toFloat()
    }

    override fun draw(tick: Long, strip: Strip, stripIdx: Int, pixel: Pixel) {
        val seed = pixelSeed.get(strip, stripIdx)

        val flickerOrReseed: Boolean = tick % flickerReseedMod.toLong() == 0L

        val t = 2 * Math.PI * (seed + (tick / periodTicks.toFloat()))
        if (flickerOrReseed) {
            t + (2 * Math.PI * (flickerIntensity * (2 * Math.random() - 1)))
        }

        if (seed < 0.5) {
            val sin = 0.5 + 0.50 * Math.sin(t)
            if (sin.toFloat() > 0.5) {
                pixel.setColor(pixelA)
            } else {
                pixel.setColor(pixelB)
            }
//            pixel.tween(pixelA, sin.toFloat())
        } else {
            val cos = 0.5 + 0.50 * Math.cos(t)
            pixel.tween(pixelB, cos.toFloat())
        }

        if (flickerOrReseed && Math.random() < reseedProbability) {
            pixelSeed.set(strip, stripIdx, (Math.random() * Math.PI * 2).toFloat())
        }
    }
}