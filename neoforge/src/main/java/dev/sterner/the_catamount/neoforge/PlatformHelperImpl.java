package dev.sterner.the_catamount.neoforge;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public class PlatformHelperImpl {

    public static void sendPayloadToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    public static void sendPayloadToAllPlayers(ServerLevel level, CustomPacketPayload payload) {
        PacketDistributor.sendToAllPlayers(payload);
    }

    public static void sendPayloadToTrackingPlayers(ServerLevel level, Entity entity, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
    }

    public static void sendPayloadToNearbyPlayers(ServerLevel level, Vec3 pos, double radius, CustomPacketPayload payload) {
        for (ServerPlayer player : level.players()) {
            if (player.position().distanceToSqr(pos) < radius * radius) {
                PacketDistributor.sendToPlayer(player, payload);
            }
        }
    }
}