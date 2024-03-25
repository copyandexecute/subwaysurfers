@file:Suppress("HasPlatformType")

package gg.norisk.subwaysurfers.subwaysurfers

import net.minecraft.client.MinecraftClient
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.MathHelper


val isEnabled: Boolean
    get() {
        return MinecraftClient.getInstance().player?.isSubwaySurfers == true
    }

//TODO leftPattern, rightPattern, trackPattern

var PlayerEntity.isSliding: Boolean
    get() {
        return this.dataTracker.get(slidingTracker)
    }
    set(value) {
        this.dataTracker.set(slidingTracker, value)
    }


var PlayerEntity.punishTicks: Int
    get() {
        return this.dataTracker.get(punishTicksTracker)
    }
    set(value) {
        this.dataTracker.set(punishTicksTracker, value)
    }

var PlayerEntity.gravity: Double
    get() {
        return this.dataTracker.get(gravityTracker).toDouble()
    }
    set(value) {
        this.dataTracker.set(gravityTracker, value.toFloat())
    }

var PlayerEntity.dashStrength: Double
    get() {
        return this.dataTracker.get(dashStrengthTracker).toDouble()
    }
    set(value) {
        this.dataTracker.set(dashStrengthTracker, value.toFloat())
    }

var PlayerEntity.multiplier: Int
    get() {
        return this.dataTracker.get(multiplierTracker)
    }
    set(value) {
        this.dataTracker.set(multiplierTracker, value)
    }

var PlayerEntity.rail: Int
    get() {
        return this.dataTracker.get(railDataTracker)
    }
    set(value) {
        this.dataTracker.set(railDataTracker, MathHelper.clamp(value, 0, 2))
    }

var PlayerEntity.lastPatternUpdatePos: Int
    get() {
        return this.dataTracker.get(lastPatternUpdatePosTracker)
    }
    set(value) {
        this.dataTracker.set(lastPatternUpdatePosTracker, value)
    }

var PlayerEntity.lastHorizontalCollisionPos: Int
    get() {
        return this.dataTracker.get(lastHorizontalCollisionPosTracker)
    }
    set(value) {
        this.dataTracker.set(lastHorizontalCollisionPosTracker, value)
    }

var PlayerEntity.lastBlockCollisionPos: Int
    get() {
        return this.dataTracker.get(lastBlockCollisionPosTracker)
    }
    set(value) {
        this.dataTracker.set(lastBlockCollisionPosTracker, value)
    }


val PlayerEntity.isSubwaySurfersOrSpectator: Boolean
    get() {
        return this.dataTracker.get(subwaySurfersTracker) or false
    }

var PlayerEntity.debugMode: Boolean
    get() {
        return this.dataTracker.get(debugModeTracker)
    }
    set(value) {
        this.dataTracker.set(debugModeTracker, value)
    }

var PlayerEntity.isSubwaySurfers: Boolean
    get() {
        return this.dataTracker.get(subwaySurfersTracker)
    }
    set(value) {
        this.dataTracker.set(subwaySurfersTracker, value)
    }

var PlayerEntity.coins: Int
    get() {
        return this.dataTracker.get(coinDataTracker)
    }
    set(value) {
        this.dataTracker.set(coinDataTracker, value)
    }

fun PlayerEntity.handlePunishTicks() {
    if (punishTicks > 0) {
        --punishTicks
    }
}

val coinDataTracker =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.INTEGER)
val dashStrengthTracker =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.FLOAT)
val gravityTracker =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.FLOAT)
val railDataTracker =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.INTEGER)
val lastPatternUpdatePosTracker =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.INTEGER)
val lastHorizontalCollisionPosTracker =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.INTEGER)
val lastBlockCollisionPosTracker =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.INTEGER)
val multiplierTracker =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.INTEGER)
val punishTicksTracker =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.INTEGER)
val slidingTracker =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)
val subwaySurfersTracker =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)
val debugModeTracker =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)