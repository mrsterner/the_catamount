package dev.sterner.the_catamount.registry;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;

import java.util.function.UnaryOperator;

public class TCDataComponents {

    public static final DataComponentType<Boolean> WHITE_ASH_COATED =
            build(builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    private static <T> DataComponentType<T> build(UnaryOperator<DataComponentType.Builder<T>> builder) {
        return builder.apply(DataComponentType.builder()).build();
    }
}
