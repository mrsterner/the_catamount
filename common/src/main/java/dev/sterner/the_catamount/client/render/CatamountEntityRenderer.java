package dev.sterner.the_catamount.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.sterner.the_catamount.TheCatamount;
import dev.sterner.the_catamount.client.model.CatamountEntityModel;
import dev.sterner.the_catamount.entity.CatamountEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class CatamountEntityRenderer extends GeoEntityRenderer<CatamountEntity> {

    public CatamountEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new CatamountEntityModel());

        this.addRenderLayer(new CatamountEyesLayer(this));
    }

    @Override
    public RenderType getRenderType(CatamountEntity animatable, ResourceLocation texture,
                                    @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }


    public static class CatamountEyesLayer extends GeoRenderLayer<CatamountEntity> {

        public CatamountEyesLayer(GeoEntityRenderer<CatamountEntity> renderer) {
            super(renderer);
        }

        protected ResourceLocation getTextureResource(CatamountEntity entity) {
            return switch (entity.getStage()) {
                case 3 -> TheCatamount.id("textures/entity/catamount_stage_1_glowmask.png");
                case 4 -> TheCatamount.id("textures/entity/catamount_stage_2_glowmask.png");
                case 5 -> TheCatamount.id("textures/entity/catamount_stage_3_glowmask.png");
                default -> TheCatamount.id("textures/entity/catamount_stage_1_glowmask.png");
            };
        }

        @Override
        public void render(PoseStack poseStack, CatamountEntity animatable, BakedGeoModel bakedModel,
                           @Nullable RenderType renderType, MultiBufferSource bufferSource,
                           @Nullable VertexConsumer buffer, float partialTick,
                           int packedLight, int packedOverlay) {

            RenderType eyesRenderType = RenderType.eyes(getTextureResource(animatable));

            VertexConsumer eyesBuffer = bufferSource.getBuffer(eyesRenderType);

            this.getRenderer().reRender(
                    bakedModel,
                    poseStack,
                    bufferSource,
                    animatable,
                    eyesRenderType,
                    eyesBuffer,
                    partialTick,
                    15728880,
                    OverlayTexture.NO_OVERLAY,
                    -1
            );
        }
    }
}