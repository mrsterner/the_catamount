package dev.sterner.the_catamount.block;

import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;


public class PetroglyphBlock extends Block {

    public static final EnumProperty<Type> TYPE = EnumProperty.create("type", Type.class);

    public PetroglyphBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(TYPE, Type.AWAKEN));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        RandomSource random = context.getLevel().getRandom();
        Type[] types = Type.values();
        Type randomType = types[random.nextInt(types.length)];
        return this.defaultBlockState().setValue(TYPE, randomType);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TYPE);
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