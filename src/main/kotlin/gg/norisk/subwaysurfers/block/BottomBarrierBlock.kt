package gg.norisk.subwaysurfers.block

import com.mojang.serialization.MapCodec
import gg.norisk.subwaysurfers.client.mechanics.ClientCollisionManager
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.HorizontalFacingBlock
import net.minecraft.entity.Entity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class BottomBarrierBlock(settings: Settings) : HorizontalFacingBlock(settings) {
    companion object {
        val CODEC: MapCodec<BottomBarrierBlock> = createCodec(::BottomBarrierBlock)
    }

    override fun getCodec(): MapCodec<out HorizontalFacingBlock> = CODEC

    override fun getPlacementState(itemPlacementContext: ItemPlacementContext): BlockState {
        return defaultState.with(FACING, itemPlacementContext.horizontalPlayerFacing.opposite) as BlockState
    }

    override fun appendProperties(builder: StateManager.Builder<Block?, BlockState?>) {
        builder.add(FACING)
    }

    override fun onEntityCollision(blockState: BlockState, world: World, blockPos: BlockPos, entity: Entity) {
        ClientCollisionManager.handleBlockCollision(blockState, world, blockPos, entity)
    }
}
