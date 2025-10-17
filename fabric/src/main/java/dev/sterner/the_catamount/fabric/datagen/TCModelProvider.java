package dev.sterner.the_catamount.fabric.datagen;

import dev.sterner.the_catamount.TheCatamount;
import dev.sterner.the_catamount.block.PetroglyphBlock;
import dev.sterner.the_catamount.registry.TCBlocks;
import dev.sterner.the_catamount.registry.TCItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.core.Direction;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.model.ModelLocationUtils;
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

        // Petroglyphs
        createPetroglyphBlock(generators, TCBlocks.DEVOUR_PETROGLYPH, "devour");
        createPetroglyphBlock(generators, TCBlocks.AWAKEN_PETROGLYPH, "awaken");
        createPetroglyphBlock(generators, TCBlocks.LIGHTENING_PETROGLYPH, "lightening");
        createPetroglyphBlock(generators, TCBlocks.SLAIN_PETROGLYPH, "slain");
    }

    @Override
    public void generateItemModels(ItemModelGenerators generators) {
        generators.generateFlatItem(TCItems.BEAST_IVORY, ModelTemplates.FLAT_ITEM);
        generators.generateFlatItem(TCItems.WHITE_ASH, ModelTemplates.FLAT_ITEM);
    }

    private void createPetroglyphBlock(BlockModelGenerators generators, Block block, String name) {
        ResourceLocation petroglyphTexture = TheCatamount.id("block/" + name + "_petroglyph");
        ResourceLocation stoneTexture = ResourceLocation.withDefaultNamespace("block/stone");

        TextureMapping mapping = new TextureMapping()
                .put(TextureSlot.PARTICLE, stoneTexture)
                .put(TextureSlot.NORTH, petroglyphTexture)
                .put(TextureSlot.SOUTH, stoneTexture)
                .put(TextureSlot.EAST, stoneTexture)
                .put(TextureSlot.WEST, stoneTexture)
                .put(TextureSlot.UP, stoneTexture)
                .put(TextureSlot.DOWN, stoneTexture);

        ResourceLocation modelLocation = ModelLocationUtils.getModelLocation(block);

        ModelTemplates.CUBE.create(modelLocation, mapping, generators.modelOutput);

        MultiVariantGenerator generator = MultiVariantGenerator.multiVariant(block)
                .with(PropertyDispatch.property(BlockStateProperties.HORIZONTAL_FACING)
                        .select(Direction.NORTH, Variant.variant()
                                .with(VariantProperties.MODEL, modelLocation)
                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                        .select(Direction.SOUTH, Variant.variant()
                                .with(VariantProperties.MODEL, modelLocation))
                        .select(Direction.WEST, Variant.variant()
                                .with(VariantProperties.MODEL, modelLocation)
                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                        .select(Direction.EAST, Variant.variant()
                                .with(VariantProperties.MODEL, modelLocation)
                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                );

        generators.blockStateOutput.accept(generator);

        generators.delegateItemModel(block, modelLocation);
    }

}

