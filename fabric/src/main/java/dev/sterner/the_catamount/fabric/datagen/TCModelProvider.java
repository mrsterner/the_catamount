package dev.sterner.the_catamount.fabric.datagen;

import dev.sterner.the_catamount.registry.TCBlocks;
import dev.sterner.the_catamount.registry.TCItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class TCModelProvider extends FabricModelProvider {
    public TCModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators generators) {
        generators.createGenericCube(TCBlocks.BONE_HEAP);
        generators.createBrushableBlock(TCBlocks.SUSPICIOUS_DIRT);
    }


    @Override
    public void generateItemModels(ItemModelGenerators generators) {
        generators.generateFlatItem(TCItems.BEAST_IVORY, ModelTemplates.FLAT_ITEM);
        generators.generateFlatItem(TCItems.WHITE_ASH, ModelTemplates.FLAT_ITEM);
    }
}
