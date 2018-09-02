package pro.dbro.lighting

import com.heroicrobot.dropbit.registry.DeviceRegistry

interface Show {
    fun setup(registry: DeviceRegistry)
    fun draw(tick: Long)
}