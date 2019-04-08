package pro.dbro.lighting

import com.heroicrobot.dropbit.devices.pixelpusher.Pixel
import com.heroicrobot.dropbit.devices.pixelpusher.Strip

interface Effect {
    fun draw(tick: Long, strip: Strip, stripIdx: Int, pixel: Pixel)
}

class PixelMap<T>(val newValFunc: () -> T) {
    private val map = HashMap<Int, T>(240)

    private fun keyForStrip(strip: Strip, pos: Int): Int {
        return strip.stripNumber + pos
    }

    fun get(strip: Strip, stripPos: Int): T {
        val key = keyForStrip(strip, stripPos)
        if (!map.containsKey(key)) {
            map[key] = newValFunc()
        }
        return map[key]!!
    }

    fun set(strip: Strip, stripPos: Int, value: T) {
        val key = keyForStrip(strip, stripPos)
        map[key] = value
    }
}