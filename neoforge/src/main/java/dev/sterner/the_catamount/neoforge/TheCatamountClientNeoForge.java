package dev.sterner.the_catamount.neoforge;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import dev.sterner.the_catamount.ClientCatamountConfig;
import dev.sterner.the_catamount.TheCatamount;
import dev.sterner.the_catamount.client.CatamountHudOverlay;
import dev.sterner.the_catamount.client.ClientTickHandler;
import dev.sterner.the_catamount.client.render.*;
import dev.sterner.the_catamount.registry.TCBlockEntityTypes;
import dev.sterner.the_catamount.registry.TCBlocks;
import dev.sterner.the_catamount.registry.TCEntityTypes;
import dev.sterner.the_catamount.registry.TCShaders;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.io.IOException;

@EventBusSubscriber(value = Dist.CLIENT)
public class TheCatamountClientNeoForge {

    @SubscribeEvent
    public static void onClientSetup(EntityRenderersEvent.RegisterRenderers event){
        event.registerEntityRenderer(TCEntityTypes.CATAMOUNT, CatamountEntityRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntityTypes.SUSPICIOUS_DIRT, SuspiciousDirtBlockEntityRenderer::new);
        event.registerEntityRenderer(TCEntityTypes.DEVOURED, DevouredRenderer::new);
        event.registerEntityRenderer(TCEntityTypes.WIND, WindEntityRenderer::new);
        event.registerEntityRenderer(TCEntityTypes.LIGHT_ORB, LightOrbEntityRenderer::new);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        ClientTickHandler.onClientTick();
    }

    @SubscribeEvent
    public static void registerRenderTypes(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(TCBlocks.MONSTROUS_REMAINS, RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(TCBlocks.MONSTROUS_SKULL, RenderType.cutout());
        });
    }

    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGuiLayerEvent.Post event) {
        if (event.getName().equals(VanillaGuiLayers.HOTBAR)) {
            CatamountHudOverlay.render(event.getGuiGraphics(), event.getPartialTick());
        }
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(),
                        TheCatamount.id("rendertype_entity_desaturated"),
                        DefaultVertexFormat.NEW_ENTITY
                ),
                TCShaders::setRendertypeEntityDesaturated
        );
    }

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("catamounthud")
                        .then(Commands.literal("toggle")
                                .executes(context -> {
                                    ClientCatamountConfig.toggleHud();
                                    boolean enabled = ClientCatamountConfig.isHudEnabled();

                                    context.getSource().sendSuccess(
                                            () -> Component.literal("Catamount HUD: " + (enabled ? "ON" : "OFF"))
                                                    .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED),
                                            false
                                    );
                                    return 1;
                                })
                        )
                        .then(Commands.literal("on")
                                .executes(context -> {
                                    ClientCatamountConfig.setHudEnabled(true);
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("Catamount HUD: ON")
                                                    .withStyle(ChatFormatting.GREEN),
                                            false
                                    );
                                    return 1;
                                })
                        )
                        .then(Commands.literal("off")
                                .executes(context -> {
                                    ClientCatamountConfig.setHudEnabled(false);
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("Catamount HUD: OFF")
                                                    .withStyle(ChatFormatting.RED),
                                            false
                                    );
                                    return 1;
                                })
                        )
        );
    }
}
