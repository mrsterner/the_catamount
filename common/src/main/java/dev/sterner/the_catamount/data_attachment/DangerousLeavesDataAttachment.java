package dev.sterner.the_catamount.data_attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.sterner.the_catamount.TheCatamount;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.Map;

public class DangerousLeavesDataAttachment {

    @ExpectPlatform
    public static Data getData(ServerLevel level) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void setData(ServerLevel level, Data data) {
        throw new AssertionError();
    }

    public record Data(Map<BlockPos, Long> dangerousLeaves) {

        public Data() {
            this(new HashMap<>());
        }

        public Data withDangerousLeaf(BlockPos pos, long endTime) {
            Map<BlockPos, Long> newMap = new HashMap<>(dangerousLeaves);
            newMap.put(pos.immutable(), endTime);
            return new Data(newMap);
        }

        public Data withoutDangerousLeaf(BlockPos pos) {
            Map<BlockPos, Long> newMap = new HashMap<>(dangerousLeaves);
            newMap.remove(pos);
            return new Data(newMap);
        }

        private record SerializedEntry(BlockPos pos, long endTime) {
            public static final Codec<SerializedEntry> CODEC = RecordCodecBuilder.create(instance ->
                    instance.group(
                            BlockPos.CODEC.fieldOf("pos").forGetter(SerializedEntry::pos),
                            Codec.LONG.fieldOf("endTime").forGetter(SerializedEntry::endTime)
                    ).apply(instance, SerializedEntry::new)
            );
        }

        public static final Codec<Data> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        SerializedEntry.CODEC.listOf()
                                .xmap(
                                        list -> {
                                            Map<BlockPos, Long> map = new HashMap<>();
                                            list.forEach(entry -> map.put(entry.pos().immutable(), entry.endTime()));
                                            return map;
                                        },
                                        map -> map.entrySet().stream()
                                                .map(entry -> new SerializedEntry(entry.getKey().immutable(), entry.getValue()))
                                                .toList()
                                )
                                .fieldOf("dangerousLeaves")
                                .forGetter(Data::dangerousLeaves)
                ).apply(instance, Data::new)
        );

        public static final ResourceLocation ID = TheCatamount.id("dangerous_leaves_data");
    }
}