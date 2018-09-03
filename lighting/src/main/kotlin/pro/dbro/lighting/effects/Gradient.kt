package pro.dbro.lighting.effects

import com.heroicrobot.dropbit.devices.pixelpusher.Pixel
import com.heroicrobot.dropbit.devices.pixelpusher.Strip
import pro.dbro.lighting.Effect
import pro.dbro.lighting.tween

open class Gradient(
        var pixelA: Pixel,
        var pixelB: Pixel,
        var periodTicks: Long = 60
) : Effect {

    open fun tForPos(tick: Long, strip: Strip, stripIdx: Int): Double {
        val posFrac = (stripIdx) / (strip.length.toFloat())
        return 2 * Math.PI * (posFrac + (tick / periodTicks.toFloat()))
    }

    override fun draw(tick: Long, strip: Strip, stripIdx: Int, pixel: Pixel) {

        val t = tForPos(tick, strip, stripIdx)
        val sin = 0.5f + (0.5 * Math.sin(t)).toFloat()

        pixel.tween(
                pixelA,
                sin,
                pixelB,
                1f - sin)
    }
}