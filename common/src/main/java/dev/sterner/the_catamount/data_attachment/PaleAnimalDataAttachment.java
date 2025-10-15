package dev.sterner.the_catamount.data_attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.sterner.the_catamount.TheCatamount;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PaleAnimalDataAttachment {

    @ExpectPlatform
    public static Data getData(ServerLevel level) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void setData(ServerLevel level, Data data) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void sync(ServerLevel level, Data data, List<ServerPlayer> players) {
        throw new AssertionError();
    }

    public static void syncToTrackingPlayers(ServerLevel level, Data data, Entity entity) {
        List<ServerPlayer> trackingPlayers = level.getPlayers(
                p -> p.distanceToSqr(entity) < 4096
        );
        sync(level, data, trackingPlayers);
    }

    public static void syncToAll(ServerLevel level, Data data) {
        sync(level, data, level.players());
    }

    public record Data(Map<UUID, Long> paleAnimals) {

        public Data() {
            this(new HashMap<>());
        }

        public Data withPaleAnimal(UUID uuid, long endTime) {
            Map<UUID, Long> newMap = new HashMap<>(paleAnimals);
            newMap.put(uuid, endTime);
            return new Data(newMap);
        }

        public Data withoutPaleAnimal(UUID uuid) {
            Map<UUID, Long> newMap = new HashMap<>(paleAnimals);
            newMap.remove(uuid);
            return new Data(newMap);
        }

        public boolean isPale(UUID uuid, long currentTime) {
            Long endTime = paleAnimals.get(uuid);
            return endTime != null && currentTime < endTime;
        }

        private record SerializedEntry(UUID uuid, long endTime) {
            public static final Codec<SerializedEntry> CODEC = RecordCodecBuilder.create(instance ->
                    instance.group(
                            UUIDUtil.CODEC.fieldOf("uuid").forGetter(SerializedEntry::uuid),
                            Codec.LONG.fieldOf("endTime").forGetter(SerializedEntry::endTime)
                    ).apply(instance, SerializedEntry::new)
            );
        }

        public static final Codec<Data> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        SerializedEntry.CODEC.listOf()
                                .xmap(
                                        list -> {
                                            Map<UUID, Long> map = new HashMap<>();
                                            list.forEach(entry -> map.put(entry.uuid(), entry.endTime()));
                                            return map;
                                        },
                                        map -> map.entrySet().stream()
                                                .map(entry -> new SerializedEntry(entry.getKey(), entry.getValue()))
                                                .toList()
                                )
                                .fieldOf("paleAnimals")
                                .forGetter(Data::paleAnimals)
                ).apply(instance, Data::new)
        );

        public static final ResourceLocation ID = TheCatamount.id("pale_animal_data");
    }
}