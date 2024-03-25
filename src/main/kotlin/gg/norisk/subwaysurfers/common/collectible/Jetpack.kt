package gg.norisk.subwaysurfers.common.collectible

import gg.norisk.subwaysurfers.client.ClientSettings
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.particle.ParticleTypes
import net.minecraft.util.math.MathHelper
import kotlin.time.Duration.Companion.seconds

object Jetpack : Powerup("jetpack", 5.seconds, EquipmentSlot.CHEST) {
    override fun onTickClient(player: ClientPlayerEntity) {
        // add particles
        repeat(2) {
            player.world.addParticle(
                ParticleTypes.FLAME,
                player.x + 0.1 - it * 0.2,
                player.y + 0.6,
                player.z + 0.1,
                0.0,
                0.0,
                0.0
            )
        }
    }

    fun getY(player: PlayerEntity, origY: Double): Double {
        if (player.hasPowerUp(this)) {
            val y = ClientSettings.startPos?.y ?: return origY
            val targetY = y + 15.0
            val currentY = MathHelper.lerp(0.1, origY, targetY)
            return currentY
        }
        return origY
    }
}