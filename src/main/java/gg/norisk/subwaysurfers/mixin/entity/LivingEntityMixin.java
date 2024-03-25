package gg.norisk.subwaysurfers.mixin.entity;

import gg.norisk.subwaysurfers.subwaysurfers.SubwaySurferKt;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow
    public abstract float getMovementSpeed();

    public LivingEntityMixin(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @ModifyConstant(method = "travel", constant = @Constant(doubleValue = 0.08))
    private double travelInjection(double constant) {
        if (getType() == EntityType.PLAYER) {
            //noinspection DataFlowIssue
            var player = (PlayerEntity) (Object) this;
            if (SubwaySurferKt.isSubwaySurfers(player)) {
                return SubwaySurferKt.getGravity(player);
            }
        }
        return constant;
    }

    @Inject(method = "onTrackedDataSet", at = @At("TAIL"))
    private void injected(TrackedData<?> data, CallbackInfo ci) {
        if (getType() == EntityType.PLAYER) {
            var player = (PlayerEntity) (Object) this;
            if (SubwaySurferKt.getSlidingTracker().equals(data)) {
                calculateDimensions();
            }
        }
    }

    @Inject(method = "getMovementSpeed(F)F", at = @At("RETURN"), cancellable = true)
    private void injected(float f, CallbackInfoReturnable<Float> cir) {
        if (getType() == EntityType.PLAYER) {
            var player = (PlayerEntity) (Object) this;
            if (SubwaySurferKt.isSubwaySurfers(player)) {
                //cir.setReturnValue(this.getMovementSpeed());
            }
        }
    }
}
