package dev.sterner.the_catamount.fabric;

import dev.sterner.the_catamount.TheCatamount;
import dev.sterner.the_catamount.data_attachment.PaleAnimalDataAttachment;
import dev.sterner.the_catamount.data_attachment.TCDataAttachmentsFabric;
import dev.sterner.the_catamount.entity.CatamountEntity;
import dev.sterner.the_catamount.entity.DevouredEntity;
import dev.sterner.the_catamount.events.ModEventHandlers;
import dev.sterner.the_catamount.listener.SoulConversionListener;
import dev.sterner.the_catamount.payload.*;
import dev.sterner.the_catamount.registry.*;
import dev.sterner.the_catamount.registry.fabric.TCParticlesFabric;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class TheCatamountFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        TheCatamount.init();
        TCDataAttachmentsFabric.init();
        TCParticlesFabric.init();

        Registry.register(BuiltInRegistries.ITEM,  TheCatamount.id("beast_ivory"), TCItems.BEAST_IVORY);
        Registry.register(BuiltInRegistries.ITEM,  TheCatamount.id("white_ash"), TCItems.WHITE_ASH);
        Registry.register(BuiltInRegistries.ITEM,  TheCatamount.id("bone_heap"), TCItems.BONE_HEAP);
        Registry.register(BuiltInRegistries.ITEM,  TheCatamount.id("suspicious_dirt"), TCItems.SUSPICIOUS_DIRT);
        Registry.register(BuiltInRegistries.ITEM, TheCatamount.id("devour_petroglyph"), TCItems.DEVOUR_PETROGLYPH);
        Registry.register(BuiltInRegistries.ITEM, TheCatamount.id("awaken_petroglyph"), TCItems.AWAKEN_PETROGLYPH);
        Registry.register(BuiltInRegistries.ITEM, TheCatamount.id("lightening_petroglyph"), TCItems.LIGHTENING_PETROGLYPH);
        Registry.register(BuiltInRegistries.ITEM, TheCatamount.id("slain_petroglyph"), TCItems.SLAIN_PETROGLYPH);

        Registry.register(BuiltInRegistries.ITEM, TheCatamount.id("monstrous_remains"), TCItems.MONSTROUS_REMAINS);
        Registry.register(BuiltInRegistries.ITEM, TheCatamount.id("monstrous_skull"), TCItems.MONSTROUS_SKULL);

        Registry.register(BuiltInRegistries.BLOCK,  TheCatamount.id("white_ash"), TCBlocks.WHITE_ASH);
        Registry.register(BuiltInRegistries.BLOCK,  TheCatamount.id("bone_heap"), TCBlocks.BONE_HEAP);
        Registry.register(BuiltInRegistries.BLOCK, TheCatamount.id("suspicious_dirt"), TCBlocks.SUSPICIOUS_DIRT);
        Registry.register(BuiltInRegistries.BLOCK, TheCatamount.id("devour_petroglyph"), TCBlocks.DEVOUR_PETROGLYPH);
        Registry.register(BuiltInRegistries.BLOCK, TheCatamount.id("awaken_petroglyph"), TCBlocks.AWAKEN_PETROGLYPH);
        Registry.register(BuiltInRegistries.BLOCK, TheCatamount.id("lightening_petroglyph"), TCBlocks.LIGHTENING_PETROGLYPH);
        Registry.register(BuiltInRegistries.BLOCK, TheCatamount.id("slain_petroglyph"), TCBlocks.SLAIN_PETROGLYPH);

        Registry.register(BuiltInRegistries.BLOCK, TheCatamount.id("monstrous_remains"), TCBlocks.MONSTROUS_REMAINS);
        Registry.register(BuiltInRegistries.BLOCK, TheCatamount.id("monstrous_skull"), TCBlocks.MONSTROUS_SKULL);

        Registry.register(BuiltInRegistries.ENTITY_TYPE,  TheCatamount.id("catamount"), TCEntityTypes.CATAMOUNT);
        Registry.register(BuiltInRegistries.ENTITY_TYPE,  TheCatamount.id("devoured"), TCEntityTypes.DEVOURED);
        Registry.register(BuiltInRegistries.ENTITY_TYPE,  TheCatamount.id("wind"), TCEntityTypes.WIND);
        Registry.register(BuiltInRegistries.ENTITY_TYPE,  TheCatamount.id("light_orb"), TCEntityTypes.LIGHT_ORB);
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,  TheCatamount.id("suspicious_dirt"), TCBlockEntityTypes.SUSPICIOUS_DIRT);
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,  TheCatamount.id("monstrous_remains"), TCBlockEntityTypes.MONSTROUS_REMAINS);
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TheCatamount.MOD_ID, new TCCreativeTabs().createMain());

        FabricDefaultAttributeRegistry.register(TCEntityTypes.CATAMOUNT, CatamountEntity.createAttributes().build() );
        FabricDefaultAttributeRegistry.register(TCEntityTypes.DEVOURED, DevouredEntity.createAttributes().build() );
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

        PayloadTypeRegistry.playS2C().register(
                SyncPaleAnimalDataPayload.ID,
                SyncPaleAnimalDataPayload.STREAM_CODEC
        );

        PayloadTypeRegistry.playS2C().register(
                PaleAnimalSyncPayload.ID,
                PaleAnimalSyncPayload.STREAM_CODEC
        );

        PayloadTypeRegistry.playS2C().register(
                EventTriggeredPayload.ID,
                EventTriggeredPayload.STREAM_CODEC
        );

        PayloadTypeRegistry.playS2C().register(
                FogEffectPayload.ID,
                FogEffectPayload.STREAM_CODEC
        );

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            ServerLevel level = player.serverLevel();

            PaleAnimalDataAttachment.Data paleData = PaleAnimalDataAttachment.getData(level);
            PaleAnimalDataAttachment.sync(level, paleData, List.of(player));
        });
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
