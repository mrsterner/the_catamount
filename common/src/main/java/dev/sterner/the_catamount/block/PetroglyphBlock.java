package dev.sterner.the_catamount.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;


public class PetroglyphBlock extends HorizontalDirectionalBlock {

    public static final EnumProperty<Type> TYPE = EnumProperty.create("type", Type.class);

    public PetroglyphBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(TYPE, Type.AWAKEN).setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return simpleCodec(PetroglyphBlock::new);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        RandomSource random = context.getLevel().getRandom();
        Type[] types = Type.values();
        Type randomType = types[random.nextInt(types.length)];

        Direction facing = context.getHorizontalDirection();

        return this.defaultBlockState()
                .setValue(TYPE, randomType)
                .setValue(FACING, facing);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TYPE, FACING);
    }

    public enum Type implements StringRepresentable {
        AWAKEN,
        DEVOUR,
        LIGHTENING,
        SLAIN;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}