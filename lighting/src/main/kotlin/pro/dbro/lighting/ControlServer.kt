package pro.dbro.lighting

import java.net.*

class ControlServer {

    val listenPort = 8422

    private lateinit var socket: DatagramSocket

    var listenRequested = true
    var listener: ((command: String) -> Unit)? = null

    fun listen() {
        socket = DatagramSocket(listenPort)
        println("Control server bound: ${socket.isBound} port: $listenPort")

        val recvBuffer = ByteArray(1024)
        val receivePacket = DatagramPacket(recvBuffer, 0, recvBuffer.size)

        while (listenRequested) {
            socket.receive(receivePacket)
            receivePacket.length
            val command = String(recvBuffer, receivePacket.offset, receivePacket.length)
            println("Got command: $command")
            listener?.invoke(command)
        }
        socket.close()
    }

    fun listenAsync() {
        Thread(Runnable {
            listen()
        }).start()
    }
}