package dev.sterner.the_catamount.mixin;

import dev.sterner.the_catamount.client.ClientFogEffectTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {

    @Inject(
            method = "getSkyColor",
            at = @At("RETURN"),
            cancellable = true
    )
    private void the_catamount$modifySkyColor(Vec3 pos, float partialTick, CallbackInfoReturnable<Vec3> cir) {
        if (!ClientFogEffectTracker.hasFogEffect()) return;

        float darkness = ClientFogEffectTracker.getSkyDarkness(pos);

        if (darkness > 0.0f) {
            Vec3 originalColor = cir.getReturnValue();

            double r = originalColor.x * (1.0 - darkness * 0.9);
            double g = originalColor.y * (1.0 - darkness * 0.9);
            double b = originalColor.z * (1.0 - darkness * 0.8);

            r = Math.max(r, 0.08 * darkness);
            g = Math.max(g, 0.08 * darkness);
            b = Math.max(b, 0.12 * darkness);

            cir.setReturnValue(new Vec3(r, g, b));
        }
    }

    @Inject(
            method = "getCloudColor",
            at = @At("RETURN"),
            cancellable = true
    )
    private void the_catamount$modifyCloudColor(float partialTick, CallbackInfoReturnable<Vec3> cir) {
        if (!ClientFogEffectTracker.hasFogEffect()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        float darkness = ClientFogEffectTracker.getSkyDarkness(mc.player.position());

        if (darkness > 0.0f) {
            Vec3 originalColor = cir.getReturnValue();

            double r = originalColor.x * (1.0 - darkness * 0.85);
            double g = originalColor.y * (1.0 - darkness * 0.85);
            double b = originalColor.z * (1.0 - darkness * 0.75);

            cir.setReturnValue(new Vec3(r, g, b));
        }
    }
}