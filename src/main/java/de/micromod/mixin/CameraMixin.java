package de.micromod.mixin;

import de.micromod.Features;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    protected abstract void setPos(double x, double y, double z);

    @Inject(method = "update", at = @At("TAIL"))
    private void micromod$update(CallbackInfo ci) {
        if (Features.freecam) {
            setPos(Features.freecamPos.x, Features.freecamPos.y, Features.freecamPos.z);
        }
    }
}
