package dev.sterner.the_catamount.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.sterner.the_catamount.entity.WindEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;


@Environment(value= EnvType.CLIENT)
public class WindEntityRenderer extends EntityRenderer<WindEntity> {

    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/breeze/breeze.png");

    public WindEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(WindEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(WindEntity entity) {
        return TEXTURE_LOCATION;
    }
}