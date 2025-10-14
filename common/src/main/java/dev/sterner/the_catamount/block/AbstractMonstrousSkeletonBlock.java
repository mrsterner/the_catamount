package dev.sterner.the_catamount.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractMonstrousSkeletonBlock extends Block {

    public AbstractMonstrousSkeletonBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            breakConnectedBlocks(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            breakConnectedBlocks(level, pos);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    protected void breakConnectedBlocks(Level level, BlockPos pos) {
        Set<BlockPos> toBreak = new HashSet<>();
        findConnectedBlocks(level, pos, toBreak, new HashSet<>());

        for (BlockPos breakPos : toBreak) {
            if (!breakPos.equals(pos)) {
                level.destroyBlock(breakPos, true);
            }
        }
    }

    protected void findConnectedBlocks(Level level, BlockPos pos, Set<BlockPos> toBreak, Set<BlockPos> visited) {
        if (visited.contains(pos)) {
            return;
        }
        visited.add(pos);

        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof AbstractMonstrousSkeletonBlock) {
            toBreak.add(pos);

            for (Direction direction : Direction.values()) {
                BlockPos adjacentPos = pos.relative(direction);
                findConnectedBlocks(level, adjacentPos, toBreak, visited);
            }
        }
    }
}