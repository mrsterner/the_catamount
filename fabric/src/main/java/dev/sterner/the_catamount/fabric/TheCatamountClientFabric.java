package dev.sterner.the_catamount.fabric;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import dev.sterner.the_catamount.ClientCatamountConfig;
import dev.sterner.the_catamount.TheCatamount;
import dev.sterner.the_catamount.client.CatamountHudOverlay;
import dev.sterner.the_catamount.client.ClientTickHandler;
import dev.sterner.the_catamount.client.StrugglingSpiritParticle;
import dev.sterner.the_catamount.client.model.DevouredModel;
import dev.sterner.the_catamount.client.model.WindEntityModel;
import dev.sterner.the_catamount.client.render.*;
import dev.sterner.the_catamount.payload.*;
import dev.sterner.the_catamount.registry.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.network.chat.Component;

import java.io.IOException;

public class TheCatamountClientFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(TCEntityTypes.CATAMOUNT, CatamountEntityRenderer::new);
        EntityRendererRegistry.register(TCEntityTypes.DEVOURED, DevouredRenderer::new);
        EntityRendererRegistry.register(TCEntityTypes.WIND, WindEntityRenderer::new);
        EntityRendererRegistry.register(TCEntityTypes.LIGHT_ORB, LightOrbEntityRenderer::new);

        EntityModelLayerRegistry.registerModelLayer(DevouredModel.LAYER, DevouredModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(WindEntityModel.LAYER, WindEntityModel::createBodyLayer);

        BlockEntityRenderers.register(TCBlockEntityTypes.SUSPICIOUS_DIRT, SuspiciousDirtBlockEntityRenderer::new);

        BlockRenderLayerMap.INSTANCE.putBlock(TCBlocks.MONSTROUS_REMAINS, RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(TCBlocks.MONSTROUS_SKULL, RenderType.cutout());

        ClientPlayNetworking.registerGlobalReceiver(SyncPaleAnimalDataPayload.ID, (payload, ctx) -> {
            payload.handleS2C();
        });

        ClientPlayNetworking.registerGlobalReceiver(SyncCatamountPlayerDataPayload.ID, (payload, ctx) -> {
            payload.handleS2C();
        });

        ClientPlayNetworking.registerGlobalReceiver(PaleAnimalSyncPayload.ID, (payload, ctx) -> {
            payload.handleS2C();
        });

        ClientPlayNetworking.registerGlobalReceiver(EventTriggeredPayload.ID, (payload, ctx) -> {
            payload.handleS2C();
        });

        ClientPlayNetworking.registerGlobalReceiver(FogEffectPayload.ID, (payload, ctx) -> {
            payload.handleS2C();
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientTickHandler.onClientTick();
        });

        ParticleFactoryRegistry.getInstance().register(
                TCParticles.SPIRIT_FACE,
                StrugglingSpiritParticle.Provider::new
        );

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    ClientCommandManager.literal("catamounthud")
                            .then(ClientCommandManager.literal("toggle")
                                    .executes(context -> {
                                        ClientCatamountConfig.toggleHud();
                                        boolean enabled = ClientCatamountConfig.isHudEnabled();

                                        context.getSource().sendFeedback(
                                                Component.literal("Catamount HUD: " + (enabled ? "ON" : "OFF"))
                                                        .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED)
                                        );
                                        return 1;
                                    })
                            )
                            .then(ClientCommandManager.literal("on")
                                    .executes(context -> {
                                        ClientCatamountConfig.setHudEnabled(true);
                                        context.getSource().sendFeedback(
                                                Component.literal("Catamount HUD: ON").withStyle(ChatFormatting.GREEN)
                                        );
                                        return 1;
                                    })
                            )
                            .then(ClientCommandManager.literal("off")
                                    .executes(context -> {
                                        ClientCatamountConfig.setHudEnabled(false);
                                        context.getSource().sendFeedback(
                                                Component.literal("Catamount HUD: OFF").withStyle(ChatFormatting.RED)
                                        );
                                        return 1;
                                    })
                            )
            );
        });

        HudRenderCallback.EVENT.register(CatamountHudOverlay::render);

        CoreShaderRegistrationCallback.EVENT.register((resourceManager) -> {
            try {
                resourceManager.register(
                        TheCatamount.id("rendertype_entity_desaturated"),
                        DefaultVertexFormat.NEW_ENTITY,
                        TCShaders::setRendertypeEntityDesaturated
                );
            } catch (IOException e) {
                TheCatamount.LOGGER.error("Failed to register desaturated entity shader", e);
            }
        });
    }
}
