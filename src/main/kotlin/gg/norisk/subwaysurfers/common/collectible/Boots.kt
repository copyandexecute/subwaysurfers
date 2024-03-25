package gg.norisk.subwaysurfers.common.collectible

import net.minecraft.entity.EquipmentSlot
import kotlin.time.Duration.Companion.seconds

object Boots : Powerup("boots", 8.seconds, EquipmentSlot.LEGS, isArmor = false /* todo remove this when we have an armor model */) {
    // TODO functionality
}