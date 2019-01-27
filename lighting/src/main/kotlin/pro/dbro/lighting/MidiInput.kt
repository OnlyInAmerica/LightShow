package pro.dbro.lighting

import java.lang.StringBuilder
import javax.sound.midi.*


/**
 * AKAI MPD32
 * Sliders: Last byte ranges from [0x00, 0x7F]
 *  F1: B00C
 *  F2: B00D
 *  F3: B00E
 *  F4: B00F
 *  F5: B010
 *  F6: B011
 *  F7: B012
 *  F8: B013
 * (Under) Slider Buttons: Last byte is either 0x00 or 0x7F
 *  S1: B020
 *  S2: B021
 *  S3: B022
 *  S4: B023
 *  S5: B024
 *  S6: B025
 *  S7: B026
 *  S8: B027
 * Knobs: Last byte ranges from [0x00, 0x7F]
 *  K1: B016
 *  K2: B017
 *  K3: B018
 *  K4: B019
 *  K5: B01A
 *  K6: B01B
 *  K7: B01C
 *  K8: B01D
 *
 * Pads: Last byte ranges from [0x00, 0x7F]
 *   Pad 15 Onset:   0x914758
 *   Pad 15 Release: 0x814700
 *   Pad 15 Hold:    0xD100 to 0xD17F
 *
 *  Pad Bank A         Pad Bank B
 *  Pad 1
 *   Down - 0x903C      9224
 *   Up   - 0x803C      8224
 *  Pad 2
 *   Down - 0x903E      9225
 *   Up   - 0x803E      8225
 *  Pad 3
 *   Down - 0x9040
 *   Up   - 0x8040
 *  Pad 4
 *   Down - 0x9041
 *   Up   - 0x8041
 *  Pad 5
 *   Down - 0x9043
 *   Up   - 0x8043
 *  Pad 6
 *   Down - 0x9045
 *   Up   - 0x8045
 *  Pad 7
 *   Down - 0x9047
 *   Up   - 0x8047
 *  Pad 8
 *   Down - 0x9048
 *   Up   - 0x8048
 *  Pad 9
 *   Down - 0x913C
 *   Up   - 0x813C
 *  Pad 10
 *   Down - 0x913E
 *   Up   - 0x813E
 *  Pad 11
 *   Down - 0x9140
 *   Up   - 0x8140
 *  Pad 12
 *   Down - 0x9141
 *   Up   - 0x8141
 *
 *  Pad 13-14 Non-responsive
 *
 *  Pad 15
 *   Down  - 0x9147
 *   Up    - 0x8147
 *  Pad 16
 *   Down  - 0x9148
 *   Up    - 0x8148
 * Pad Bank B
 *
 */
class MidiInput {

    enum class InputType {
        Slider, Button, Knob, PadA, PadB
    }

    enum class EventType {
        Press, Alter, Release
    }

    interface MidiListener {
        fun onEvent(inputType: InputType, inputId: Int, eventType: EventType, value: Float)
    }

    /**
     * Enumerate connected MIDI outputs until we find one sending messages
     *
     */
    fun findMidiOuputDevice(): MidiDevice? {
        val deviceInfo = MidiSystem.getMidiDeviceInfo()
        if (deviceInfo.isEmpty()) {
            println("No MIDI devices found")
            return null
        }

        for (info in deviceInfo) {
            println("Testing ${info.vendor} device...")
            try {
                val device = MidiSystem.getMidiDevice(info)
                if (probeOutputDevice(device)) {
                    println("Found candidate device!")
                    return device
                }
            } catch (e: MidiUnavailableException) {
                println("Can't get MIDI device")
                e.printStackTrace()
            }
        }
        return null
    }

    private fun MidiMessage.getInputType(): InputType {
        return InputType.Button
    }

    fun listenToDevice(device: MidiDevice, listener: MidiListener) {
        if (device.maxTransmitters != 0) {
            val transmitter = device.transmitter
            device.open()
            transmitter.receiver = object : Receiver {
                override fun close() {
                    println("Device '${device.deviceInfo.description}' closed")
                }

                override fun send(message: MidiMessage?, timeStamp: Long) {
                    val data = message?.message ?: return
                    println("Message ${message.status} message : ${data.toHex()}")

                    val normVal = if (data.size == 3) {
                        data[2] / 127f
                    } else 0f

                    val b1 = data[0]
                    val b2 = data[1]

                    when (b1) {
                        0xB0.toByte() -> // Sliders, Slider buttons, Knobs
                            when (b2) {
                                in 0x0C..0x13 -> {
                                    // Sliders F1-F8
                                    val sliderId = (b2 - 0x0C) + 1
                                    listener.onEvent(InputType.Slider, sliderId, EventType.Alter, normVal)
                                }
                                in 0x20..0x27 -> {
                                    // Slider Buttons S1-S8
                                    val sliderButtonId = (b2 - 0x20) + 1
                                    listener.onEvent(InputType.Button, sliderButtonId, EventType.Press, normVal)
                                }
                                in 0x16..0x1D -> {
                                    // Knobs K1-K8
                                    val knobId = (b2 - 0x16) + 1
                                    listener.onEvent(InputType.Knob, knobId, EventType.Alter, normVal)
                                }
                            }
                        0x90.toByte() -> {
                            // Pads 1-8
                            val padId = when (b2) {
                                0x3C.toByte() -> 1
                                0x3E.toByte() -> 2
                                0x40.toByte() -> 3
                                0x41.toByte() -> 4
                                0x43.toByte() -> 5
                                0x45.toByte() -> 6
                                0x47.toByte() -> 7
                                0x48.toByte() -> 8
                                else -> 0
                            }
                            listener.onEvent(InputType.PadA, padId, EventType.Press, normVal)
                        }
                        0x91.toByte() -> {
                            // Pads 9-16
                            val padId = when (b2) {
                                0x3C.toByte() -> 9
                                0x3E.toByte() -> 10
                                0x40.toByte() -> 11
                                0x41.toByte() -> 12
                                0x43.toByte() -> 13
                                0x45.toByte() -> 14
                                0x47.toByte() -> 15
                                0x48.toByte() -> 16
                                else -> 0
                            }
                            listener.onEvent(InputType.PadA, padId, EventType.Press, normVal)
                        }
                        0x92.toByte() -> {
                            // Pad bank b depress
                            val padId = (b2 - 0x24) + 1
                            listener.onEvent(InputType.PadB, padId, EventType.Press, normVal)
                        }
                        0x82.toByte() -> {
                            // Pad bank b release
                            val padId = (b2 - 0x24) + 1
                            listener.onEvent(InputType.PadB, padId, EventType.Release, normVal)
                        }
                    }
                }
            }
            // transmitter should be closed, but since this is the only
            // MIDI client of this process / device connection, we can be sloppy
        }
    }

    /**
     * Returns whether any messages can be received from the MIDI device
     * over a short period
     */
    private fun probeOutputDevice(device: MidiDevice, probePeriodMs: Long = 1000): Boolean {
        if (device.maxTransmitters != 0) {
            val transmitter = device.transmitter
            device.open()
            var receivedMsg = false
            val waitingThread = Thread.currentThread()
            transmitter.receiver = object : Receiver {
                override fun close() {
                    println("Device '${device.deviceInfo.description}' closed")
                }

                override fun send(message: MidiMessage?, timeStamp: Long) {
                    println("Device '${device.deviceInfo.description}' got message with len ${message?.length}: ${message?.message?.toHex()}")
                    receivedMsg = true
                    waitingThread.interrupt()
                }
            }
            try {
                Thread.sleep(probePeriodMs)
            } catch (e: InterruptedException) {
                // proceed
            }
            transmitter.close()
            // Turns out some MIDI devices can't be opened after being closed by process,
            // so don't close. Maybe we can rediscover them though...
            return receivedMsg
        }
        return false
    }

}

fun ByteArray.toHex(): String {

    val sb = StringBuilder()
    for (b in this) {
        sb.append(String.format("%02X", b))
    }
    return sb.toString()
}

public fun main(args: Array<String>) {
    val input = MidiInput()
    input.findMidiOuputDevice()?.let {
        println("Listening to candidate device...")
        input.listenToDevice(it, object : MidiInput.MidiListener {
            override fun onEvent(inputType: MidiInput.InputType, inputId: Int, eventType: MidiInput.EventType, value: Float) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
    }
}