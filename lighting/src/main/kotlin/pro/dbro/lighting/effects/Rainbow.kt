package pro.dbro.lighting.effects

import com.heroicrobot.dropbit.devices.pixelpusher.Pixel
import com.heroicrobot.dropbit.devices.pixelpusher.Strip
import pro.dbro.lighting.Effect
import kotlin.math.sin

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

        val red = sin(t) * 127 + 128
        val green = sin(t + 2) * 127 + 128
        val blue = sin(t + 4) * 127 + 128

        pixel.red = red.toByte()
        pixel.green = green.toByte()
        pixel.blue = blue.toByte()
        pixel.white = 0
        pixel.orange = 0
    }
}