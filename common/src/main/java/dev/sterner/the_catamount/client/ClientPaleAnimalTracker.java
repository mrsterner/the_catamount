package dev.sterner.the_catamount.client;

import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientPaleAnimalTracker {
    private static final Map<UUID, Long> paleAnimals = new HashMap<>();

    public static void markPale(UUID animalUUID, long endTime) {
        paleAnimals.put(animalUUID, endTime);
    }

    public static void removePale(UUID animalUUID) {
        paleAnimals.remove(animalUUID);
    }

    public static boolean isPale(UUID animalUUID) {
        Long endTime = paleAnimals.get(animalUUID);
        if (endTime == null) return false;

        long currentTime = Minecraft.getInstance().level != null ?
                Minecraft.getInstance().level.getGameTime() : 0;

        if (currentTime >= endTime) {
            paleAnimals.remove(animalUUID);
            return false;
        }

        return true;
    }

    public static void tick() {
        if (Minecraft.getInstance().level == null) return;

        long currentTime = Minecraft.getInstance().level.getGameTime();
        paleAnimals.entrySet().removeIf(entry -> currentTime >= entry.getValue());
    }

    public static void clear() {
        paleAnimals.clear();
    }
}