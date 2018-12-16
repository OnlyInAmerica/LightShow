package pro.dbro.lighting.effects

import com.heroicrobot.dropbit.devices.pixelpusher.Pixel
import com.heroicrobot.dropbit.devices.pixelpusher.Strip
import pro.dbro.lighting.Effect
import pro.dbro.lighting.off

class HorizontalWalk(
        var pixelMap: HashMap<Int, Pixel>,
        var periodTicks: Long = 60,
        var pixelMod: Int = 9,
        var additive: Boolean = false
) : Effect {

    override fun draw(tick: Long, strip: Strip, stripIdx: Int, pixel: Pixel) {

        val group = stripIdx % pixelMod

        val groupId = ((group + (tick / periodTicks)) % pixelMod).toInt()

        if (pixelMap.containsKey(groupId)) {
            pixel.setColor(pixelMap[groupId])
        } else if (!additive) {
            pixel.off()
        }
    }
}