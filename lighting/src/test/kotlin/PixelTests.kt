import com.heroicrobot.dropbit.devices.pixelpusher.Pixel
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import pro.dbro.lighting.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PixelTests {

    @Test
    fun tween() {
        val pixelOff = Pixel()
        val pixelRed = Pixel(255.toByte(), 13.toByte(), 120.toByte())

        pixelOff.tween(pixelRed, 0.5f)

        Assertions.assertEquals(pixelOff.redInt(), pixelRed.redInt() / 2)
        Assertions.assertEquals(pixelOff.greenInt(), pixelRed.greenInt() / 2)
        Assertions.assertEquals(pixelOff.blueInt(), pixelRed.blueInt() / 2)

        val pixelBlue = Pixel(10.toByte(), 13.toByte(), 255.toByte())
        val pixel = Pixel()
        pixel.tween(pixelRed, 0.5f, pixelBlue, 0.5f)
        Assertions.assertEquals(pixel.redInt(), (pixelRed.redInt() + pixelBlue.redInt()) / 2)
    }

    @Test
    fun ceil() {
        val pixel = Pixel(127.toByte(), 13.toByte(), 120.toByte())

        val ceil = pixel.ceil()

        Assertions.assertEquals(ceil.redInt(), 255)
        Assertions.assertEquals(ceil.greenInt(), 26)
        Assertions.assertEquals(ceil.blueInt(), 240)
    }
}