package dev.sterner.the_catamount.client.render;

import dev.sterner.the_catamount.client.model.DevouredModel;
import dev.sterner.the_catamount.entity.DevouredEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class DevouredRenderer extends MobRenderer<DevouredEntity, DevouredModel> {
    private static final ResourceLocation VEX_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/illager/vex.png");
    private static final ResourceLocation VEX_CHARGING_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/illager/vex_charging.png");

    public DevouredRenderer(EntityRendererProvider.Context context) {
        super(context, new DevouredModel(context.bakeLayer(ModelLayers.VEX)), 0.3F);
        this.addLayer(new ItemInHandLayer(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(DevouredEntity entity) {
        return entity.isCharging() ? VEX_CHARGING_LOCATION : VEX_LOCATION;
    }
}