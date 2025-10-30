package dev.sterner.the_catamount.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.sterner.the_catamount.TheCatamount;
import dev.sterner.the_catamount.client.model.WindEntityModel;
import dev.sterner.the_catamount.entity.WindEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class WindRenderLayer extends RenderLayer<WindEntity, WindEntityModel> {

    private static final ResourceLocation TEXTURE = TheCatamount.id("textures/entity/biting_wind.png");

    public WindRenderLayer(EntityRendererProvider.Context ctx, RenderLayerParent<WindEntity, WindEntityModel> parent) {
        super(parent);
    }

    @Override
    public void render(
            PoseStack poseStack, MultiBufferSource buffer, int light,
            WindEntity entity, float limbSwing, float limbSwingAmount,
            float partialTicks, float ageInTicks, float yaw, float pitch
    ) {
        WindEntityModel model = this.getParentModel();
        float t = entity.tickCount + partialTicks;

        VertexConsumer vc = buffer.getBuffer(
                RenderType.breezeWind(TEXTURE, (t * 0.02F) % 1F, 0F)
        );

        model.renderToBuffer(
                poseStack, vc, light, OverlayTexture.NO_OVERLAY,
                -1
        );
    }
}
