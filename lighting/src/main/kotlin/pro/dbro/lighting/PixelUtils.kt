package pro.dbro.lighting

import com.heroicrobot.dropbit.devices.pixelpusher.Pixel

fun Pixel.tween(src: Pixel, factor: Float) {
    this.red = ((src.red.toInt() and 0xFF) * factor).toByte()
    this.green = ((src.green.toInt() and 0xFF) * factor).toByte()
    this.blue = ((src.blue.toInt() and 0xFF) * factor).toByte()
    this.white = ((src.white.toInt() and 0xFF) * factor).toByte()
    this.orange = ((src.orange.toInt() and 0xFF) * factor).toByte()
}

fun Pixel.tween(src: Pixel, srcFactor: Float, dst: Pixel, dstFactor: Float) {
    this.red = (((src.red.toInt() and 0xFF) * srcFactor).toByte() +
            ((dst.red.toInt() and 0xFF) * dstFactor).toByte()).toByte()
    this.green = (((src.green.toInt() and 0xFF) * srcFactor).toByte() +
            ((dst.green.toInt() and 0xFF) * dstFactor).toByte()).toByte()
    this.blue = (((src.blue.toInt() and 0xFF) * srcFactor).toByte() +
            ((dst.blue.toInt() and 0xFF) * dstFactor).toByte()).toByte()
    this.white = (((src.white.toInt() and 0xFF) * srcFactor).toByte() +
            ((dst.white.toInt() and 0xFF) * dstFactor).toByte()).toByte()
    this.orange = (((src.orange.toInt() and 0xFF) * srcFactor).toByte() +
            ((dst.orange.toInt() and 0xFF) * dstFactor).toByte()).toByte()
}

fun Pixel.ceil(): Pixel {
    val max = Math.max(this.red.toInt(),
            Math.max(this.green.toInt(),
                    Math.max(this.blue.toInt(),
                            Math.max(this.white.toInt(), this.orange.toInt())
                    )
            )
    )
    val scale = max.toFloat() / max

    val copy = Pixel(this)
    copy.tween(copy, scale)
    return copy
}