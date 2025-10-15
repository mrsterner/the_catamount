package dev.sterner.the_catamount;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class PlatformHelper {

    @ExpectPlatform
    public static void sendPayloadToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void sendPayloadToAllPlayers(ServerLevel level, CustomPacketPayload payload) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void sendPayloadToTrackingPlayers(ServerLevel level, Entity entity, CustomPacketPayload payload) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void sendPayloadToNearbyPlayers(ServerLevel level, Vec3 pos, double radius, CustomPacketPayload payload) {
        throw new AssertionError();
    }
}