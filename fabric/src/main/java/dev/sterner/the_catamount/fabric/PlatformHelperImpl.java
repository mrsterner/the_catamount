package dev.sterner.the_catamount.fabric;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class PlatformHelperImpl {

    public static void sendPayloadToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    public static void sendPayloadToAllPlayers(ServerLevel level, CustomPacketPayload payload) {
        for (ServerPlayer player : level.players()) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static void sendPayloadToTrackingPlayers(ServerLevel level, Entity entity, CustomPacketPayload payload) {
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(entity) < 4096) {
                ServerPlayNetworking.send(player, payload);
            }
        }
    }

    public static void sendPayloadToNearbyPlayers(ServerLevel level, Vec3 pos, double radius, CustomPacketPayload payload) {
        double radiusSq = radius * radius;
        for (ServerPlayer player : level.players()) {
            if (player.position().distanceToSqr(pos) < radiusSq) {
                ServerPlayNetworking.send(player, payload);
            }
        }
    }
}