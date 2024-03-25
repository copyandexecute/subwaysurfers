package gg.norisk.subwaysurfers.common.collectible

import gg.norisk.subwaysurfers.common.entity.CollectibleEntity
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.silkmc.silk.core.entity.directionVector
import net.silkmc.silk.core.entity.modifyVelocity
import kotlin.time.Duration.Companion.seconds

object Magnet : Powerup("magnet", 10.seconds, EquipmentSlot.OFFHAND, isArmor = false) {
    override fun onPickupServer(player: ServerPlayerEntity) {
        super.onPickupServer(player)

        // ServerPlayer pickup up a magnet
        player.playSound(SoundEvents.ENTITY_CAT_EAT, SoundCategory.PLAYERS, 0.5f, 3f)
    }

    override fun onTickClient(player: ClientPlayerEntity) {
        val coinsInRange = player.world.getEntitiesByClass(CollectibleEntity::class.java, player.boundingBox.expand(5.0)) {
            it.type == Coin.entityType // check if it's a coin
        }

        for (coin in coinsInRange) {
            val direction =
                player.eyePos.add(player.directionVector.normalize().multiply(2.0)).subtract(coin.pos).normalize().multiply(0.9)

            coin.modifyVelocity(direction)
            if (coin.distanceTo(player) < 2) {
                coin.onPlayerCollision(player)
            }
        }
    }
}