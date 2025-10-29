package dev.sterner.the_catamount.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

public class PetroglyphBlock extends HorizontalDirectionalBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<PetroglyphPart> PART = EnumProperty.create("part", PetroglyphPart.class);

    public PetroglyphBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, PetroglyphPart.SINGLE));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return simpleCodec(PetroglyphBlock::new);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var dir = context.getHorizontalDirection();
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            dir = dir.getOpposite();
        }
        return this.defaultBlockState()
                .setValue(FACING, dir);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide && !oldState.is(this)) {
            checkAndFormMultiblock(level, pos, state);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide && !newState.is(this)) {
            breakMultiblock(level, pos, state);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    private void checkAndFormMultiblock(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        Direction right = facing.getClockWise();

        BlockPos topLeft = pos;
        BlockPos topRight = pos.relative(right);
        BlockPos bottomLeft = pos.relative(Direction.DOWN);
        BlockPos bottomRight = pos.relative(right).relative(Direction.DOWN);

        if (canFormMultiblock(level, topLeft, topRight, bottomLeft, bottomRight, facing)) {
            formMultiblock(level, topLeft, topRight, bottomLeft, bottomRight, facing);
        } else {
            tryFormFromPosition(level, pos, facing, right);
        }
    }

    private void tryFormFromPosition(Level level, BlockPos pos, Direction facing, Direction right) {
        BlockPos tl = pos.relative(right.getOpposite());
        BlockPos tr = pos;
        BlockPos bl = pos.relative(right.getOpposite()).relative(Direction.DOWN);
        BlockPos br = pos.relative(Direction.DOWN);
        if (canFormMultiblock(level, tl, tr, bl, br, facing)) {
            formMultiblock(level, tl, tr, bl, br, facing);
            return;
        }

        tl = pos.relative(Direction.UP);
        tr = pos.relative(right).relative(Direction.UP);
        bl = pos;
        br = pos.relative(right);
        if (canFormMultiblock(level, tl, tr, bl, br, facing)) {
            formMultiblock(level, tl, tr, bl, br, facing);
            return;
        }

        tl = pos.relative(right.getOpposite()).relative(Direction.UP);
        tr = pos.relative(Direction.UP);
        bl = pos.relative(right.getOpposite());
        br = pos;
        if (canFormMultiblock(level, tl, tr, bl, br, facing)) {
            formMultiblock(level, tl, tr, bl, br, facing);
        }
    }

    private boolean canFormMultiblock(Level level, BlockPos topLeft, BlockPos topRight,
                                      BlockPos bottomLeft, BlockPos bottomRight, Direction facing) {
        return isValidPetroglyphForMultiblock(level, topLeft, facing) &&
                isValidPetroglyphForMultiblock(level, topRight, facing) &&
                isValidPetroglyphForMultiblock(level, bottomLeft, facing) &&
                isValidPetroglyphForMultiblock(level, bottomRight, facing);
    }

    private boolean isValidPetroglyphForMultiblock(Level level, BlockPos pos, Direction facing) {
        BlockState state = level.getBlockState(pos);
        return state.is(this) &&
                state.getValue(FACING) == facing &&
                state.getValue(PART) == PetroglyphPart.SINGLE;
    }

    private void formMultiblock(Level level, BlockPos topLeft, BlockPos topRight,
                                BlockPos bottomLeft, BlockPos bottomRight, Direction facing) {
        level.setBlock(topLeft, level.getBlockState(topLeft).setValue(PART, PetroglyphPart.TOP_LEFT), 3);
        level.setBlock(topRight, level.getBlockState(topRight).setValue(PART, PetroglyphPart.TOP_RIGHT), 3);
        level.setBlock(bottomLeft, level.getBlockState(bottomLeft).setValue(PART, PetroglyphPart.BOTTOM_LEFT), 3);
        level.setBlock(bottomRight, level.getBlockState(bottomRight).setValue(PART, PetroglyphPart.BOTTOM_RIGHT), 3);
    }

    private void breakMultiblock(Level level, BlockPos pos, BlockState state) {
        PetroglyphPart part = state.getValue(PART);
        if (part == PetroglyphPart.SINGLE) return;

        Direction facing = state.getValue(FACING);
        Direction right = facing.getClockWise();

        BlockPos topLeft, topRight, bottomLeft, bottomRight;

        switch (part) {
            case TOP_LEFT -> {
                topLeft = pos;
                topRight = pos.relative(right);
                bottomLeft = pos.relative(Direction.DOWN);
                bottomRight = pos.relative(right).relative(Direction.DOWN);
            }
            case TOP_RIGHT -> {
                topLeft = pos.relative(right.getOpposite());
                topRight = pos;
                bottomLeft = pos.relative(right.getOpposite()).relative(Direction.DOWN);
                bottomRight = pos.relative(Direction.DOWN);
            }
            case BOTTOM_LEFT -> {
                topLeft = pos.relative(Direction.UP);
                topRight = pos.relative(right).relative(Direction.UP);
                bottomLeft = pos;
                bottomRight = pos.relative(right);
            }
            case BOTTOM_RIGHT -> {
                topLeft = pos.relative(right.getOpposite()).relative(Direction.UP);
                topRight = pos.relative(Direction.UP);
                bottomLeft = pos.relative(right.getOpposite());
                bottomRight = pos;
            }
            default -> { return; }
        }

        resetToSingle(level, topLeft);
        resetToSingle(level, topRight);
        resetToSingle(level, bottomLeft);
        resetToSingle(level, bottomRight);
    }

    private void resetToSingle(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(this) && state.getValue(PART) != PetroglyphPart.SINGLE) {
            level.setBlock(pos, state.setValue(PART, PetroglyphPart.SINGLE), 3);
        }
    }

    public enum PetroglyphPart implements net.minecraft.util.StringRepresentable {
        SINGLE("single"),
        TOP_LEFT("top_left"),
        TOP_RIGHT("top_right"),
        BOTTOM_LEFT("bottom_left"),
        BOTTOM_RIGHT("bottom_right");

        private final String name;

        PetroglyphPart(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}