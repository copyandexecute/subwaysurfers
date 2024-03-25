package gg.norisk.subwaysurfers.item

import gg.norisk.subwaysurfers.SubwaySurfers.toId
import gg.norisk.subwaysurfers.common.collectible.Powerup
import net.minecraft.client.render.entity.model.BipedEntityModel
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ArmorItem
import net.minecraft.item.ArmorMaterials
import net.minecraft.item.ItemStack
import software.bernie.geckolib.animatable.GeoItem
import software.bernie.geckolib.animatable.client.RenderProvider
import software.bernie.geckolib.constant.DefaultAnimations
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache
import software.bernie.geckolib.core.animation.AnimatableManager
import software.bernie.geckolib.model.DefaultedItemGeoModel
import software.bernie.geckolib.renderer.GeoArmorRenderer
import software.bernie.geckolib.util.GeckoLibUtil
import java.util.function.Consumer
import java.util.function.Supplier

class PowerupItem(val powerup: Powerup, settings: Settings) : ArmorItem(
    ArmorMaterials.DIAMOND,
    Type.values().first { it.equipmentSlot == powerup.equipmentSlot },
    settings,
), GeoItem {
    private val cache: AnimatableInstanceCache = GeckoLibUtil.createInstanceCache(this)
    private val renderProvider: Supplier<Any> = GeoItem.makeRenderer(this)


    override fun createRenderer(consumer: Consumer<Any>) {
        consumer.accept(object : RenderProvider {
            private var renderer: GeoArmorRenderer<*>? = null

            override fun getHumanoidArmorModel(
                livingEntity: LivingEntity,
                itemStack: ItemStack,
                equipmentSlot: EquipmentSlot,
                original: BipedEntityModel<LivingEntity>
            ): BipedEntityModel<LivingEntity> {
                if (this.renderer == null) this.renderer = GeoArmorRenderer<PowerupItem>(DefaultedItemGeoModel("armor/${powerup.id}".toId()))

                // This prepares our GeoArmorRenderer for the current render frame.
                // These parameters may be null however, so we don't do anything further with them
                renderer!!.prepForRender(livingEntity, itemStack, equipmentSlot, original)

                return renderer!!
            }
        })
    }

    override fun registerControllers(controllers: AnimatableManager.ControllerRegistrar) {
        controllers.add(DefaultAnimations.genericIdleController(this))
    }

    override fun getAnimatableInstanceCache(): AnimatableInstanceCache = cache
    override fun getRenderProvider(): Supplier<Any> = renderProvider
}
