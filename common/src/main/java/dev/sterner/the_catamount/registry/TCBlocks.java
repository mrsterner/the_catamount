package dev.sterner.the_catamount.registry;

import dev.sterner.the_catamount.block.MonstrousHeadBlock;
import dev.sterner.the_catamount.block.MonstrousRemainsBlock;
import dev.sterner.the_catamount.block.PetroglyphBlock;
import dev.sterner.the_catamount.block.SuspiciousDirtBlock;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class TCBlocks {

    public static final Block BONE_HEAP = new Block(BlockBehaviour.Properties.of()
            .strength(1.5f)
            .sound(SoundType.BONE_BLOCK)
            .noOcclusion()
    );

    public static final Block WHITE_ASH = new Block(BlockBehaviour.Properties.of()
            .strength(0.2f)
            .sound(SoundType.SAND)
    );

    public static final Block PETROGLYPH = new PetroglyphBlock(BlockBehaviour.Properties.of()
            .sound(SoundType.STONE)
    );

    public static final Block SUSPICIOUS_DIRT = new SuspiciousDirtBlock(Blocks.DIRT, SoundEvents.BRUSH_GRAVEL, SoundEvents.BRUSH_GRAVEL_COMPLETED, BlockBehaviour.Properties.of()
            .mapColor(MapColor.DIRT)
            .instrument(NoteBlockInstrument.SNARE)
            .strength(0.25F)
            .sound(SoundType.SUSPICIOUS_GRAVEL)
            .pushReaction(PushReaction.DESTROY)
    );

    public static final Block MONSTROUS_REMAINS = new MonstrousRemainsBlock(BlockBehaviour.Properties.of()
            .sound(SoundType.BONE_BLOCK).noOcclusion()
    );

    public static final Block MONSTROUS_SKULL = new MonstrousHeadBlock(BlockBehaviour.Properties.of()
            .sound(SoundType.BONE_BLOCK).noOcclusion()
    );

}
