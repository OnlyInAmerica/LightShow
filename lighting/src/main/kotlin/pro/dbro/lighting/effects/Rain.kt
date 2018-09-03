package pro.dbro.lighting.effects

import com.heroicrobot.dropbit.devices.pixelpusher.Pixel
import com.heroicrobot.dropbit.devices.pixelpusher.Strip
import pro.dbro.lighting.Effect
import pro.dbro.lighting.off
import pro.dbro.lighting.tween

class Rain(
        var pixelA: Pixel,
        var pixelB: Pixel,
        var periodTicks: Long = 60,
        var pixelMod: Int = 9
) : Effect {

    override fun draw(tick: Long, strip: Strip, stripIdx: Int, pixel: Pixel) {

        val group: Int = stripIdx % pixelMod

        val groupFrac = group.toFloat() / (pixelMod - 1)
        val groupNum: Int = stripIdx / pixelMod
        val maxGroupNum = strip.length / pixelMod

        val groupNumFrac = groupNum / maxGroupNum.toFloat()

        val t = 2 * Math.PI * (groupFrac + groupNumFrac + (tick / periodTicks.toFloat()))
        val sin = 0.5f + (0.5f * Math.sin(t)).toFloat()

        if (sin > 0.9) {
            pixel.tween(
                    pixelA,
                    groupNumFrac,
                    pixelB,
                    1 - groupNumFrac)
        } else {
            pixel.off()
        }
    }
}