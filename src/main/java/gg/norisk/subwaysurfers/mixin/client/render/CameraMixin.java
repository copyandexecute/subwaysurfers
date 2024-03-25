package gg.norisk.subwaysurfers.mixin.client.render;

import gg.norisk.subwaysurfers.client.ClientSettings;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static net.minecraft.util.math.MathHelper.lerp;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Unique
    private double customX;

    @Unique
    private double lastCustomY;

    @Unique
    private double customY;

    @ModifyConstant(method = "update", constant = @Constant(doubleValue = 4.0))
    private double subwaySurfersIncreaseCameraDistance(double constant) {
        return (ClientSettings.INSTANCE.useSubwayCamera()) ? ClientSettings.INSTANCE.getCameraSettings().getDesiredCameraDistance() : constant;
    }

    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V"))
    private void setPosInjection(Args args, BlockView blockView, Entity entity, boolean bl, boolean bl2, float f) {
        var settings = ClientSettings.INSTANCE;
        if (ClientSettings.INSTANCE.useSubwayCamera() && settings.getStartPos() != null && entity instanceof PlayerEntity player) {
            var visualSettings = settings.getCameraSettings();
            // - interpolate x and y coordinates  todo fix interpolation relative to fps
            // - offset camera up and forward
            customX = lerp(f * visualSettings.getCameraSpeedX(), customX, player.getX());
            customY = lerp(f * visualSettings.getCameraSpeedY(), lastCustomY, entity.getY() + visualSettings.getCameraOffsetY());
            double customZ = lerp(f, entity.prevZ + visualSettings.getCameraOffsetZ(), entity.getZ() + visualSettings.getCameraOffsetZ());

            args.setAll(customX, customY, customZ);
        } else {
            customX = args.get(0);
            customY = args.get(1);
        }
    }

    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V", ordinal = 0))
    private void subwaySurfersStaticPerspective(Args args) {
        if (ClientSettings.INSTANCE.useSubwayCamera()) {
            args.set(0, ClientSettings.INSTANCE.getCameraSettings().getYaw());
            args.set(1, ClientSettings.INSTANCE.getCameraSettings().getPitch());
        }
    }

    @Inject(method = "updateEyeHeight", at = @At("HEAD"))
    private void updateCustomY(CallbackInfo ci) {
        if (ClientSettings.INSTANCE.useSubwayCamera() && ClientSettings.INSTANCE.getStartPos() != null) {
            lastCustomY = customY;
        }
    }
}
