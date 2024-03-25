package gg.norisk.subwaysurfers.mixin.client.input;

import gg.norisk.subwaysurfers.client.ClientSettings;
import gg.norisk.subwaysurfers.client.lifecycle.ClientGamePreStartLifeCycle;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends Input {
    @Inject(method = "tick", at = @At(value = "TAIL"))
    private void subwaySurfersMoveInjection(boolean slowDown, float f, CallbackInfo ci) {
        if (ClientSettings.INSTANCE.isEnabled()) {
            movementForward = 1.0F;
            movementSideways = 0.0f;
            pressingLeft = false;
            pressingRight = false;
            jumping = false;
            sneaking = false;
        } else if (ClientGamePreStartLifeCycle.INSTANCE.isPreStarting()) {
            movementForward = 0.0F;
            movementSideways = 0.0f;
            pressingLeft = false;
            pressingRight = false;
            jumping = false;
            sneaking = false;
        }
    }
}
