package dev.sterner.the_catamount.fabric.datagen;

import dev.sterner.the_catamount.registry.TCBlocks;
import dev.sterner.the_catamount.registry.TCEntityTypes;
import dev.sterner.the_catamount.registry.TCItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public class TCLangProvider extends FabricLanguageProvider {
    public TCLangProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generateTranslations(HolderLookup.Provider provider, TranslationBuilder builder) {
        builder.add("itemGroup.the_catamount", "The Catamount");

        builder.add(TCItems.BEAST_IVORY, "Beast Ivory");

        builder.add(TCBlocks.BONE_HEAP, "Bone Heap");
        builder.add(TCBlocks.WHITE_ASH, "White Ash");
        builder.add(TCBlocks.SUSPICIOUS_DIRT, "Suspicious Dirt");
        builder.add(TCBlocks.PETROGLYPH, "Petroglyph");

        builder.add(TCEntityTypes.CATAMOUNT, "Catamount");
    }
}
