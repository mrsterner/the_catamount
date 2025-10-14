package dev.sterner.the_catamount.neoforge;

import dev.sterner.the_catamount.client.render.CatamountEntityRenderer;
import dev.sterner.the_catamount.client.render.SuspiciousDirtBlockEntityRenderer;
import dev.sterner.the_catamount.registry.TCBlockEntityTypes;
import dev.sterner.the_catamount.registry.TCEntityTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class TheCatamountClientNeoForge {

    @SubscribeEvent
    public static void onClientSetup(EntityRenderersEvent.RegisterRenderers event){
        event.registerEntityRenderer(TCEntityTypes.CATAMOUNT, CatamountEntityRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntityTypes.SUSPICIOUS_DIRT, SuspiciousDirtBlockEntityRenderer::new);
    }
}
