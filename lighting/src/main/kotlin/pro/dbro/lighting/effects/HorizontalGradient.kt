package pro.dbro.lighting.effects

import com.heroicrobot.dropbit.devices.pixelpusher.Pixel
import com.heroicrobot.dropbit.devices.pixelpusher.Strip

class HorizontalGradient(
        pixelA: Pixel,
        pixelB: Pixel,
        periodTicks: Long = 60,
        var pixelMod: Int = 9

) : Gradient(pixelA, pixelB, periodTicks) {

    override fun tForPos(tick: Long, strip: Strip, stripIdx: Int): Double {
        val group: Int = stripIdx % pixelMod
        val posFrac = group / (pixelMod - 1).toFloat()
        return 2 * Math.PI * (posFrac + (tick / periodTicks.toFloat()))
    }
}