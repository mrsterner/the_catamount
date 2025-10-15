package dev.sterner.the_catamount.mixin;

import dev.sterner.the_catamount.client.ClientFogEffectTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DimensionSpecialEffects.OverworldEffects.class)
public class OverworldSpecialEffectsMixin {

    @Inject(
            method = "getBrightnessDependentFogColor",
            at = @At("RETURN"),
            cancellable = true
    )
    private void the_catamount$modifyFogColor(Vec3 fogColor, float brightness, CallbackInfoReturnable<Vec3> cir) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !ClientFogEffectTracker.hasFogEffect()) return;

        float darkness = ClientFogEffectTracker.getSkyDarkness(mc.player.position());

        if (darkness > 0.0f) {
            Vec3 originalColor = cir.getReturnValue();

            double r = originalColor.x * (1.0 - darkness) + 0.08 * darkness;
            double g = originalColor.y * (1.0 - darkness) + 0.08 * darkness;
            double b = originalColor.z * (1.0 - darkness) + 0.12 * darkness;

            cir.setReturnValue(new Vec3(r, g, b));
        }
    }
}