package dev.sterner.the_catamount.block;

import dev.sterner.the_catamount.data_attachment.CatamountPlayerDataAttachment;
import dev.sterner.the_catamount.registry.TCBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MonstrousHeadBlockEntity extends BlockEntity {
    private static final int PROXIMITY_RADIUS = 48;
    private static final int MAX_PROXIMITY_TIME = 20 * 60 * 3;

    private final Map<UUID, Integer> playerProximityTime = new HashMap<>();
    private boolean hasBeenAwakened = false;

    public MonstrousHeadBlockEntity(BlockPos pos, BlockState blockState) {
        super(TCBlockEntityTypes.MONSTROUS_REMAINS, pos, blockState);
    }


    public static void tick(Level level, BlockPos pos, BlockState state, MonstrousHeadBlockEntity blockEntity) {
        if (level.isClientSide || blockEntity.hasBeenAwakened) return;

        List<ServerPlayer> nearbyPlayers = level.getEntitiesOfClass(
                ServerPlayer.class,
                new AABB(pos).inflate(PROXIMITY_RADIUS)
        );

        for (ServerPlayer player : nearbyPlayers) {
            UUID playerId = player.getUUID();
            int time = blockEntity.playerProximityTime.getOrDefault(playerId, 0) + 1;
            blockEntity.playerProximityTime.put(playerId, time);

            if (time >= MAX_PROXIMITY_TIME) {
                blockEntity.awakenCatamount(player, (ServerLevel) level);
                break;
            }
        }

        blockEntity.playerProximityTime.keySet().removeIf(uuid ->
                nearbyPlayers.stream().noneMatch(p -> p.getUUID().equals(uuid))
        );

        blockEntity.setChanged();
    }

    private void awakenCatamount(ServerPlayer player, ServerLevel level) {
        CatamountPlayerDataAttachment.Data data = CatamountPlayerDataAttachment.getData(player);

        if (data.catamountStage() >= 0) {
            return;
        }

        data = data.withCatamountStage(0).withStageTimer(0);
        CatamountPlayerDataAttachment.setData(player, data);

        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                worldPosition.getX() + 0.5, worldPosition.getY() + 1, worldPosition.getZ() + 0.5,
                50, 1.0, 1.0, 1.0, 0.05);

        level.playSound(null, worldPosition, SoundEvents.WARDEN_EMERGE,
                SoundSource.HOSTILE, 2.0f, 0.5f);

        hasBeenAwakened = true;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("HasBeenAwakened", hasBeenAwakened);

        CompoundTag proximityTag = new CompoundTag();
        playerProximityTime.forEach((uuid, time) ->
                proximityTag.putInt(uuid.toString(), time));
        tag.put("PlayerProximityTime", proximityTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        hasBeenAwakened = tag.getBoolean("HasBeenAwakened");

        CompoundTag proximityTag = tag.getCompound("PlayerProximityTime");
        playerProximityTime.clear();
        for (String key : proximityTag.getAllKeys()) {
            UUID uuid = UUID.fromString(key);
            playerProximityTime.put(uuid, proximityTag.getInt(key));
        }
    }
}