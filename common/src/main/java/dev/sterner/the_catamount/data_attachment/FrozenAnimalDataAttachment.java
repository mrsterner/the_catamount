package dev.sterner.the_catamount.data_attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.sterner.the_catamount.TheCatamount;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FrozenAnimalDataAttachment {

    @ExpectPlatform
    public static Data getData(ServerLevel level) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void setData(ServerLevel level, Data data) {
        throw new AssertionError();
    }

    public record Data(Map<UUID, FrozenRecord> frozenAnimals) {

        public Data() {
            this(new HashMap<>());
        }

        public Data withFrozenAnimal(UUID uuid, long endTime, float yaw, float pitch) {
            Map<UUID, FrozenRecord> newMap = new HashMap<>(frozenAnimals);
            newMap.put(uuid, new FrozenRecord(endTime, yaw, pitch));
            return new Data(newMap);
        }

        public Data withoutFrozenAnimal(UUID uuid) {
            Map<UUID, FrozenRecord> newMap = new HashMap<>(frozenAnimals);
            newMap.remove(uuid);
            return new Data(newMap);
        }

        private record SerializedEntry(UUID uuid, FrozenRecord record) {
            public static final Codec<SerializedEntry> CODEC = RecordCodecBuilder.create(instance ->
                    instance.group(
                            UUIDUtil.CODEC.fieldOf("uuid").forGetter(SerializedEntry::uuid),
                            FrozenRecord.CODEC.fieldOf("record").forGetter(SerializedEntry::record)
                    ).apply(instance, SerializedEntry::new)
            );
        }

        public static final Codec<Data> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        SerializedEntry.CODEC.listOf()
                                .xmap(
                                        list -> {
                                            Map<UUID, FrozenRecord> map = new HashMap<>();
                                            list.forEach(entry -> map.put(entry.uuid(), entry.record()));
                                            return map;
                                        },
                                        map -> map.entrySet().stream()
                                                .map(entry -> new SerializedEntry(entry.getKey(), entry.getValue()))
                                                .toList()
                                )
                                .fieldOf("frozenAnimals")
                                .forGetter(Data::frozenAnimals)
                ).apply(instance, Data::new)
        );

        public static final ResourceLocation ID = TheCatamount.id("frozen_animal_data");
    }

    public record FrozenRecord(long endTime, float yaw, float pitch) {
        public static final Codec<FrozenRecord> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.LONG.fieldOf("endTime").forGetter(FrozenRecord::endTime),
                        Codec.FLOAT.fieldOf("yaw").forGetter(FrozenRecord::yaw),
                        Codec.FLOAT.fieldOf("pitch").forGetter(FrozenRecord::pitch)
                ).apply(instance, FrozenRecord::new)
        );
    }
}