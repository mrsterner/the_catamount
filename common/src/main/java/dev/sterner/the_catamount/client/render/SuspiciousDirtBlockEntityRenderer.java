package dev.sterner.the_catamount.client.render;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.sterner.the_catamount.block.SuspiciousDirtBlockEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class SuspiciousDirtBlockEntityRenderer implements BlockEntityRenderer<SuspiciousDirtBlockEntity> {

    private final ItemRenderer itemRenderer;

    public SuspiciousDirtBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(
            SuspiciousDirtBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        if (blockEntity.getLevel() != null) {
            int dusted = blockEntity.getBlockState().getValue(BlockStateProperties.DUSTED);
            if (dusted > 0) {
                Direction direction = blockEntity.getHitDirection();
                ItemStack itemStack = blockEntity.getStoredItem();
                if (direction != null && !itemStack.isEmpty()) {
                    poseStack.pushPose();
                    poseStack.translate(0.0f, 0.5f, 0.0f);

                    float[] translations = this.translations(direction, dusted);
                    poseStack.translate(translations[0], translations[1], translations[2]);

                    poseStack.mulPose(Axis.YP.rotationDegrees(75.0f));

                    boolean bl = direction == Direction.EAST || direction == Direction.WEST;
                    poseStack.mulPose(Axis.YP.rotationDegrees((bl ? 90 : 0) + 11));

                    poseStack.scale(0.5f, 0.5f, 0.5f);

                    int light = LevelRenderer.getLightColor(
                            blockEntity.getLevel(),
                            blockEntity.getBlockState(),
                            blockEntity.getBlockPos().relative(direction)
                    );

                    itemRenderer.renderStatic(
                            itemStack,
                            ItemDisplayContext.FIXED,
                            light,
                            OverlayTexture.NO_OVERLAY,
                            poseStack,
                            bufferSource,
                            blockEntity.getLevel(),
                            0
                    );

                    poseStack.popPose();
                }
            }
        }
    }

    private float[] translations(Direction direction, int dustedLevel) {
        float[] fs = new float[]{0.5f, 0.0f, 0.5f};
        float f = dustedLevel / 10.0f * 0.75f;

        switch (direction) {
            case EAST -> fs[0] = 0.73f + f;
            case WEST -> fs[0] = 0.25f - f;
            case UP -> fs[1] = 0.25f + f;
            case DOWN -> fs[1] = -0.23f - f;
            case NORTH -> fs[2] = 0.25f - f;
            case SOUTH -> fs[2] = 0.73f + f;
        }

        return fs;
    }
}