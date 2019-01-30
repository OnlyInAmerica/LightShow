package pro.dbro.lighting

import com.heroicrobot.dropbit.devices.pixelpusher.Pixel
import com.heroicrobot.dropbit.registry.DeviceRegistry
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pro.dbro.lighting.effects.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.sin

const val maxStrips = 8

fun forEachInBitMask(bitSet: Int, iterator: (Int) -> Unit) {
    for (stripId in 0 until maxStrips) {
        if (bitSet and (1 shl stripId) != 0) {
            iterator.invoke(stripId)
        }
    }
}

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

    // Pixel additives (used as flash argument)
    private val bigBoost = Pixel(0.toByte(), 0.toByte(), 0.toByte(), 255.toByte(), 255.toByte())
    private val mediumBoost = Pixel(0.toByte(), 0.toByte(), 0.toByte(), 150.toByte(), 20.toByte())
    private val smallBoost = Pixel(0.toByte(), 0.toByte(), 0.toByte(), 70.toByte(), 2.toByte())

    // Variable flash pixel
    private val pixelFlash = Pixel(0.toByte(), 0.toByte(), 0.toByte(), 255.toByte(), 255.toByte())

    enum class Program {
        Fire, VGradient, HGradient, /*Blend,*/ Purp, Earth, Sparkle, Rainbow, Off //, Rain
    }

    private var currentProgram = Program.Earth

    private val nextProgramCol = hashMapOf(
            Pair(Program.VGradient, pixelRed),
            Pair(Program.HGradient, pixelYellow),
            Pair(Program.Fire, pixelGreen),
//            Pair(Program.Blend, pixelPurp),
            Pair(Program.Purp, pixelGreen),
            Pair(Program.Earth, pixelWhite),
            Pair(Program.Sparkle, pixelRed),
            Pair(Program.Rainbow, pixelFire)
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

    val rainbow = Rainbow()

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
            //println("Registry changed!")
            if (updatedDevice != null) {
                //println("Device change: $updatedDevice")
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

    fun setFlashPixel(value: Float, useWhitePixels: Boolean = true) {

        val t = 2 * Math.PI * (value)

        val tOffset = if (useWhitePixels) (2 * Math.PI) / 5 else (2 * Math.PI) / 3

        val red = sin(t) * 127 + 128
        val green = sin(t + tOffset) * 127 + 128
        val blue = sin(t + 2 * tOffset) * 127 + 128
        val white = sin(t + 2 * tOffset) * 127 + 128
        val orange = sin(t + 2 * tOffset) * 127 + 128

        pixelFlash.red = red.toByte()
        pixelFlash.green = green.toByte()
        pixelFlash.blue = blue.toByte()

        if (useWhitePixels) {
            pixelFlash.white = white.toByte()
            pixelFlash.orange = orange.toByte()
        } else {
            pixelFlash.white = 0
            pixelFlash.orange = 0
        }
    }

    fun smallFlash() {
        println("Small flash")
        flash(boostPixel = mediumBoost)
    }

    fun largeFlash() {
        println("Large flash")
        flash(boostPixel = bigBoost)
    }

    fun flash(boostPixel: Pixel?, intensity: Float = flashIntensity) {
        flash.flash(intensity, boostPixel = boostPixel, durationTicks = 120)
    }

    fun walkFlash(stripIdMask: Int = 0b11111111, durationTicks: Long = 60) {
        println("H Walk flash")
//        val nextProgPixel = Pixel(nextProgramCol[currentProgram])
//        nextProgPixel.white = 200.toByte()
        walkFlash.flash(
                intensity = flashIntensity,
                flashPixel = pixelFlash,
                durationTicks = durationTicks,
                stripIdxMask = stripIdMask)
    }

    /**
     * If [walkFlash] is called with durationTicks 0, decay will begin on this call
     */
    fun unWalkFlash(durationTicks: Long = 60,
                    stripIdMask: Int) {
        walkFlash.unFlash(durationTicks, stripIdMask)
    }

    fun vWalkFlash() {
        println("V Walk flash")
//        val nextProgPixel = Pixel(nextProgramCol[currentProgram])
//        nextProgPixel.white = 200.toByte()
        vWalkFlash.flash(flashIntensity, flashPixel = pixelFlash, durationTicks = 90)
    }

    fun switchProgram() {
        val values = Program.values()
        val lastOrdinal = values.last().ordinal
        val newOrdinal = (currentProgram.ordinal + 1) % (lastOrdinal + 1)
        switchProgram(newProgram = values[newOrdinal])
    }

    fun switchProgram(newProgram: Program) {
        currentProgram = newProgram
        println("Changing program to " + currentProgram.name)
    }

    fun pulse(intensity: Float = flashIntensity) {
        pulse.pulse(intensity = intensity, boostPixel = smallBoost)
    }

    /**
     *
     * @param tick should be regular and monotonic. Used for transitions.
     * @param programTick can change in timescale and direction for fun times.
     */
    fun draw(tick: Long, programTick: Long = tick) {
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

            strips.forEach { strip ->

                if (cycleFlashPixelHue) {
                    pixelFlash.rainbow(2 * Math.PI * (tick / 60.toFloat()) + (strip.stripNumber / 7f) * 2 * Math.PI)
                }

                for (pos in pixelStartIdx until strip.length) {

                    when (currentProgram) {
                        Program.VGradient -> gradient.draw(programTick, strip, pos, pixel)
                        Program.HGradient -> hGradient.draw(programTick, strip, pos, pixel)
                        Program.Fire -> fireWw/*fire*/.draw(programTick, strip, pos, pixel)
//                        Lighting.Program.Blend -> blend.draw(programTick, strip, pos, pixel)
                        Program.Purp -> purp.draw(programTick, strip, pos, pixel)
//                        Lighting.Program.Walk -> walk.draw(programTick, strip, pos, pixel)
//                        Lighting.Program.Rain -> rain.draw(programTick, strip, pos, pixel)
                        Program.Earth -> earth.draw(programTick, strip, pos, pixel)
                        Program.Sparkle -> fastTwinkle.draw(programTick, strip, pos, pixel)
                        Program.Rainbow -> rainbow.draw(programTick, strip, pos, pixel)
                        Program.Off -> pixel.setColor(pixelOff)
                    }
//                    fastTwinkle.draw(tick, strip, pos, pixel)

//                    plainWalk.draw(tick, strip, pos, pixel)
//                    rain.draw(tick, strip, pos, pixel)
//                    walk.draw(tick, strip, pos, pixel)
//                    gradient.draw(tick, strip, pos, pixel)
//                    twinkle.draw(tick, strip, pos, pixel)
//                    blend.draw(tick, strip, pos, pixel)
                    flash.draw(tick, strip, pos, pixel)
                    walkFlash.draw(tick, strip, pos, pixel)
                    vWalkFlash.draw(tick, strip, pos, pixel)
                    pulse.draw(tick, strip, pos, pixel)
//                    fastTwinkle.draw(tick, strip, pos, pixel)
                    strip.setPixel(pixel, pos)
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

var programTickFreeze = false

var programTickAdj = 1L
    set(value) {
        println("TickAdj $value")
        field = value
    }

var programTickOffset = 0

val controlServer = ControlServer()
var controlServerMod = 1
    set(value) {
        if (value == 0) {
            // modulus cannot be zero
            println("controlServerMod at min: $value")
            return
        }
        println("controlServerMod at: $value")
        field = value
    }
var controlServerPing = true
var controlServerCount = 0

var lastCommand = ""

public fun main(args: Array<String>) {

    val lighting = Lighting()

    val input = MidiInput()
    input.findMidiOuputDevice()?.let {
        input.listenToDevice(it, object : MidiInput.MidiListener {
            override fun onEvent(inputType: MidiInput.InputType,
                                 inputId: Int,
                                 eventType: MidiInput.EventType,
                                 value: Float) {
                handleMidiCommand(lighting, inputType, inputId, eventType, value)
            }
        })
    }


    // Render thread
    Thread(Runnable {
        var tick = 0L
        var renderTick = 0L
        while (true) {
            synchronized(lighting) {
                if (!programTickFreeze) {
                    renderTick += programTickAdj
                }
                lighting.draw(tick++, renderTick + programTickOffset)
            }
            Thread.sleep(16)
        }
    }).start()

    // Control server thread
    controlServer.listenAsync()
    controlServer.listener = { command ->

        fun activate(command: String) {
            val commandArr = command.split(",")
            val intensity = commandArr[1].toFloat()
            val command = commandArr[0]
            println("Activate with '$command' intensity $intensity")
            handleKeyCommand(lighting, command, intensity)
        }

        if (controlServerPing) {
            if (controlServerCount++ % controlServerMod == 0) {
                activate(command)
            }
        } else {
            if (controlServerCount++ % controlServerMod != 0) {
                activate(command)
            }
        }
    }

    val sc = Scanner(System.`in`).useDelimiter("")
    while (true) {
        var i = sc.nextLine()
        println("Read '$i' last: '$lastCommand'")

        if (i == "") {
            println("Replaying $lastCommand")
            i = lastCommand
        }

        handleKeyCommand(lighting, i)
    }
}

val pixelWhite = Pixel(0.toByte(), 0.toByte(), 0.toByte(), 255.toByte(), 255.toByte())
var useWhitePixelForFlash = false
var cycleFlashPixelHue = false

const val stripIdMaskNone: Int = 0b00000000
const val stripIdMaskAll: Int = 0b11111111

const val crescendoStepMinMs = 10
const val crescendoStepMaxMs = 250
var crescendoStepMs: Int = 60

fun handleMidiCommand(lighting: Lighting,
                      inputType: MidiInput.InputType,
                      inputId: Int,
                      eventType: MidiInput.EventType,
                      value: Float) {

    synchronized(lighting) {
        if (inputType == MidiInput.InputType.Slider) {
            if (inputId == 8) {
                // Time scale
                programTickAdj = (value * 40f).toLong() - 20
            } else if (inputId == 7) {
                // Time offset
                programTickOffset = ((value * 3000) - 1500).toInt()
            } else if (inputId == 6) {
                // HWalkFlash mod
                lighting.walkFlash.pixelMod = (value * 10).toInt() + 1
            } else if (inputId == 5) {
                // Flash pixel hue
                lighting.setFlashPixel(value, useWhitePixelForFlash)
            } else if (inputId == 4) {
                // Crescendo interval
                crescendoStepMs =
                        (((crescendoStepMaxMs - crescendoStepMinMs) * value)
                                + crescendoStepMinMs).toInt()
            }
        } else if (inputType == MidiInput.InputType.Knob) {
            // Some of the knobs on this pad are pretty janky: Once
            // actuated, they fire spurious events for a while.
            // This can cause painful glitching. Probably have to filter
            // this event before we use it.

            //programTickOffset = ((value * 600) - 300).toInt()
        } else if (inputType == MidiInput.InputType.Button) {
            if (inputId == 8) {
                // Time
                println("Time buttton value $value")
                programTickFreeze = (value == 0f)
            } else if (inputId == 7) {
                val on = value == 1f
                if (on) {
                    lighting.rainbow.mode = Rainbow.Mode.Vertical
                } else {
                    lighting.rainbow.mode = Rainbow.Mode.Strip
                }
            } else if (inputId == 4) {
                // Toggle use of white LEDs in flash
                useWhitePixelForFlash = value == 1f
            } else if (inputId == 5) {
                // Cycle hue vs keep static
                cycleFlashPixelHue = value == 1f
            }
        } else if (inputType == MidiInput.InputType.PadA) {
            when (inputId) {
                2 -> lighting.walkFlash()
                6 -> lighting.vWalkFlash()
                3 -> lighting.flash(boostPixel = pixelWhite, intensity = value)
                4 -> lighting.pulse(intensity = value)
                7 -> lighting.flash(boostPixel = pixelWhite, intensity = 0.20f)
                9 -> lighting.switchProgram(Lighting.Program.Earth)
                10 -> lighting.switchProgram(Lighting.Program.Fire)
                11 -> lighting.switchProgram(Lighting.Program.Purp)
                12 -> lighting.switchProgram(Lighting.Program.Rainbow)
                15 -> lighting.switchProgram(Lighting.Program.Sparkle)
                16 -> lighting.switchProgram(Lighting.Program.Off)
            }
        } else if (inputType == MidiInput.InputType.PadB) {
            if (inputId < 9) {
                // Trigger individual strips
                val eventStripIdMask: Int = 1 shl (inputId - 1)
                if (eventType == MidiInput.EventType.Press) {
                    lighting.walkFlash(eventStripIdMask, durationTicks = 0L)
                } else if (eventType == MidiInput.EventType.Release) {
                    lighting.unWalkFlash(stripIdMask = eventStripIdMask)
                }
            } else if (inputId <= 12) {
                // Trigger strip pairs
                val firstStripIdx = inputId - 9
                val eventStripIdMask: Int =
                        (1 shl (firstStripIdx * 2)) or
                                (1 shl (firstStripIdx * 2 + 1))
                if (eventType == MidiInput.EventType.Press) {
                    lighting.walkFlash(eventStripIdMask, durationTicks = 0L)
                } else if (eventType == MidiInput.EventType.Release) {
                    lighting.unWalkFlash(stripIdMask = eventStripIdMask)
                }
            } else if (inputId == 15) {
                // Trigger all
                if (eventType == MidiInput.EventType.Press) {
                    lighting.walkFlash(stripIdMaskAll, durationTicks = 0L)
                } else if (eventType == MidiInput.EventType.Release) {
                    lighting.unWalkFlash(stripIdMask = stripIdMaskAll)
                }
            }
        } else if (inputType == MidiInput.InputType.PadC) {
            if (inputId == 1) {
                // Left to Right Crescendo
                for (i in 0 until maxStrips) {
                    val stripIdMask = 1 shl i
                    GlobalScope.launch {
                        delay(crescendoStepMs * (i + 1L))
                        lighting.walkFlash(stripIdMask)
                    }
                }
            } else if (inputId == 4) {
                // Right to Left Crescendo
                for (i in 0 until maxStrips) {
                    val stripIdMask = 1 shl (maxStrips - 1 - i)
                    GlobalScope.launch {
                        delay(crescendoStepMs * (i + 1L))
                        lighting.walkFlash(stripIdMask)
                    }
                }
            } else if (inputId == 2) {
                // Middle out Crescendo
                val midPt = (maxStrips - 1) / 2
                for (i in 0..midPt) {
                    val stripIdMask = (1 shl (midPt + 1 + i)) or
                            (1 shl (midPt - i))
                    GlobalScope.launch {
                        delay(crescendoStepMs * (i + 1L))
                        lighting.walkFlash(stripIdMask)
                    }
                }
            } else if (inputId == 3) {
                // Edges in Crescendo
                val midPt = (maxStrips - 1) / 2
                for (i in 0..midPt) {
                    val stripIdMask = (1 shl i) or
                            (1 shl (maxStrips - 1 - i))
                    GlobalScope.launch {
                        delay(crescendoStepMs * (i + 1L))
                        lighting.walkFlash(stripIdMask)
                    }
                }
            }
        }
    }
}

fun handleKeyCommand(lighting: Lighting, command: String, intensity: Float = 0.8f) {
    when (command) {
        "'" -> {
            println("Major")

            lighting.switchProgram()
            randEffect(lighting)
        }
        "c0" -> controlServerMod = 1
        "cx" -> controlServerMod--
        "cv" -> controlServerMod++
        "cp" -> controlServerPing = !controlServerPing
        "f" -> lighting.smallFlash()
        "g" -> lighting.largeFlash()
        "u" -> lighting.pulse(intensity)
        "w" -> lighting.walkFlash()
        "v" -> lighting.vWalkFlash()
        "s" -> {
            randEffect(lighting)
            lighting.switchProgram(Lighting.Program.Sparkle)
        }
        "e" -> {
            randEffect(lighting)
            lighting.switchProgram(Lighting.Program.Earth)
        }
        "p" -> {
            randEffect(lighting)
            lighting.switchProgram(Lighting.Program.Purp)
        }
        "i" -> {
            randEffect(lighting)
            lighting.switchProgram(Lighting.Program.Fire)
        }
        "b" -> {
            randEffect(lighting)
            lighting.switchProgram(Lighting.Program.HGradient)
        }
        "o" -> {
            randEffect(lighting)
            lighting.switchProgram(Lighting.Program.VGradient)
        }
        "r" -> {
            randEffect(lighting)
            lighting.switchProgram(Lighting.Program.Rainbow)
        }
        "q" -> {
            println("Quitting...")
            controlServer.listenRequested = false
            Thread.sleep(16)
            System.exit(0)
        }
        "=" -> programTickAdj += 3
        "-" -> programTickAdj -= 3
        "0" -> programTickAdj = 1
        else -> {
            println("Minor")
            lighting.pulse()
        }
    }
    lastCommand = command
}

fun randEffect(lighting: Lighting) {
    val rand = Math.random()
    when {
        rand > 0.66 -> lighting.vWalkFlash()
        rand > 0.33 -> lighting.walkFlash()
        else -> lighting.flash(null)
    }
}