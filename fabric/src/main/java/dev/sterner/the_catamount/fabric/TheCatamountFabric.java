package dev.sterner.the_catamount.fabric;

import dev.sterner.the_catamount.TheCatamount;
import dev.sterner.the_catamount.listener.SoulConversionListener;
import dev.sterner.the_catamount.registry.TCBlocks;
import dev.sterner.the_catamount.registry.TCCreativeTabs;
import dev.sterner.the_catamount.registry.TCEntityTypes;
import dev.sterner.the_catamount.registry.TCItems;
import net.fabricmc.api.ModInitializer;
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

        Registry.register(BuiltInRegistries.ITEM, TheCatamount.MOD_ID, TCItems.BEAST_IVORY);
        Registry.register(BuiltInRegistries.ITEM, TheCatamount.MOD_ID, TCItems.WHITE_ASH);
        Registry.register(BuiltInRegistries.ITEM, TheCatamount.MOD_ID, TCItems.BONE_HEAP);

        Registry.register(BuiltInRegistries.BLOCK, TheCatamount.MOD_ID, TCBlocks.WHITE_ASH);
        Registry.register(BuiltInRegistries.BLOCK, TheCatamount.MOD_ID, TCBlocks.BONE_HEAP);

        Registry.register(BuiltInRegistries.ENTITY_TYPE, TheCatamount.MOD_ID, TCEntityTypes.CATAMOUNT);
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TheCatamount.MOD_ID, new TCCreativeTabs().createMain());

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new SoulConversionListenerFabric());
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
