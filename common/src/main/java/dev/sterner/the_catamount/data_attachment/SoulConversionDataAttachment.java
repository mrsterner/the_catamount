package dev.sterner.the_catamount.data_attachment;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.sterner.the_catamount.TheCatamount;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public class SoulConversionDataAttachment {

    @ExpectPlatform
    public static Data getData(ServerLevel level) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void setData(ServerLevel level, Data data) {
        throw new AssertionError();
    }

    public record Data(Map<BlockPos, ConversionRecord> activeConversions) {

        public Data() {
            this(new HashMap<>());
        }

        public Data withConversion(BlockPos pos, ConversionRecord record) {
            Map<BlockPos, ConversionRecord> newMap = new HashMap<>(activeConversions);
            newMap.put(pos.immutable(), record);
            return new Data(newMap);
        }

        public Data withoutConversion(BlockPos pos) {
            Map<BlockPos, ConversionRecord> newMap = new HashMap<>(activeConversions);
            newMap.remove(pos);
            return new Data(newMap);
        }

        private record SerializedEntry(BlockPos pos, ConversionRecord record) {
            public static final Codec<SerializedEntry> CODEC = RecordCodecBuilder.create(instance ->
                    instance.group(
                            BlockPos.CODEC.fieldOf("pos").forGetter(SerializedEntry::pos),
                            ConversionRecord.CODEC.fieldOf("record").forGetter(SerializedEntry::record)
                    ).apply(instance, SerializedEntry::new)
            );
        }

        public static final Codec<Data> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        SerializedEntry.CODEC.listOf()
                                .xmap(
                                        list -> {
                                            Map<BlockPos, ConversionRecord> map = new HashMap<>();
                                            list.forEach(entry -> {
                                                BlockPos immutablePos = entry.pos().immutable();
                                                map.put(immutablePos, entry.record());
                                            });
                                            return map;
                                        },
                                        map -> map.entrySet().stream()
                                                .map(entry -> new SerializedEntry(entry.getKey().immutable(), entry.getValue()))
                                                .toList()
                                )
                                .fieldOf("activeConversions")
                                .forGetter(Data::activeConversions)
                ).apply(instance, Data::new)
        );

        public static final ResourceLocation ID = TheCatamount.id("soul_conversion_data");
    }

    public record ConversionRecord(BlockState originalState, long endTime) {

        public static final Codec<ConversionRecord> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        BlockState.CODEC.fieldOf("originalState").forGetter(c -> c.originalState),
                        Codec.LONG.fieldOf("endTime").forGetter(c -> c.endTime)
                ).apply(instance, ConversionRecord::new)
        );
    }
}