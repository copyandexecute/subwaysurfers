package gg.norisk.subwaysurfers.common.collectible

import gg.norisk.subwaysurfers.subwaysurfers.isSubwaySurfers
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.silkmc.silk.core.server.players
import kotlin.time.Duration

/**
 * An ability or power-up (e.g. jetpack, hoverboard, magnet).
 */
open class Powerup(
    id: String,
    /**
     * How long the power up lasts, without modifiers (e.g. some kind of upgrade that makes all abilities last longer).
     */
    val baseDuration: Duration,
    /**
     * The slot in which the power-up should be equipped while active.
     */
    val equipmentSlot: EquipmentSlot,
    /** can the power up be equipped as armor (needs armor model!) */
    val isArmor: Boolean = true,
) : Collectible(id) {
    /** data tracker that is synced from server to client to indicate until when a power-up will last */
    val endTimestampTracker: TrackedData<Long> = DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.LONG)

    override fun onPickupServer(player: ServerPlayerEntity) {
        val duration = calculateDuration(player)
        val endTimestamp = player.world.time + (duration.inWholeMilliseconds / 50)

        // todo equip the power-up on the specified armor slot (maybe only do this on the client?)

        // update the end timestamp for client(s)
        player.dataTracker.set(endTimestampTracker, endTimestamp)
    }

    open fun calculateDuration(player: ServerPlayerEntity): Duration {
        // Here we could implement custom behaviour, e.g. if the player has some kind of achievement that
        // increases the duration for all power-ups.
        return baseDuration
    }

    open fun onTickClient(player: ClientPlayerEntity) {}
    open fun onTickServer(player: ServerPlayerEntity) {}

    init {
        ClientTickEvents.END_CLIENT_TICK.register {
            if (it.player?.hasPowerUp(this) == true) {
                onTickClient(MinecraftClient.getInstance().player!!)
            }
        }
        ServerTickEvents.START_SERVER_TICK.register { server ->
            server.players.forEach {
                if (it.hasPowerUp(this)) {
                    onTickServer(it)
                }
            }
        }
    }

    fun isActiveFor(player: PlayerEntity) = player.dataTracker.get(endTimestampTracker) > player.world.time
}

fun PlayerEntity.hasPowerUp(powerUp: Powerup) = isSubwaySurfers && powerUp.isActiveFor(this)