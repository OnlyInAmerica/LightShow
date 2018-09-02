package pro.dbro.lighting

import com.heroicrobot.dropbit.devices.pixelpusher.Pixel
import com.heroicrobot.dropbit.registry.DeviceRegistry
import pro.dbro.lighting.effects.Flash
import pro.dbro.lighting.effects.Twinkle
import java.util.*
import kotlin.collections.ArrayList


class Lighting {

    private val registry = DeviceRegistry()
    private val observer = TestObserver()

    private val pixelRed = Pixel(255.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte())
    private val pixelBright = Pixel(0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 255.toByte())
    private val pixelRedFire = Pixel(255.toByte(), 75.toByte(), 0.toByte(), 200.toByte(), 17.toByte())
    private val pixelFire = Pixel(129.toByte(), 75.toByte(), 0.toByte(), 0xFF.toByte(), 17.toByte())
    private val pixel = Pixel(129.toByte(), 75.toByte(), 0.toByte(), 0xFF.toByte(), 17.toByte())
    private val pixelOff = Pixel()

    val twinkle = Twinkle(pixelA = pixelFire, pixelB = pixelRedFire, periodTicks = 100)
    val flash = Flash()

    private val ticksPerTween = 30.0

    private val pixelSeed = ArrayList<Double>(240)

    private var didSetup = false

    private val pixelStartIdx = 13

    init {
        registry.addObserver(observer)
    }

    internal class TestObserver : Observer {
        var hasStrips = false
        override fun update(registry: Observable, updatedDevice: Any?) {
            println("Registry changed!")
            if (updatedDevice != null) {
                println("Device change: $updatedDevice")
            }
            this.hasStrips = true
        }
    }

    private fun setup() {
        val strips = registry.strips
        for (pos in pixelStartIdx until strips.first().length) {
            pixelSeed.add(Math.random() * Math.PI * 2)
        }
    }

    fun draw(tick: Long) {
        if (observer.hasStrips) {

            if (!didSetup) {
                setup()
                didSetup = true
            }

            registry.startPushing()
            registry.setExtraDelay(0)
            registry.setAutoThrottle(true)
            val strips = registry.strips

            val numStrips = strips.size
            if (numStrips == 0)
                return

//            val timeTick = ((tick % 300) / 299.0)
//            println(timeTick)

//            val intTick = Math.floor(tick / 2.0)
            strips.forEach { strip ->
                for (pos in pixelStartIdx until strip.length) {

                    twinkle.draw(tick, strip, pos, pixel)

                    if (tick % 120L == 0L) {
                        println("Flash!")
                        flash.flash(1f, flashPixel = pixelRedFire)
                    }

                    flash.draw(tick, strip, pos, pixel)

                    strip.setPixel(pixel, pos)
                    // Barber pole style
//                    val pos2: Long = ((pos + intTick) % 9).toLong()

//                    val t = 2 * Math.PI * (tick / 30f) + ((pos2 / 8.0) * 2 * Math.PI)
//                    val sin = 0.5 + (0.5 * Math.sin(t))
//                    val sin = Math.max(0.0, Math.sin(t))

//                    pixel.pro.dbro.lighting.tween(pixelBright, sin)
//                    strip.setPixel(pixel, pos)

//                    when (pos2) {
//                        0L -> {
//                            pixel.pro.dbro.lighting.tween(pixelBright, 1.0)
//                            strip.setPixel(pixelBright, pos)
//                        }
//                        1L, 8L -> {
//                            pixel.pro.dbro.lighting.tween(pixelBright, 0.4)
//                            strip.setPixel(pixel, pos)
//                        }
//                        2L, 7L -> {
//                            pixel.pro.dbro.lighting.tween(pixelBright, 0.2)
//                            strip.setPixel(pixel, pos)
//                        }
//                        2 -> {
//                            pixel.pro.dbro.lighting.tween(pixelFire, 0.6)
//                        }
//                        3 -> {
//                            pixel.pro.dbro.lighting.tween(pixelFire, 0.4)
//                        }
//                        4 -> {
//                            pixel.pro.dbro.lighting.tween(pixelFire, 0.2)
//                        }
//                        5 -> {
//                            pixel.pro.dbro.lighting.tween(pixelFire, 0.0)
//                        }
//                        else -> {
//                            strip.setPixel(pixelOff, pos)
//                        }
//                    }

                    // Frigonometry
//                    val seed = pixelSeed[pos - 13]
//                    val t = seed + 2 * Math.PI * ((tick + (Math.random() - 0.5)) / 60f)
//                    val sin = Math.max(0.0, (0.75 * Math.sin(t)) + (0.5 * rand - 0.25))
//                    val cos = 0.5 + (0.5 * Math.cos(t))
//
//                    if (seed < 0.5) {
//                        val sin = Math.max(0.0, (0.75 * Math.sin(t)))// + (0.5 * rand - 0.25))
//                        pixel.pro.dbro.lighting.tween(pixelFire, sin)
//                    } else {
//                        val cos = Math.max(0.0, (0.75 * Math.cos(t)))// + (0.5 * rand - 0.25))
//                        pixel.pro.dbro.lighting.tween(pixelRedFire, cos)
//                    }
//
//                    if (Math.random() > 0.97) {
//                        pixelSeed[pos - 13] = Math.random() * Math.PI * 2
//                    }

                    // Longitudal gradient
//                    val scaleFactor = (pos.toDouble() - pixelStartIdx) / (strip.length - pixelStartIdx)
//                    pixel.tween(
//                            pixelRed,
//                            scaleFactor,
//                            pixelFire,
//                            1.0 - scaleFactor)

//                    pixel.pro.dbro.lighting.tween(pixelFire, (tick % 30) / 30.0)

//                    pixel.pro.dbro.lighting.tween(pixelFire, 0.4)
//                    strip.setPixel(pixel, pos)
                }
            }
        }
    }

}

fun clampLight(value: Double): Int {
    return clamp(value, 1.0, 255.0).toInt()
}

fun clamp(value: Double, min: Double, max: Double): Double {
    return Math.max(min, Math.min(value, max))
}

public fun main(args: Array<String>) {
    val lighting = Lighting()
    var tick = 0L
    while (true) {
        lighting.draw(tick++)
        Thread.sleep(30)
    }
}