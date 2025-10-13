package dev.sterner.the_catamount.fabric;

import dev.sterner.the_catamount.client.render.CatamountEntityRenderer;
import dev.sterner.the_catamount.payload.SyncCatamountPlayerDataPayload;
import dev.sterner.the_catamount.registry.TCEntityTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class TheCatamountClientFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(TCEntityTypes.CATAMOUNT, CatamountEntityRenderer::new);

        ClientPlayNetworking.registerGlobalReceiver(SyncCatamountPlayerDataPayload.ID, (payload, ctx) -> {
            payload.handleS2C();
        });
    }
}
