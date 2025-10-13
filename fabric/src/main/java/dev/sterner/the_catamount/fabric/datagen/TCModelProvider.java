package dev.sterner.the_catamount.fabric.datagen;

import dev.sterner.the_catamount.registry.TCBlocks;
import dev.sterner.the_catamount.registry.TCItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.model.ModelTemplates;

public class TCModelProvider extends FabricModelProvider {
    public TCModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators generators) {
        generators.createGenericCube(TCBlocks.BONE_HEAP);
    }

    @Override
    public void generateItemModels(ItemModelGenerators generators) {
        generators.generateFlatItem(TCItems.BEAST_IVORY, ModelTemplates.FLAT_ITEM);
        generators.generateFlatItem(TCItems.WHITE_ASH, ModelTemplates.FLAT_ITEM);
    }
}
