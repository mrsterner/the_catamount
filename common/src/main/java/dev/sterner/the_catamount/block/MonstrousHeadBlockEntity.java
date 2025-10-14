package dev.sterner.the_catamount.block;

import dev.sterner.the_catamount.registry.TCBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MonstrousHeadBlockEntity extends BlockEntity {
    public MonstrousHeadBlockEntity(BlockPos pos, BlockState blockState) {
        super(TCBlockEntityTypes.MONSTROUS_REMAINS, pos, blockState);
    }
}