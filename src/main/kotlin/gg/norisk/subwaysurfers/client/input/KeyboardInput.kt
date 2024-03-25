package gg.norisk.subwaysurfers.client.input

import gg.norisk.subwaysurfers.client.ClientSettings
import gg.norisk.subwaysurfers.entity.TrainEntity
import gg.norisk.subwaysurfers.event.events.KeyEvents
import gg.norisk.subwaysurfers.network.c2s.MovementType
import gg.norisk.subwaysurfers.network.c2s.movementTypePacket
import gg.norisk.subwaysurfers.network.c2s.punishPacketC2S
import gg.norisk.subwaysurfers.subwaysurfers.dashStrength
import gg.norisk.subwaysurfers.subwaysurfers.rail
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d

object KeyboardInput {
    fun init() {
        sendClientInput()
    }

    private fun sendClientInput() {
        KeyEvents.keyPressedOnce.listen {
            val player = MinecraftClient.getInstance().player ?: return@listen
            if (!ClientSettings.isEnabled()) {
                return@listen
            }
            if (it.client.options.leftKey.matchesKey(it.key, it.scanCode)) {
                handleLeftDash(player)
            } else if (it.client.options.rightKey.matchesKey(it.key, it.scanCode)) {
                handleRightDash(player)
            } else if (it.client.options.jumpKey.matchesKey(it.key, it.scanCode)) {
                //Jup thats kinda rough buddy
                if (player.velocity.y == -0.2940000174045565) {
                    movementTypePacket.send(MovementType.JUMP)
                }
            } else if (it.client.options.sneakKey.matchesKey(it.key, it.scanCode)) {
                movementTypePacket.send(MovementType.SLIDE)
            }
        }
    }

    private fun handleRightDash(player: ClientPlayerEntity) {
        if (player.rail != 2) {
            if (player.punishDash(
                    Vec3d(
                        player.x + -player.dashStrength,
                        player.y,
                        player.z,
                    )
                )
            ) {
                punishPacketC2S.send(Unit)
            } else {
                movementTypePacket.send(MovementType.RIGHT)
            }
        } else {
            movementTypePacket.send(MovementType.RIGHT)
        }
    }

    private fun handleLeftDash(player: ClientPlayerEntity) {
        if (player.rail != 0) {
            if (player.punishDash(
                    Vec3d(
                        player.x + player.dashStrength,
                        player.y,
                        player.z,
                    )
                )
            ) {
                punishPacketC2S.send(Unit)
            } else {
                movementTypePacket.send(MovementType.LEFT)
            }
        } else {
            movementTypePacket.send(MovementType.LEFT)
        }
    }

    private fun PlayerEntity.punishDash(position: Vec3d): Boolean {
        return world.getOtherEntities(this, Box.from(position)) {
            it is TrainEntity
        }.isNotEmpty()
    }
}
