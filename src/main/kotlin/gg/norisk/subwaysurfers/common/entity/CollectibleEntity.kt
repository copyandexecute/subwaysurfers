package gg.norisk.subwaysurfers.common.entity

import gg.norisk.subwaysurfers.common.collectible.Collectible
import gg.norisk.subwaysurfers.entity.DriveableEntity
import gg.norisk.subwaysurfers.entity.OriginMarker
import gg.norisk.subwaysurfers.network.dto.toDto
import gg.norisk.subwaysurfers.subwaysurfers.isSubwaySurfers
import net.minecraft.block.BlockState
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.passive.AnimalEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import software.bernie.geckolib.animatable.GeoEntity
import software.bernie.geckolib.constant.DefaultAnimations
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar
import software.bernie.geckolib.util.GeckoLibUtil

class CollectibleEntity(
    private val collectible: Collectible,
    type: EntityType<out AnimalEntity>,
    level: World
) : DriveableEntity(type, level), GeoEntity, OriginMarker {
    override var origin: BlockPos = this.blockPos
    private val cache: AnimatableInstanceCache = GeckoLibUtil.createInstanceCache(this)

    override fun pushAwayFrom(entity: Entity?) {
        super.pushAwayFrom(entity)
    }
    override fun pushAway(entity: Entity?) {
        super.pushAway(entity)
    }
    override fun playStepSound(pos: BlockPos, block: BlockState) {}

    override fun onPlayerCollision(player: PlayerEntity) {
        if (world.isClient && player.isSubwaySurfers && player is ClientPlayerEntity) {
            collectible.pickupPacket.send(this.blockPos.toDto())
            this.discard()
        }
    }

    // ---- ANIMATIONS ----

    // Add our generic idle animation controller
    override fun registerControllers(controllers: ControllerRegistrar) {
        controllers.add(DefaultAnimations.genericIdleController(this))
    }

    override fun getAnimatableInstanceCache(): AnimatableInstanceCache = this.cache
}

