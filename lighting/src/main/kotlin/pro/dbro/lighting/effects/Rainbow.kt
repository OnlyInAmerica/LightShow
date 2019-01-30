package pro.dbro.lighting.effects

import com.heroicrobot.dropbit.devices.pixelpusher.Pixel
import com.heroicrobot.dropbit.devices.pixelpusher.Strip
import pro.dbro.lighting.Effect
import pro.dbro.lighting.rainbow

class Rainbow(
        var periodTicks: Long = 2400,
        var mode: Mode = Mode.Strip
) : Effect {

    enum class Mode {
        Vertical,
        Horizontal,
        Strip
    }

    override fun draw(tick: Long, strip: Strip, stripIdx: Int, pixel: Pixel) {
        val stripOffset: Double = when (mode) {
            Rainbow.Mode.Vertical -> (stripIdx / 7f) * 2 * Math.PI
            Rainbow.Mode.Horizontal -> {
                val group: Int = stripIdx % 9
                group / (9.0 - 1)
            }
            Rainbow.Mode.Strip -> (strip.stripNumber / 7f) * 2 * Math.PI
        }
        val t = 2 * Math.PI * (tick / periodTicks.toFloat()) + stripOffset
        pixel.rainbow(t)
    }
}