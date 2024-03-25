package gg.norisk.subwaysurfers.server.mechanics

import gg.norisk.subwaysurfers.common.collectible.Jetpack
import gg.norisk.subwaysurfers.common.collectible.hasPowerUp
import gg.norisk.subwaysurfers.server.ServerConfig
import gg.norisk.subwaysurfers.subwaysurfers.isSubwaySurfers
import kotlinx.coroutines.Job
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.entity.attribute.EntityAttributes
import net.silkmc.silk.core.kotlin.ticks
import net.silkmc.silk.core.task.infiniteMcCoroutineTask

object SpeedManager {
    const val vanillaSpeed = 0.10000000149011612
    var infiniteRoutine: Job? = null

    fun init() {
        ServerLifecycleEvents.SERVER_STARTED.register {
            infiniteRoutine?.cancel()
            infiniteRoutine = infiniteMcCoroutineTask(period = 20.ticks) {
                it.playerManager.playerList.filter {
                    it.isSubwaySurfers && !it.hasPowerUp(Jetpack)
                }.forEach {
                    // Gradually increase movement speed over time
                    it.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)?.let { attr ->
                        attr.baseValue = (attr.baseValue + ServerConfig.config.surferAcceleration).coerceIn(
                            ServerConfig.config.surferBaseSpeed,
                            ServerConfig.config.surferMaxSpeed
                        )
                    }
                }
            }
        }
    }
}
