package dev.sterner.the_catamount.fabric;

import dev.sterner.the_catamount.client.render.CatamountEntityRenderer;
import dev.sterner.the_catamount.client.render.SuspiciousDirtBlockEntityRenderer;
import dev.sterner.the_catamount.payload.SyncCatamountPlayerDataPayload;
import dev.sterner.the_catamount.registry.TCBlockEntityTypes;
import dev.sterner.the_catamount.registry.TCBlocks;
import dev.sterner.the_catamount.registry.TCEntityTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class TheCatamountClientFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(TCEntityTypes.CATAMOUNT, CatamountEntityRenderer::new);

        BlockEntityRenderers.register(TCBlockEntityTypes.SUSPICIOUS_DIRT, SuspiciousDirtBlockEntityRenderer::new);

        BlockRenderLayerMap.INSTANCE.putBlock(TCBlocks.MONSTROUS_REMAINS, RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(TCBlocks.MONSTROUS_SKULL, RenderType.cutout());

        ClientPlayNetworking.registerGlobalReceiver(SyncCatamountPlayerDataPayload.ID, (payload, ctx) -> {
            payload.handleS2C();
        });
    }
}
