package dev.sterner.the_catamount.client.render;

import dev.sterner.the_catamount.entity.LightOrbEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class LightOrbEntityRenderer extends EntityRenderer<LightOrbEntity> {
    public LightOrbEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(LightOrbEntity entity) {
        return null;
    }
}