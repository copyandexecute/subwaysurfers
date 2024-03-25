package gg.norisk.subwaysurfers.mixin.entity.player;

import gg.norisk.subwaysurfers.common.collectible.CollectiblesKt;
import gg.norisk.subwaysurfers.common.collectible.Powerup;
import gg.norisk.subwaysurfers.event.events.PlayerEvents;
import gg.norisk.subwaysurfers.subwaysurfers.SubwaySurferKt;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    @Shadow
    public abstract boolean damage(DamageSource source, float amount);

    @Shadow
    public abstract float getMovementSpeed();

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void onBlockCollision(BlockState blockState) {
        super.onBlockCollision(blockState);
        PlayerEvents.INSTANCE.getBlockCollisionEvent().invoke(new PlayerEvents.PlayerBlockCollisionEvent((PlayerEntity) (Object) this, blockState));
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tickInjection(CallbackInfo ci) {
        if (!this.getWorld().isClient) {
            SubwaySurferKt.handlePunishTicks((PlayerEntity) (Object) this);
        } else {
            if (horizontalCollision) {
                //PlayerEvents.INSTANCE.getHorionztalCollisionEvent().invoke(new PlayerEvents.PlayerHorionztalCollisionEvent((PlayerEntity) (Object) this));
            }
        }
    }


    @Inject(method = "slowMovement", at = @At("HEAD"), cancellable = true)
    private void injected(CallbackInfo ci) {
        if (SubwaySurferKt.isSubwaySurfers((PlayerEntity) (Object) this)) {
            ci.cancel();
        }
    }

    @Inject(method = "getOffGroundSpeed", at = @At("HEAD"), cancellable = true)
    private void getOffGroundSpeedInjection(CallbackInfoReturnable<Float> cir) {
        if (SubwaySurferKt.isSubwaySurfers((PlayerEntity) (Object) this)) {
            cir.setReturnValue(this.getMovementSpeed());
        }
    }

    @ModifyArgs(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Box;expand(DDD)Lnet/minecraft/util/math/Box;", ordinal = 1))
    private void tickMovementInjection(Args args) {
        if (SubwaySurferKt.isSubwaySurfers((PlayerEntity) (Object) this)) {
            args.setAll(0.3, 0.0, 0.3);
        }
    }

    //TODO das anders coden lol
    @Override
    public boolean isOnGround() {
        if (SubwaySurferKt.isSubwaySurfers((PlayerEntity) (Object) this)) {
            return true;
        } else {
            return super.isOnGround();
        }
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void initDataTrackerInjection(CallbackInfo ci) {
        this.dataTracker.startTracking(SubwaySurferKt.getSlidingTracker(), false);
        this.dataTracker.startTracking(SubwaySurferKt.getGravityTracker(), 0.3f);
        this.dataTracker.startTracking(SubwaySurferKt.getDashStrengthTracker(), 2.0f);
        this.dataTracker.startTracking(SubwaySurferKt.getMultiplierTracker(), 1);
        this.dataTracker.startTracking(SubwaySurferKt.getRailDataTracker(), 1);
        this.dataTracker.startTracking(SubwaySurferKt.getLastPatternUpdatePosTracker(), 0);
        this.dataTracker.startTracking(SubwaySurferKt.getLastBlockCollisionPosTracker(), 0);
        this.dataTracker.startTracking(SubwaySurferKt.getLastHorizontalCollisionPosTracker(), 0);
        this.dataTracker.startTracking(SubwaySurferKt.getPunishTicksTracker(), 0);
        this.dataTracker.startTracking(SubwaySurferKt.getCoinDataTracker(), 0);
        this.dataTracker.startTracking(SubwaySurferKt.getSubwaySurfersTracker(), false);
        this.dataTracker.startTracking(SubwaySurferKt.getDebugModeTracker(), false);

        // data trackers for power-ups
        CollectiblesKt.getPowerups().forEach(powerUp -> this.dataTracker.startTracking(powerUp.getEndTimestampTracker(), 0L));
    }

    @Inject(method = "getEquippedStack", at = @At("HEAD"), cancellable = true)
    private void fakePowerupStacks(EquipmentSlot equipmentSlot, CallbackInfoReturnable<ItemStack> cir) {
        if (!getWorld().isClient) return;
        for (Powerup powerup : CollectiblesKt.getPowerups()) {
            if (powerup.isActiveFor((PlayerEntity) (Object) this) && powerup.getEquipmentSlot() == equipmentSlot && powerup.getItem() != null) {
                cir.setReturnValue(new ItemStack(powerup.getItem()));
            }
        }
    }
}
