package gg.norisk.subwaysurfers.mixin.entity;

import gg.norisk.subwaysurfers.common.collectible.Jetpack;
import gg.norisk.subwaysurfers.subwaysurfers.SubwaySurferKt;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    private EntityDimensions dimensions;
  
    @Shadow public abstract void setPos(double d, double e, double f);

    @Shadow public abstract void setBoundingBox(Box box);

    @Shadow protected abstract Box calculateBoundingBox();

    @Shadow public abstract double getX();

    @Shadow public abstract double getY();

    @Shadow public abstract double getZ();

    @Shadow public abstract void setPosition(double d, double e, double f);

    @Inject(method = "setPosition(DDD)V", at = @At("HEAD"), cancellable = true)
    public void setPosition(double x, double y, double z, CallbackInfo ci) {
        if ((Object) this instanceof ClientPlayerEntity player) {
            if (SubwaySurferKt.isSubwaySurfers(player) && Jetpack.INSTANCE.isActiveFor(player)) {
                ci.cancel();
                setPos(x, Jetpack.INSTANCE.getY(player, y), z);
                setBoundingBox(calculateBoundingBox());
            }
        }
    }

    @Redirect(method = "calculateDimensions", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;dimensions:Lnet/minecraft/entity/EntityDimensions;", opcode = Opcodes.PUTFIELD))
    private void injected(Entity instance, EntityDimensions value) {
        if (instance instanceof PlayerEntity player && SubwaySurferKt.isSliding(player)) {
            dimensions = EntityDimensions.fixed(0.2f, 0.2f);
        } else {
            dimensions = value;
        }
    }

    @Inject(method = "move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V", at = @At("HEAD"), cancellable = true)
    private void moveInjection(MovementType movementType, Vec3d vec3d, CallbackInfo ci) {
        if ((Object) this instanceof ClientPlayerEntity player) {
            if (SubwaySurferKt.isSubwaySurfers(player) && Jetpack.INSTANCE.isActiveFor(player)) {
                ci.cancel();
                setPosition(getX() + vec3d.x, getY() + vec3d.y, getZ() + vec3d.z);
            }
        }
    }

    @Inject(method = "hasNoGravity", at = @At("HEAD"), cancellable = true)
    private void hasNoGravityInjection(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof ClientPlayerEntity player) {
            if (SubwaySurferKt.isSubwaySurfers(player) && Jetpack.INSTANCE.isActiveFor(player)) {
                cir.setReturnValue(true);
            }
        }
    }
}
