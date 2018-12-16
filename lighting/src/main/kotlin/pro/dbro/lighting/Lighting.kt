package pro.dbro.lighting

import com.heroicrobot.dropbit.devices.pixelpusher.Pixel
import com.heroicrobot.dropbit.registry.DeviceRegistry
import pro.dbro.lighting.effects.*
import java.util.*
import kotlin.collections.ArrayList


class Lighting {

    private val registry = DeviceRegistry()
    private val observer = TestObserver()

    private val pixelRed = Pixel(255.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte())
    private val pixelOrange = Pixel(255.toByte(), 41.toByte(), 0.toByte(), 0.toByte(), 0.toByte())
    private val pixelYellow = Pixel(255.toByte(), 180.toByte(), 0.toByte(), 164.toByte(), 0.toByte())
    private val pixelGreen = Pixel(0.toByte(), 180.toByte(), 0.toByte(), 34.toByte(), 0.toByte())
    private val pixelPureGreen = Pixel(0.toByte(), 255.toByte(), 0.toByte(), 0.toByte(), 0.toByte())
    private val pixelLightBlue = Pixel(0.toByte(), 188.toByte(), 255.toByte(), 0.toByte(), 3.toByte())
    private val pixelLightPurple = Pixel(80.toByte(), 0.toByte(), 255.toByte(), 0.toByte(), 0.toByte())
    private val pixelBlue = Pixel(0.toByte(), 100.toByte(), 255.toByte(), 0.toByte(), 0.toByte())
    private val pixelPureBlue = Pixel(0.toByte(), 0.toByte(), 255.toByte(), 0.toByte(), 0.toByte())
    private val pixelPurp = Pixel(255.toByte(), 10.toByte(), 255.toByte(), 0.toByte(), 0.toByte())
    private val pixelBright = Pixel(255.toByte(), 255.toByte(), 255.toByte(), 255.toByte(), 255.toByte())
    private val pixelRedFire = Pixel(255.toByte(), 75.toByte(), 0.toByte(), 0.toByte(), 0.toByte())
    private val pixelFire = Pixel(255.toByte(), 0.toByte(), 0.toByte(), 0xFF.toByte(), 0.toByte())
    private val pixel = Pixel(129.toByte(), 75.toByte(), 0.toByte(), 0xFF.toByte(), 17.toByte())
    private val pixelWhite = Pixel(0.toByte(), 0.toByte(), 0.toByte(), 255.toByte(), 255.toByte())

    // Warm White
    private val pixelRedFireWw = Pixel(255.toByte(), 17.toByte(), 0.toByte(), 30.toByte(), 0.toByte()) // Pixel(156.toByte(), 17.toByte(), 0.toByte(), 8.toByte(), 0.toByte())
    private val pixelFireWw = Pixel(57.toByte(), 13.toByte(), 0.toByte(), 0.toByte(), 0.toByte())

    enum class Program {
        Fire, VGradient, HGradient, /*Blend,*/ Purp, Earth, Sparkle//, Rain
    }

    private var curentProgram = Program.VGradient

    private val nextProgramCol = hashMapOf(
            Pair(Program.VGradient, pixelRed),
            Pair(Program.HGradient, pixelYellow),
            Pair(Program.Fire, pixelGreen),
//            Pair(Program.Blend, pixelPurp),
            Pair(Program.Purp, pixelGreen),
            Pair(Program.Earth, pixelWhite),
            Pair(Program.Sparkle, pixelFire)
    )

    /*
    On WW strips, blend (yellows) look poor. Vgradient
     */

    val plainWalk = Walk(pixelA = pixelRed, periodTicks = 420)
    val rain = Rain(pixelA = pixelRed, pixelB = pixelBlue)
    val walk = HorizontalWalk(pixelMap = hashMapOf(Pair(0, pixelBright)), periodTicks = 10, additive = true)
    val walkFlash = HorizontalWalkFlash()
    val vWalkFlash = VerticalWalkFlash()
    val gradient = Gradient(pixelA = pixelGreen, pixelB = pixelOrange, periodTicks = 480)
    val hGradient = HorizontalGradient(pixelA = pixelRed, pixelB = pixelPureBlue, periodTicks = 480)
    val twinkle = Twinkle(pixelA = pixelBlue, pixelB = pixelGreen, periodTicks = 480, reseedProbability = 0f, flickerIntensity = 0f)

    val fastTwinkle = Sparkle(pixelSparkle = pixelWhite, onFraction = .3f)

    val blend = Blend(pixelA = pixelRed, pixelB = pixelYellow, periodTicks = 480, reseedProbability = 0f, flickerIntensity = 0f)
    val purp = Blend(pixelA = pixelLightPurple, pixelB = pixelRed, periodTicks = 480, reseedProbability = 0f, flickerIntensity = 0f)
    val fire = Twinkle(pixelA = pixelFire, pixelB = pixelRedFire, periodTicks = 200)
    val fireWw = Twinkle(pixelA = pixelFireWw, pixelB = pixelRedFireWw, periodTicks = 200)
    val earth = Blend(pixelA = pixelPureBlue, pixelB = pixelPureGreen, periodTicks = 480, reseedProbability = 0f, flickerIntensity = 0f)
    val flash = Flash()
    val pulse = Pulse()

    private val ticksPerTween = 30.0

    private val pixelSeed = ArrayList<Double>(240)

    private var didSetup = false

    private val pixelStartIdx = 0

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

    var flashIntensity = 0.8f

//    fun twinkle() {
//        println("Twinkle")
//        twinkle.flash(flashIntensity, flashPixel = nextProgPixel, durationTicks = 120)
//    }

    fun flash() {
        println("Flash")
        val nextProgPixel = Pixel(nextProgramCol[curentProgram])
        nextProgPixel.white = 200.toByte()

        flash.flash(flashIntensity, flashPixel = nextProgPixel, durationTicks = 120)
    }

    fun walkFlash() {
        println("H Walk flash")
        val nextProgPixel = Pixel(nextProgramCol[curentProgram])
        nextProgPixel.white = 200.toByte()
        walkFlash.flash(flashIntensity, flashPixel = nextProgPixel, durationTicks = 60)
    }

    fun vWalkFlash() {
        println("V Walk flash")
        val nextProgPixel = Pixel(nextProgramCol[curentProgram])
        nextProgPixel.white = 200.toByte()
        vWalkFlash.flash(flashIntensity, flashPixel = nextProgPixel, durationTicks = 90)
    }

    fun switchProgram() {
        val values = Program.values()
        val lastOrdinal = values.last().ordinal
        val newOrdinal = (curentProgram.ordinal + 1) % (lastOrdinal + 1)
        curentProgram = values[newOrdinal]

        println("Changing program to " + curentProgram.name)

    }

    fun pulse() {
        pulse.pulse(intensity = flashIntensity)
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

                    when (curentProgram) {
                        Lighting.Program.VGradient -> gradient.draw(tick, strip, pos, pixel)
                        Lighting.Program.HGradient -> hGradient.draw(tick, strip, pos, pixel)
                        Lighting.Program.Fire -> fireWw/*fire*/.draw(tick, strip, pos, pixel)
//                        Lighting.Program.Blend -> blend.draw(tick, strip, pos, pixel)
                        Lighting.Program.Purp -> purp.draw(tick, strip, pos, pixel)
//                        Lighting.Program.Walk -> walk.draw(tick, strip, pos, pixel)
//                        Lighting.Program.Rain -> rain.draw(tick, strip, pos, pixel)
                        Lighting.Program.Earth -> earth.draw(tick, strip, pos, pixel)
                        Lighting.Program.Sparkle -> fastTwinkle.draw(tick, strip, pos, pixel)
                    }
//                    fastTwinkle.draw(tick, strip, pos, pixel)

//                    plainWalk.draw(tick, strip, pos, pixel)
//                    rain.draw(tick, strip, pos, pixel)
//                    walk.draw(tick, strip, pos, pixel)
//                    gradient.draw(tick, strip, pos, pixel)
//                    twinkle.draw(tick, strip, pos, pixel)
//                    blend.draw(tick, strip, pos, pixel)

//                    if (tick % 240L == 0L) {
//                    }
                    flash.draw(tick, strip, pos, pixel)
                    walkFlash.draw(tick, strip, pos, pixel)
                    vWalkFlash.draw(tick, strip, pos, pixel)
                    pulse.draw(tick, strip, pos, pixel)
//                    fastTwinkle.draw(tick, strip, pos, pixel)
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

    Thread(Runnable {
        var tick = 0L
        while (true) {
            lighting.draw(tick++)
            Thread.sleep(16)
        }
    }).start()

    val sc = Scanner(System.`in`).useDelimiter("")
    while (true) {
        val i = sc.next()

//        lighting.pulse()
//        lighting.vWalkFlash()

        val rand = Math.random()

        lighting.switchProgram()



        when {
            rand > 0.66 -> lighting.vWalkFlash()
            rand > 0.33 -> lighting.walkFlash()
//            rand > 0.25 -> lighting.pulse()
            else -> lighting.flash()
        }
//        when (i) {
//            "p" -> lighting.switchProgram()
//            "w" -> lighting.walkFlash()
//            else -> lighting.flash()
//        }

    }
}