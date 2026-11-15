package de.micromod.mixin;

import de.micromod.Features;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {

    @Inject(method = "tickMovement", at = @At("HEAD"), cancellable = true)
    private void micromod$tickMovement(CallbackInfo ci) {
        ClientPlayerEntity self = (ClientPlayerEntity) (Object) this;
        if (Features.freecam) {
            // Spieler bleibt stehen, nur die Kamera fliegt
            ci.cancel();
            return;
        }
        if (Features.noclip) {
            self.noClip = true;
            self.getAbilities().allowFlying = true;
            self.getAbilities().flying = true;
        }
    }

    // verhindert, dass Minecraft dich aus Bloecken herausschiebt
    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true, require = 0)
    private void micromod$pushOut(double x, double z, CallbackInfo ci) {
        if (Features.noclip) ci.cancel();
    }
}
