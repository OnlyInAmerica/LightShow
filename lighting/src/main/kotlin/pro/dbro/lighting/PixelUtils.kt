package pro.dbro.lighting

import com.heroicrobot.dropbit.devices.pixelpusher.Pixel

private val pixelOff = Pixel(0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte())

fun Pixel.redInt(): Int {
    return red.toInt() and 0xFF
}

fun Pixel.greenInt(): Int {
    return green.toInt() and 0xFF
}

fun Pixel.blueInt(): Int {
    return blue.toInt() and 0xFF
}

fun Pixel.whiteInt(): Int {
    return white.toInt() and 0xFF
}

fun Pixel.orangeInt(): Int {
    return orange.toInt() and 0xFF
}

fun Pixel.off() {
    this.setColor(pixelOff)
}

fun Pixel.tween(src: Pixel, factor: Float) {
    this.red = ((src.redInt()) * factor).toByte()
    this.green = ((src.greenInt()) * factor).toByte()
    this.blue = ((src.blueInt()) * factor).toByte()
    this.white = ((src.whiteInt()) * factor).toByte()
    this.orange = ((src.orangeInt()) * factor).toByte()
}

fun Pixel.tween(src: Pixel, srcFactor: Float, dst: Pixel, dstFactor: Float) {
    this.red = (((src.redInt()) * srcFactor) +
            ((dst.redInt()) * dstFactor)).toByte()
    this.green = (((src.greenInt()) * srcFactor) +
            ((dst.greenInt()) * dstFactor)).toByte()
    this.blue = (((src.blueInt()) * srcFactor) +
            ((dst.blueInt()) * dstFactor)).toByte()
    this.white = (((src.whiteInt()) * srcFactor) +
            ((dst.whiteInt()) * dstFactor)).toByte()
    this.orange = (((src.orangeInt()) * srcFactor) +
            ((dst.orangeInt()) * dstFactor)).toByte()
}

fun Pixel.ceil(boost: Pixel? = null): Pixel {
    val max = Math.max(this.redInt(),
            Math.max(this.greenInt(),
                    Math.max(this.blueInt(),
                            Math.max(this.whiteInt(), this.orangeInt())
                    )
            )
    )
    val scale = 255 / max.toFloat()

    val copy = Pixel(this)
    copy.tween(copy, scale)
    boost?.let { copy.boost(boost) }
    return copy
}

/**
 * Add boost's component values to this Pixel
 */
fun Pixel.boost(boost: Pixel) {
        this.red = (this.red + boost.red).toByte()
        this.green = (this.green + boost.green).toByte()
        this.blue = (this.blue + boost.blue).toByte()
        this.white = (this.white + boost.white).toByte()
        this.orange = (this.orange + boost.orange).toByte()
}