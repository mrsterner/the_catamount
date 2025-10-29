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

public class TCModelProvider extends FabricModelProvider {
    public TCModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators generators) {
        generators.createGenericCube(TCBlocks.BONE_HEAP);
        generators.createBrushableBlock(TCBlocks.SUSPICIOUS_DIRT);

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
        ResourceLocation stoneTexture = ResourceLocation.withDefaultNamespace("block/stone");

        ResourceLocation singleTexture = TheCatamount.id("block/" + name + "_petroglyph");
        ResourceLocation singleModel = createPetroglyphModel(generators, block, name + "_single", singleTexture, stoneTexture);

        ResourceLocation tl = TheCatamount.id("block/" + name + "_petroglyph_full_top_left");
        ResourceLocation tr = TheCatamount.id("block/" + name + "_petroglyph_full_top_right");
        ResourceLocation bl = TheCatamount.id("block/" + name + "_petroglyph_full_bottom_left");
        ResourceLocation br = TheCatamount.id("block/" + name + "_petroglyph_full_bottom_right");
        ResourceLocation topLeftModel = createPetroglyphPartModel(generators, name + "_top_left", tl, stoneTexture);
        ResourceLocation topRightModel = createPetroglyphPartModel(generators, name + "_top_right", tr, stoneTexture);
        ResourceLocation bottomLeftModel = createPetroglyphPartModel(generators, name + "_bottom_left", bl, stoneTexture);
        ResourceLocation bottomRightModel = createPetroglyphPartModel(generators, name + "_bottom_right", br, stoneTexture);

        MultiVariantGenerator generator = MultiVariantGenerator.multiVariant(block);

        PropertyDispatch.C2<Direction, PetroglyphBlock.PetroglyphPart> dispatch =
                PropertyDispatch.properties(PetroglyphBlock.FACING, PetroglyphBlock.PART);

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            dispatch = dispatch.select(dir, PetroglyphBlock.PetroglyphPart.SINGLE, createVariant(singleModel, dir));
        }

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            dispatch = dispatch.select(dir, PetroglyphBlock.PetroglyphPart.TOP_LEFT, createVariant(topLeftModel, dir));
        }

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            dispatch = dispatch.select(dir, PetroglyphBlock.PetroglyphPart.TOP_RIGHT, createVariant(topRightModel, dir));
        }

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            dispatch = dispatch.select(dir, PetroglyphBlock.PetroglyphPart.BOTTOM_LEFT, createVariant(bottomLeftModel, dir));
        }

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            dispatch = dispatch.select(dir, PetroglyphBlock.PetroglyphPart.BOTTOM_RIGHT, createVariant(bottomRightModel, dir));
        }

        generator = generator.with(dispatch);
        generators.blockStateOutput.accept(generator);
        //generators.delegateItemModel(block, singleModel);
    }

    private ResourceLocation createPetroglyphModel(BlockModelGenerators generators, Block block,
                                                   String modelName, ResourceLocation frontTexture,
                                                   ResourceLocation stoneTexture) {
        TextureMapping mapping = new TextureMapping()
                .put(TextureSlot.PARTICLE, stoneTexture)
                .put(TextureSlot.NORTH, frontTexture)
                .put(TextureSlot.SOUTH, stoneTexture)
                .put(TextureSlot.EAST, stoneTexture)
                .put(TextureSlot.WEST, stoneTexture)
                .put(TextureSlot.UP, stoneTexture)
                .put(TextureSlot.DOWN, stoneTexture);

        ResourceLocation modelLocation = TheCatamount.id("block/" + modelName);
        ModelTemplates.CUBE.create(modelLocation, mapping, generators.modelOutput);
        return modelLocation;
    }

    private ResourceLocation createPetroglyphPartModel(BlockModelGenerators generators,
                                                       String modelName, ResourceLocation fullTexture,
                                                       ResourceLocation stoneTexture) {
        TextureMapping mapping = new TextureMapping()
                .put(TextureSlot.PARTICLE, stoneTexture)
                .put(TextureSlot.NORTH, fullTexture)
                .put(TextureSlot.SOUTH, stoneTexture)
                .put(TextureSlot.EAST, stoneTexture)
                .put(TextureSlot.WEST, stoneTexture)
                .put(TextureSlot.UP, stoneTexture)
                .put(TextureSlot.DOWN, stoneTexture);

        ResourceLocation modelLocation = TheCatamount.id("block/" + modelName);
        ModelTemplates.CUBE.create(modelLocation, mapping, generators.modelOutput);
        return modelLocation;
    }

    private Variant createVariant(ResourceLocation model, Direction facing) {
        VariantProperties.Rotation rotation = switch (facing) {
            case NORTH -> VariantProperties.Rotation.R180;
            case SOUTH -> VariantProperties.Rotation.R0;
            case WEST -> VariantProperties.Rotation.R90;
            case EAST -> VariantProperties.Rotation.R270;
            default -> VariantProperties.Rotation.R0;
        };

        return Variant.variant()
                .with(VariantProperties.MODEL, model)
                .with(VariantProperties.Y_ROT, rotation);
    }
}