package dev.sterner.the_catamount.client.render;

import dev.sterner.the_catamount.TheCatamount;
import dev.sterner.the_catamount.client.WindRenderLayer;
import dev.sterner.the_catamount.client.model.WindEntityModel;
import dev.sterner.the_catamount.entity.WindEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;


@Environment(value= EnvType.CLIENT)
public class WindEntityRenderer extends MobRenderer<WindEntity, WindEntityModel> {

    private static final ResourceLocation TEXTURE = TheCatamount.id("textures/entity/biting_wind.png");

    public WindEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new WindEntityModel(ctx.bakeLayer(WindEntityModel.LAYER)), 0F);

        this.addLayer(new WindRenderLayer(ctx, this));
    }

    @Override
    public ResourceLocation getTextureLocation(WindEntity entity) {
        return TEXTURE;
    }
}
