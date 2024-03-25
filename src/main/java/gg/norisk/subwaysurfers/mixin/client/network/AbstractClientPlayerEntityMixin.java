package gg.norisk.subwaysurfers.mixin.client.network;

import com.mojang.authlib.GameProfile;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import gg.norisk.subwaysurfers.client.player.IAnimatedPlayer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin implements IAnimatedPlayer {
    //Unique annotation will rename private methods/fields if needed to avoid collisions.
    @Unique
    private final ModifierLayer<IAnimation> subwaysurferAnimationContainer = new ModifierLayer<>();

    /*@Inject(method = "getSkinTexture", at = @At("RETURN"), cancellable = true)
    private void noriskSkin(CallbackInfoReturnable<Identifier> cir) {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            cir.setReturnValue(SubwaySurfers.INSTANCE.getNoriskSkin());
        }
    }*/


    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void init(ClientWorld world, GameProfile profile, CallbackInfo ci) {
        //Mixin does not know (yet) that this will be merged with AbstractClientPlayerEntity
        PlayerAnimationAccess.getPlayerAnimLayer((AbstractClientPlayerEntity) (Object) this).addAnimLayer(3000, subwaysurferAnimationContainer); //Register the layer with a priority
    }

    @NotNull
    @Override
    public ModifierLayer<IAnimation> subwaysurfers_getModAnimation() {
        return subwaysurferAnimationContainer;
    }
}
