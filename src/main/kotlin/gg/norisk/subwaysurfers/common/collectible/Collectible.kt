@file:OptIn(ExperimentalSerializationApi::class)

package gg.norisk.subwaysurfers.common.collectible

import gg.norisk.subwaysurfers.SubwaySurfers.toId
import gg.norisk.subwaysurfers.common.entity.CollectibleEntity
import gg.norisk.subwaysurfers.network.dto.BlockPosDto
import gg.norisk.subwaysurfers.subwaysurfers.isSubwaySurfers
import kotlinx.serialization.ExperimentalSerializationApi
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.item.Item
import net.minecraft.server.network.ServerPlayerEntity
import net.silkmc.silk.network.packet.c2sPacket

/**
 * A [Collectible] is an item (in minecraft terms an [Entity]) that can be picked up by the player; a power-up or a collectible.
 *
 * Example collectibles: [Coin], key
 *
 * Example [Powerup]s: [Magnet], [Jetpack], [Hoverboard]
 */
open class Collectible(
    /** Unique identifier for linking resources like model, texture and animation. */
    val id: String,
) {
    /** packet that is sent from a client to the server to indicate that the item was picked up */
    val pickupPacket = c2sPacket<BlockPosDto>("item_${id}_pickup".toId())

    init {
        pickupPacket.receiveOnServer { _, context ->
            // TODO position check; currently we just assume that the item exists and the player really picked it up

            context.player.takeIf { it.isSubwaySurfers }?.let(::onPickupServer)
        }
    }

    /** Callback called on the **server** when the client notified the server about picking up the item. */
    open fun onPickupServer(player: ServerPlayerEntity) {}

    // ----------

    var item: Item? = null

    var entityType: EntityType<CollectibleEntity>? = null
}