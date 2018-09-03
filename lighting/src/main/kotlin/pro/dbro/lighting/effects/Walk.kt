package pro.dbro.lighting.effects

import com.heroicrobot.dropbit.devices.pixelpusher.Pixel
import com.heroicrobot.dropbit.devices.pixelpusher.Strip
import pro.dbro.lighting.Effect
import pro.dbro.lighting.off

class Walk(
        var pixelA: Pixel,
        var periodTicks: Long = 60
) : Effect {

    override fun draw(tick: Long, strip: Strip, stripIdx: Int, pixel: Pixel) {

        val pos: Int = (((tick % periodTicks) / periodTicks.toFloat()) * (strip.length - 1)).toInt()

        if (stripIdx == pos) {
            pixel.setColor(pixelA)
        } else {
            pixel.off()
        }
    }
}