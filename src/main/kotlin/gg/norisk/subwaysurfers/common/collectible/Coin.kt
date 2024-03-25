package gg.norisk.subwaysurfers.common.collectible

import gg.norisk.subwaysurfers.subwaysurfers.coins
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents

object Coin : Collectible("coin") {
    override fun onPickupServer(player: ServerPlayerEntity) {
        super.onPickupServer(player)

        // ServerPlayer pickup up a coin
        player.coins++
        player.playSound(SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.5f, 3f)
    }
}