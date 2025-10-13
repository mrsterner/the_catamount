package dev.sterner.the_catamount.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

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

}
