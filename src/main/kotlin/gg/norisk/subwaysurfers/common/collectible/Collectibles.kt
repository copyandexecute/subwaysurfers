package gg.norisk.subwaysurfers.common.collectible

import gg.norisk.subwaysurfers.SubwaySurfers.toId
import gg.norisk.subwaysurfers.client.model.entity.CollectibleModel
import gg.norisk.subwaysurfers.common.entity.CollectibleEntity
import gg.norisk.subwaysurfers.item.PowerupItem
import gg.norisk.subwaysurfers.registry.EntityRegistry
import gg.norisk.subwaysurfers.registry.ItemRegistry
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.text.Text
import software.bernie.geckolib.renderer.GeoEntityRenderer

/**
 * Class containing client/common logic for registering items, entities and renderers for all collectibles/powerups.
 */
object Collectibles {
    fun register() {
        // Register entities, etc. for all collectibles/powerups
        val items = mutableListOf<Item>()

        collectibles.forEach { collectible ->
            // Entity
            collectible.entityType = EntityRegistry.registerMob(
                collectible.id,
                { type, world -> CollectibleEntity(collectible, type, world) },
                1f,
                1f
            )
            // Attributes
            FabricDefaultAttributeRegistry.register(collectible.entityType, EntityRegistry.createGenericEntityAttributes())

            // Item
            val item = if (collectible is Powerup && collectible.isArmor) {
                PowerupItem(collectible, Item.Settings())
            } else {
                Item(Item.Settings())
            }
            collectible.item = ItemRegistry.registerItem(collectible.id, item)
            items.add(collectible.item!!)
        }

        // Item group
        Registry.register(
            Registries.ITEM_GROUP, "subwaysurfers_collectibles".toId(), FabricItemGroup
                .builder()
                .displayName(Text.translatable("itemGroup.subwaysurfers.subwaysurfers_collectibles"))
                .icon { ItemStack(items.randomOrNull() ?: Items.AIR) }
                .entries { _: ItemGroup.DisplayContext?, entries: ItemGroup.Entries ->
                    items.map(::ItemStack).forEach(entries::add)
                }.build()
        )
    }

    fun registerClient() {
        collectibles.forEach { collectible ->
            // Renderer (only for client-side)
            val entityType = collectible.entityType ?: return@forEach
            EntityRendererRegistry.register(entityType) {
                GeoEntityRenderer(it, CollectibleModel(collectible.id.toId()))
            }
        }
    }
}

// add/remove items to/from this list to (un)register collectibles
val collectibles = listOf(Coin, Jetpack, Hoverboard, Magnet, Boots)

val powerups get() = collectibles.filterIsInstance<Powerup>()