package dev.sterner.the_catamount.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class MonstrousRemainsBlock extends AbstractMonstrousSkeletonBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<PartType> PART = EnumProperty.create("part", PartType.class);

    public MonstrousRemainsBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, PartType.CORE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(PART).getShape();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();

        if (canPlaceMultiblock(level, pos, facing)) {
            return this.defaultBlockState()
                    .setValue(FACING, facing)
                    .setValue(PART, PartType.CORE);
        }
        return null;
    }

    private boolean canPlaceMultiblock(Level level, BlockPos corePos, Direction facing) {
        for (PartType part : PartType.values()) {
            if (part == PartType.CORE) continue;
            BlockPos partPos = corePos.offset(part.getOffset(facing));
            if (!level.getBlockState(partPos).canBeReplaced()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (!level.isClientSide) {
            Direction facing = state.getValue(FACING);

            for (PartType part : PartType.values()) {
                if (part == PartType.CORE) continue;

                BlockPos partPos = pos.offset(part.getOffset(facing));
                level.setBlock(partPos, state.setValue(PART, part), 3);
            }
        }
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    public enum PartType implements StringRepresentable {
        CORE(0, 0, Block.box(0, 0, 0, 16, 12, 16)),
        FRONT(1, 0, Block.box(0, 0, 0, 16, 5, 16)),
        LEFT(0, -1, Block.box(0, 0, 0, 16, 6, 16)),
        RIGHT(0, 1, Block.box(0, 0, 0, 16, 8, 16));

        private final int offsetX;
        private final int offsetZ;
        private final VoxelShape shape;

        PartType(int offsetX, int offsetZ, VoxelShape shape) {
            this.offsetX = offsetX;
            this.offsetZ = offsetZ;
            this.shape = shape;
        }

        public VoxelShape getShape() {
            return shape;
        }

        public Vec3i getOffset(Direction facing) {
            return switch (facing) {
                case NORTH -> new Vec3i(-offsetX, 0, -offsetZ);
                case SOUTH -> new Vec3i(offsetX, 0, offsetZ);
                case WEST -> new Vec3i(-offsetZ, 0, offsetX);
                case EAST -> new Vec3i(offsetZ, 0, -offsetX);
                default -> Vec3i.ZERO;
            };
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }
    }
}