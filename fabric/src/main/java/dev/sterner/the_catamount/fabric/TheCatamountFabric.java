package dev.sterner.the_catamount.fabric;

import dev.sterner.the_catamount.TheCatamount;
import dev.sterner.the_catamount.data_attachment.TCDataAttachmentsFabric;
import dev.sterner.the_catamount.entity.CatamountEntity;
import dev.sterner.the_catamount.events.ModEventHandlers;
import dev.sterner.the_catamount.listener.SoulConversionListener;
import dev.sterner.the_catamount.payload.SyncCatamountPlayerDataPayload;
import dev.sterner.the_catamount.registry.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class TheCatamountFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        TheCatamount.init();
        TCDataAttachmentsFabric.init();
        Registry.register(BuiltInRegistries.ITEM,  TheCatamount.id("beast_ivory"), TCItems.BEAST_IVORY);
        Registry.register(BuiltInRegistries.ITEM,  TheCatamount.id("white_ash"), TCItems.WHITE_ASH);
        Registry.register(BuiltInRegistries.ITEM,  TheCatamount.id("bone_heap"), TCItems.BONE_HEAP);
        Registry.register(BuiltInRegistries.ITEM,  TheCatamount.id("suspicious_dirt"), TCItems.SUSPICIOUS_DIRT);

        Registry.register(BuiltInRegistries.BLOCK,  TheCatamount.id("white_ash"), TCBlocks.WHITE_ASH);
        Registry.register(BuiltInRegistries.BLOCK,  TheCatamount.id("bone_heap"), TCBlocks.BONE_HEAP);
        Registry.register(BuiltInRegistries.BLOCK, TheCatamount.id("suspicious_dirt"), TCBlocks.SUSPICIOUS_DIRT);

        Registry.register(BuiltInRegistries.ENTITY_TYPE,  TheCatamount.id("catamount"), TCEntityTypes.CATAMOUNT);
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,  TheCatamount.id("suspicious_dirt"), TCBlockEntityTypes.SUSPICIOUS_DIRT);
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TheCatamount.MOD_ID, new TCCreativeTabs().createMain());

        FabricDefaultAttributeRegistry.register(TCEntityTypes.CATAMOUNT, CatamountEntity.createAttributes().build() );
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, TheCatamount.id("white_ash_coated"), TCDataComponents.WHITE_ASH_COATED);

        ServerTickEvents.END_WORLD_TICK.register(ModEventHandlers::onServerLevelTick);

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new SoulConversionListenerFabric());

        ServerLivingEntityEvents.AFTER_DEATH.register(ModEventHandlers::onLivingDeath);

        CommandRegistrationCallback.EVENT.register((dispatcher, ctx, selection) -> {
            TCCommands.register(dispatcher);
        });

        PayloadTypeRegistry.playS2C().register(
                SyncCatamountPlayerDataPayload.ID,
                SyncCatamountPlayerDataPayload.STREAM_CODEC
        );
    }

    static class SoulConversionListenerFabric extends SoulConversionListener implements IdentifiableResourceReloadListener {

        @Override
        public ResourceLocation getFabricId() {
            return TheCatamount.id("soul_conversion");
        }

        @Override
        public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
            return LOADER.reload(preparationBarrier, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor);
        }
    }
}
