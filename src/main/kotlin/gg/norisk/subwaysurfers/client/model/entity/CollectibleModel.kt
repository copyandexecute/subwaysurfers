package gg.norisk.subwaysurfers.client.model.entity

import gg.norisk.subwaysurfers.common.entity.CollectibleEntity
import net.minecraft.client.render.RenderLayer
import net.minecraft.util.Identifier
import software.bernie.geckolib.model.DefaultedEntityGeoModel

class CollectibleModel(id: Identifier) : DefaultedEntityGeoModel<CollectibleEntity>(id) {
    // We want this entity to have a translucent render
    override fun getRenderType(animatable: CollectibleEntity, texture: Identifier): RenderLayer {
        return RenderLayer.getEntityTranslucent(getTextureResource(animatable))
    }
}
