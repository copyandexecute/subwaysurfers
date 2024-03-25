package gg.norisk.subwaysurfers.registry

import gg.norisk.subwaysurfers.client.renderer.entity.RampRenderer
import gg.norisk.subwaysurfers.client.renderer.entity.TrafficLightRenderer
import gg.norisk.subwaysurfers.client.renderer.entity.TrainRenderer
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry

object EntityRendererRegistry {
    fun init() {
        EntityRendererRegistry.register(EntityRegistry.TRAFFICLIGHT, ::TrafficLightRenderer)
        EntityRendererRegistry.register(EntityRegistry.TRAIN, ::TrainRenderer)
        EntityRendererRegistry.register(EntityRegistry.RAMP, ::RampRenderer)
    }
}
