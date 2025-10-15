package dev.sterner.the_catamount.mixin;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.sterner.the_catamount.client.ClientFogEffectTracker;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public class FogRendererMixin {

    @Inject(
            method = "setupFog",
            at = @At("RETURN")
    )
    private static void the_catamount$applyCustomFog(Camera camera, FogRenderer.FogMode fogMode,
                                                     float renderDistance, boolean isFoggy, float partialTick,
                                                     CallbackInfo ci) {
        if (!ClientFogEffectTracker.hasFogEffect()) return;

        Vec3 playerPos = camera.getPosition();
        float transition = ClientFogEffectTracker.getFogTransition();

        if (transition > 0.0f) {
            RenderSystem.setShaderFogColor(0.08f, 0.08f, 0.12f);

            float density = ClientFogEffectTracker.getFogDensity(playerPos);
            if (density > 0.0f) {
                float maxFogEnd = 150.0f;
                float minFogEnd = 35.0f * (1.0f - density * 0.5f);
                float fogEnd = maxFogEnd * (1.0f - transition) + minFogEnd * transition;

                float fogStart = fogEnd * 0.1f;

                RenderSystem.setShaderFogStart(fogStart);
                RenderSystem.setShaderFogEnd(fogEnd);
                RenderSystem.setShaderFogShape(FogShape.SPHERE);
            }
        }
    }
}