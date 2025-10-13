package dev.sterner.the_catamount.fabric.datagen;

import dev.sterner.the_catamount.TheCatamount;
import dev.sterner.the_catamount.listener.SoulConversionListener;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricCodecDataProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class TCSoulConversionProvider extends FabricCodecDataProvider<SoulConversionListener.ConversionData> {

    public TCSoulConversionProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(dataOutput, registriesFuture, PackOutput.Target.DATA_PACK, "soul_conversion", SoulConversionListener.ConversionData.CODEC);
    }

    @Override
    protected void configure(BiConsumer<ResourceLocation, SoulConversionListener.ConversionData> biConsumer, HolderLookup.Provider provider) {

    }

    private void makeBlock(
            BiConsumer<ResourceLocation, SoulConversionListener.ConversionData> provider,
            Block from,
            Block to
    ) {

        var fromId = BuiltInRegistries.BLOCK.getKey(from);
        var toId = BuiltInRegistries.BLOCK.getKey(to);
        provider.accept(TheCatamount.id(fromId.getPath()), new SoulConversionListener.ConversionData(fromId, toId));
    }

    @Override
    public String getName() {
        return "soul_conversion";
    }
}
