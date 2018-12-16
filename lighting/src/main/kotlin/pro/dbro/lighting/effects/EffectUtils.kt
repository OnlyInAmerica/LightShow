package pro.dbro.lighting.effects

import com.heroicrobot.dropbit.devices.pixelpusher.Pixel

val pixelOff = Pixel()

fun decay5(elapsedTime: Long, start: Float, delta: Float, duration: Long): Float {
    val time = (elapsedTime / duration.toFloat()) - 1
    return delta * (time * time * time * time * time + 1) + start
}