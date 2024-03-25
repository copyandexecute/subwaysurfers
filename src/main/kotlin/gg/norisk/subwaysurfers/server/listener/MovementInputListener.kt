package gg.norisk.subwaysurfers.server.listener

import gg.norisk.subwaysurfers.common.collectible.Jetpack
import gg.norisk.subwaysurfers.common.collectible.hasPowerUp
import gg.norisk.subwaysurfers.network.c2s.MovementType
import gg.norisk.subwaysurfers.network.c2s.movementTypePacket
import gg.norisk.subwaysurfers.network.s2c.AnimationPacket
import gg.norisk.subwaysurfers.network.s2c.playAnimationS2C
import gg.norisk.subwaysurfers.registry.SoundRegistry
import gg.norisk.subwaysurfers.server.ServerConfig
import gg.norisk.subwaysurfers.server.mechanics.PunishManager.punishHit
import gg.norisk.subwaysurfers.subwaysurfers.dashStrength
import gg.norisk.subwaysurfers.subwaysurfers.isSliding
import gg.norisk.subwaysurfers.subwaysurfers.rail
import net.minecraft.network.packet.s2c.play.PositionFlag
import net.minecraft.sound.SoundCategory
import net.minecraft.util.math.Vec3d
import net.silkmc.silk.core.entity.modifyVelocity
import net.silkmc.silk.core.kotlin.ticks
import net.silkmc.silk.core.task.mcCoroutineTask

object MovementInputListener {
    fun init() {
        movementTypePacket.receiveOnServer { packet, context ->
            val player = context.player

            if (packet == MovementType.SLIDE) {
                if (player.hasPowerUp(Jetpack)) return@receiveOnServer
                if (!player.isSliding) {
                    player.isSliding = true
                    playAnimationS2C.sendToAll(AnimationPacket(player.uuid, "subway_jump"))
                    // add downward velocity to player
                    player.modifyVelocity(Vec3d(0.0, -1.0, 0.0))
                    (player.world.playSoundFromEntity(
                        null,
                        player,
                        SoundRegistry.WHOOSH,
                        SoundCategory.PLAYERS,
                        0.4f,
                        0.8f
                    ))
                    mcCoroutineTask(delay = ServerConfig.config.slidingTicks.ticks) {
                        player.isSliding = false
                    }
                }
            } else if (packet == MovementType.JUMP) {
                playAnimationS2C.sendToAll(AnimationPacket(player.uuid, "subway_jump"))
                (player.world.playSoundFromEntity(
                    null,
                    player,
                    SoundRegistry.WHOOSH,
                    SoundCategory.PLAYERS,
                    0.4f,
                    0.8f
                ))
                val jumpStrength = ServerConfig.config.jumpStrength
                player.modifyVelocity(
                    Vec3d(0.0, jumpStrength, 0.0)
                )
            } else if (player.rail == 0 && packet == MovementType.LEFT) {
                player.punishHit()
            } else if (player.rail == 2 && packet == MovementType.RIGHT) {
                player.punishHit()
            } else {
                playAnimationS2C.sendToAll(AnimationPacket(player.uuid, "subway_dash"))

                player.rail += (if (packet == MovementType.LEFT) -1 else 1)

                val centerPos = player.pos
                // use absolute x coordinate to reset x when player bugs a bit to the left or right of a rail
                val newX = ServerConfig.config.startPos.x + (-player.rail + 1) * player.dashStrength
                val newPos = Vec3d(
                    newX,
                    player.y,
                    centerPos.z,
                )

                (player.world.playSoundFromEntity(
                    null,
                    player,
                    SoundRegistry.WHOOSH,
                    SoundCategory.PLAYERS,
                    0.4f,
                    0.8f
                ))

                player.teleport(
                    player.serverWorld,
                    newPos.x,
                    newPos.y,
                    newPos.z,
                    PositionFlag.VALUES.toSet(),
                    player.yaw,
                    player.pitch
                )
            }
        }
    }
}
