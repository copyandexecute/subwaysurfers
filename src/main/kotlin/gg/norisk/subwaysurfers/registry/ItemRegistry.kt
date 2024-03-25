package gg.norisk.subwaysurfers.registry

import gg.norisk.subwaysurfers.SubwaySurfers.toId
import gg.norisk.subwaysurfers.item.BuilderItem
import gg.norisk.subwaysurfers.item.RemoteDetonatorItem
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.text.Text
import net.minecraft.util.Rarity

object ItemRegistry {
    val BUILDER: Item = registerItem("builder", BuilderItem(Item.Settings().maxCount(1).rarity(Rarity.EPIC)))
    val REMOTE_DETONATOR: Item = registerItem("remote_detonator", RemoteDetonatorItem(Item.Settings().maxCount(1).rarity(Rarity.EPIC)))
    val SUBWAY_RAIL: BlockItem = registerItem("subway_rail", BlockItem(BlockRegistry.SUBWAY_RAIL, Item.Settings()))
    val TOP_BARRIER: BlockItem = registerItem("top_barrier", BlockItem(BlockRegistry.TOP_BARRIER, Item.Settings()))
    val BOTTOM_BARRIER: BlockItem =
        registerItem("bottom_barrier", BlockItem(BlockRegistry.BOTTOM_BARRIER, Item.Settings()))


    fun init() {
    }

    val ITEM_GROUP: ItemGroup = Registry.register(
        Registries.ITEM_GROUP, "subwaysurfers_utils".toId(), FabricItemGroup
            .builder()
            .displayName(Text.translatable("itemGroup.subwaysurfers.subwaysurfers_utils"))
            .icon { ItemStack(SUBWAY_RAIL) }
            .entries { _: ItemGroup.DisplayContext?, entries: ItemGroup.Entries ->
                entries.add(SUBWAY_RAIL)
                entries.add(TOP_BARRIER)
                entries.add(BOTTOM_BARRIER)
                entries.add(BUILDER)
                entries.add(REMOTE_DETONATOR)
            }.build()
    )

    fun <I : Item> registerItem(name: String, item: I): I {
        return Registry.register(Registries.ITEM, name.toId(), item)
    }
}
