package dev.sterner.the_catamount.client.render;

import dev.sterner.the_catamount.client.model.CatamountEntityModel;
import dev.sterner.the_catamount.entity.CatamountEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EntityType;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CatamountEntityRenderer extends GeoEntityRenderer<CatamountEntity> {

    public CatamountEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new CatamountEntityModel());
    }
}
