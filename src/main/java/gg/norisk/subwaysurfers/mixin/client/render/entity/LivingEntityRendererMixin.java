package gg.norisk.subwaysurfers.mixin.client.render.entity;

import gg.norisk.subwaysurfers.common.collectible.Jetpack;
import gg.norisk.subwaysurfers.subwaysurfers.SubwaySurferKt;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    @Unique private float subway$currentRotation = 0.0f;

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V", shift = At.Shift.AFTER, ordinal = 0))
    public void injectedRender(LivingEntity livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (livingEntity instanceof ClientPlayerEntity player && SubwaySurferKt.isSubwaySurfers(player)) {
            float targetRotation;
            if (Jetpack.INSTANCE.isActiveFor(player)) targetRotation = 80.0f;
            else targetRotation = 0.0f;

            // todo fps-independent lerp
            subway$currentRotation = MathHelper.lerp(0.05f, this.subway$currentRotation, targetRotation);
            var quaternion = new Quaternionf();
            matrixStack.multiply(quaternion.fromAxisAngleDeg(1, 0, 0, subway$currentRotation));
        }
    }
}
