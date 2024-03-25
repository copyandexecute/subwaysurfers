package gg.norisk.subwaysurfers.entity

import gg.norisk.subwaysurfers.client.hud.NbtEditorScreen
import gg.norisk.subwaysurfers.common.collectible.Coin
import gg.norisk.subwaysurfers.entity.TrainEntity.Companion.handleDiscard
import gg.norisk.subwaysurfers.subwaysurfers.isSubwaySurfers
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.EntityType
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.passive.AnimalEntity
import net.minecraft.entity.passive.PassiveEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.silkmc.silk.core.entity.modifyVelocity
import java.util.*

open class DriveableEntity(type: EntityType<out AnimalEntity>, level: World) : AnimalEntity(type, level), UUIDMarker {
    var moveSpeed: Float
        get() {
            return this.dataTracker.get(MOVE_SPEED)
        }
        set(value) {
            this.dataTracker.set(MOVE_SPEED, value)
        }

    override var owner: UUID? = null


    var startPos: Vec3d = pos
    var isVisualTestDrive: Boolean = false
        set(value) {
            if (value) {
                startPos = pos
            } else {
                this.setPosition(startPos)
            }
            field = value
        }

    var shouldDrive: Boolean
        get() {
            return this.dataTracker.get(DRIVE)
        }
        set(value) {
            this.dataTracker.set(DRIVE, value)
        }

    companion object {
        private val DRIVE: TrackedData<Boolean> =
            DataTracker.registerData(DriveableEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)
        private val MOVE_SPEED: TrackedData<Float> =
            DataTracker.registerData(DriveableEntity::class.java, TrackedDataHandlerRegistry.FLOAT)
    }

    init {
        this.ignoreCameraFrustum = true
        this.setNoGravity(true)
    }

    override fun initDataTracker() {
        super.initDataTracker()
        this.dataTracker.startTracking(DRIVE, false)
        this.dataTracker.startTracking(MOVE_SPEED, 0.3f)
    }

    override fun interactMob(playerEntity: PlayerEntity, hand: Hand): ActionResult {
        if (playerEntity.isCreativeLevelTwoOp && world.isClient) {
            MinecraftClient.getInstance().setScreen(NbtEditorScreen(this))
        }
        return super.interactMob(playerEntity, hand)
    }

    override fun travel(moveVector: Vec3d) {
        if (type == Coin.entityType) {
            // todo eigentlich soll nur PowerupEntity eine DriveableEntity sein?
            return super.travel(moveVector)
        }
        val shouldDrive = if (shouldDrive) {
            if (owner != null) {
                val ownerEntity = world.getPlayerByUuid(owner)
                ownerEntity?.isSubwaySurfers == true && ownerEntity.blockPos.z + 50 >= this.blockPos.z
            } else {
                false
            }
        } else {
            false
        }

        if (this.isAlive && (shouldDrive || isVisualTestDrive) && world.isClient) {
            if (this.shouldDrive) {
                modifyVelocity(Vec3d(0.0, moveVector.y, -moveSpeed.toDouble()))

                if (isVisualTestDrive) {
                    if (startPos.distanceTo(this.pos) >= 25) {
                        isVisualTestDrive = false
                    }
                }
            }
            super.travel(moveVector)
        }
    }

    override fun tick() {
        super.tick()
        handleDiscard(owner)
    }

    override fun writeCustomDataToNbt(nbtCompound: NbtCompound) {
        super.writeCustomDataToNbt(nbtCompound)
        nbtCompound.putBoolean("shouldDrive", shouldDrive)
        nbtCompound.putFloat("moveSpeed", moveSpeed)
    }

    override fun readCustomDataFromNbt(nbtCompound: NbtCompound) {
        super.readCustomDataFromNbt(nbtCompound)
        shouldDrive = nbtCompound.getBoolean("shouldDrive")
        moveSpeed = nbtCompound.getFloat("moveSpeed")
    }

    override fun isLogicalSideForUpdatingMovement(): Boolean = true
    override fun createChild(serverWorld: ServerWorld?, passiveEntity: PassiveEntity?): PassiveEntity? = null
}
