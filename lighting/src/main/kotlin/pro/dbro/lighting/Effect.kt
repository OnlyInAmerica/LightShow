package pro.dbro.lighting

import com.heroicrobot.dropbit.devices.pixelpusher.Pixel
import com.heroicrobot.dropbit.devices.pixelpusher.Strip

interface Effect {
    fun draw(tick: Long, strip: Strip, stripIdx: Int, pixel: Pixel)
}