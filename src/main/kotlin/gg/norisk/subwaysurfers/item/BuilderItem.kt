package gg.norisk.subwaysurfers.item

import gg.norisk.subwaysurfers.common.collectible.*
import gg.norisk.subwaysurfers.entity.TrainEntity
import gg.norisk.subwaysurfers.extensions.next
import gg.norisk.subwaysurfers.registry.EntityRegistry
import io.wispforest.owo.ui.core.Color
import net.minecraft.block.BlockState
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.SpawnReason
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import net.silkmc.silk.core.item.setLore
import net.silkmc.silk.core.text.literal
import net.silkmc.silk.core.text.literalText

class BuilderItem(settings: Settings) : Item(settings) {
    override fun hasGlint(itemStack: ItemStack): Boolean {
        return true
    }

    private enum class Action(
        val callback: (
            playerEntity: PlayerEntity,
            blockState: BlockState,
            worldAccess: WorldAccess,
            blockPos: BlockPos,
            bl: Boolean,
            itemStack: ItemStack
        ) -> Unit
    ) {
        TRAIN(callback = { player, blockState, worldAccess, blockPos, bl, itemStack ->
            EntityRegistry.TRAIN.spawn(worldAccess as ServerWorld, blockPos, SpawnReason.SPAWN_EGG)
        }),
        RAMP(callback = { player, blockState, worldAccess, blockPos, bl, itemStack ->
            EntityRegistry.RAMP.spawn(worldAccess as ServerWorld, blockPos, SpawnReason.SPAWN_EGG)
        }),
        COIN(callback = { player, blockState, worldAccess, blockPos, bl, itemStack ->
            Coin.entityType?.spawn(worldAccess as ServerWorld, blockPos, SpawnReason.SPAWN_EGG)
        }),
        MAGNET(callback = { player, blockState, worldAccess, blockPos, bl, itemStack ->
            Magnet.entityType?.spawn(worldAccess as ServerWorld, blockPos, SpawnReason.SPAWN_EGG)
        }),
        HOVERBOARD(callback = { player, blockState, worldAccess, blockPos, bl, itemStack ->
            Hoverboard.entityType?.spawn(worldAccess as ServerWorld, blockPos, SpawnReason.SPAWN_EGG)
        }),
        BOOTS(callback = { player, blockState, worldAccess, blockPos, bl, itemStack ->
            Boots.entityType?.spawn(worldAccess as ServerWorld, blockPos, SpawnReason.SPAWN_EGG)
        }),
        JETPACK(callback = { player, blockState, worldAccess, blockPos, bl, itemStack ->
            Jetpack.entityType?.spawn(worldAccess as ServerWorld, blockPos, SpawnReason.SPAWN_EGG)
        }),
        TRAFFICLIGHT(callback = { player, blockState, worldAccess, blockPos, bl, itemStack ->
            EntityRegistry.TRAFFICLIGHT.spawn(worldAccess as ServerWorld, blockPos, SpawnReason.SPAWN_EGG)
        }),
    }

    override fun canMine(
        blockState: BlockState,
        world: World,
        blockPos: BlockPos,
        playerEntity: PlayerEntity
    ): Boolean {
        if (!world.isClient) {
            this.use(playerEntity, blockState, world, blockPos, false, playerEntity.getStackInHand(Hand.MAIN_HAND))
        }

        return false
    }

    override fun useOnBlock(itemUsageContext: ItemUsageContext): ActionResult {
        val playerEntity = itemUsageContext.player
        val world = itemUsageContext.world
        if (!world.isClient && playerEntity != null) {
            val blockPos = itemUsageContext.blockPos
            val blockState = world.getBlockState(blockPos)
            val direction = itemUsageContext.side
            val blockPos2 = if (blockState.getCollisionShape(world, blockPos).isEmpty) {
                blockPos
            } else {
                blockPos.offset(direction)
            }
            if (!this.use(
                    playerEntity,
                    world.getBlockState(blockPos),
                    world,
                    blockPos2,
                    true,
                    itemUsageContext.stack
                )
            ) {
                return ActionResult.FAIL
            }
        }

        return ActionResult.success(world.isClient)
    }

    override fun useOnEntity(
        itemStack: ItemStack,
        playerEntity: PlayerEntity,
        livingEntity: LivingEntity,
        hand: Hand
    ): ActionResult {
        if (itemStack.action() == Action.TRAIN && livingEntity is TrainEntity) {
            if (!playerEntity.world.isClient && livingEntity.isAlive) {
                val train = livingEntity as TrainEntity
                train.variation = if (train.variation + 1 >= TrainEntity.TRAIN_TYPES) {
                    0
                } else {
                    train.variation + 1
                }
            }
            return ActionResult.success(playerEntity.world.isClient)
        } else {
            return ActionResult.PASS
        }
    }

    private fun ItemStack.action(): Action {
        val holder = getOrCreateSubNbt("holder")
        return Action.valueOf(holder.get("action")?.asString() ?: Action.values().first().name)
    }

    override fun use(world: World, playerEntity: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        if (!world.isClient) {
            playerEntity.getStackInHand(hand).apply {
                val holder = getOrCreateSubNbt("holder")
                val action = action()
                val nextAction = action.next()
                holder.putString("action", nextAction.name)

                setLore(listOf(literalText {
                    text("Action: ") { }
                    text(nextAction.name) { }
                    color = Color.GREEN.argb()
                    italic = false
                }))

                playerEntity.sendMessage("Action: $nextAction".literal)
            }
        }
        return super.use(world, playerEntity, hand)
    }

    private fun use(
        playerEntity: PlayerEntity,
        blockState: BlockState,
        worldAccess: WorldAccess,
        blockPos: BlockPos,
        bl: Boolean,
        itemStack: ItemStack
    ): Boolean {
        if (!playerEntity.isCreative) return false

        playerEntity.sendMessage("Position: ${blockPos.toShortString()}".literal)

        val action = itemStack.action()
        action.callback.invoke(playerEntity, blockState, worldAccess, blockPos, bl, itemStack)

        return true
    }
}
