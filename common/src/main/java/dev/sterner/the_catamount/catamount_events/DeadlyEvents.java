package dev.sterner.the_catamount.catamount_events;

import dev.sterner.the_catamount.PlatformHelper;
import dev.sterner.the_catamount.data_attachment.CatamountPlayerDataAttachment;
import dev.sterner.the_catamount.entity.CatamountEntity;
import dev.sterner.the_catamount.entity.WindEntity;
import dev.sterner.the_catamount.payload.FogEffectPayload;
import dev.sterner.the_catamount.registry.TCBlocks;
import dev.sterner.the_catamount.registry.TCEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeadlyEvents {

    public static List<CatamountEvent> getAll() {
        List<CatamountEvent> events = new ArrayList<>();

        events.addAll(DangerousEvents.getAll());

        events.add(new BloodParticlesEvent());
        events.add(new BoneHeapEvent());
        events.add(new MultipleWindGustsEvent());
        events.add(new NightManifestationEvent());

        return events;
    }

    public static class BloodParticlesEvent extends CatamountEvent {
        public BloodParticlesEvent() {
            super(EventType.DEADLY, false);
        }

        @Override
        public void execute(ServerPlayer player) {
            BlockPos location = getEventLocation(player);
            ServerLevel level = player.serverLevel();

            level.sendParticles(ParticleTypes.CRIMSON_SPORE,
                    location.getX() + 0.5, location.getY() + 3, location.getZ() + 0.5,
                    30, 1.0, 1.0, 1.0, 0.1);

            level.playSound(null, location, SoundEvents.PLAYER_HURT_SWEET_BERRY_BUSH,
                    SoundSource.AMBIENT, 1.0f, 0.8f);
        }
    }

    public static class BoneHeapEvent extends CatamountEvent {
        public BoneHeapEvent() {
            super(EventType.DEADLY, false);
        }

        @Override
        public void execute(ServerPlayer player) {
            ServerLevel level = player.serverLevel();
            RandomSource random = player.getRandom();

            int count = 1 + random.nextInt(5);

            for (int i = 0; i < count; i++) {
                BlockPos pos = getEventLocation(player);

                while (level.getBlockState(pos).isAir() && pos.getY() > level.getMinBuildHeight()) {
                    pos = pos.below();
                }
                pos = pos.above();

                level.setBlock(pos, TCBlocks.BONE_HEAP.defaultBlockState(), 3);
            }
        }
    }

    public static class MultipleWindGustsEvent extends CatamountEvent {
        public MultipleWindGustsEvent() {
            super(EventType.DEADLY, false);
        }

        @Override
        public boolean canExecute(ServerPlayer player) {
            long dayTime = player.level().getDayTime() % 24000;
            return dayTime >= 13000 && dayTime < 23000;
        }

        @Override
        public void execute(ServerPlayer player) {
            RandomSource random = player.getRandom();
            int gustCount = 3 + random.nextInt(3);
            ServerLevel level = player.serverLevel();

            for (int i = 0; i < gustCount; i++) {
                BlockPos gustPos = player.blockPosition().offset(
                        random.nextInt(16) - 8,
                        random.nextInt(4) - 2,
                        random.nextInt(16) - 8
                );

                WindEntity wind = new WindEntity(TCEntityTypes.WIND, level);
                wind.setPos(gustPos.getX() + 0.5, gustPos.getY(), gustPos.getZ() + 0.5);
                wind.setAggressive(true);
                wind.setTargetPlayer(player.getUUID());

                level.addFreshEntity(wind);
            }

            level.playSound(null, player.blockPosition(), SoundEvents.BREEZE_WIND_CHARGE_BURST.value(),
                    SoundSource.HOSTILE, 1.5f, 0.7f);
        }
    }

    public static class NightManifestationEvent extends CatamountEvent {
        public NightManifestationEvent() {
            super(EventType.DEADLY, true, 3);
        }

        @Override
        public boolean canExecute(ServerPlayer player) {
            long dayTime = player.level().getDayTime() % 24000;
            if (dayTime < 13000 || dayTime >= 23000) return false;

            CatamountPlayerDataAttachment.Data data = CatamountPlayerDataAttachment.getData(player);
            if (data.deathCooldownTimer() > 0 || data.catamountUUID().isPresent()) {
                return false;
            }

            int moonPhase = player.level().getMoonPhase();
            double chance = moonPhase == 0 ? 0.30 : 0.10;

            return player.getRandom().nextDouble() < chance;
        }

        @Override
        public void execute(ServerPlayer player) {
            ServerLevel level = player.serverLevel();
            BlockPos spawnPos = findValidSpawnPos(level, player.blockPosition());

            CatamountEntity catamount = new CatamountEntity(TCEntityTypes.CATAMOUNT, level);
            catamount.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
            catamount.setStage(Math.max(3, CatamountPlayerDataAttachment.getData(player).catamountStage()));
            catamount.setOwnerUUID(Optional.of(player.getUUID()));
            catamount.setFullyManifested(true);

            level.addFreshEntity(catamount);

            CatamountPlayerDataAttachment.Data data = CatamountPlayerDataAttachment.getData(player);
            CatamountPlayerDataAttachment.setData(player,
                    data.withCatamountUUID(Optional.of(catamount.getUUID())));

            applyFogEffect(player, level, spawnPos);

            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    spawnPos.getX() + 0.5, spawnPos.getY() + 1.5, spawnPos.getZ() + 0.5,
                    50, 2.0, 1.0, 2.0, 0.05);

            level.sendParticles(ParticleTypes.LARGE_SMOKE,
                    spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
                    20, 1.0, 0.5, 1.0, 0.02);

            level.playSound(null, spawnPos,
                    SoundEvents.WARDEN_ROAR, SoundSource.HOSTILE, 2.0f, 0.7f);
        }

        private BlockPos findValidSpawnPos(ServerLevel level, BlockPos center) {
            RandomSource random = level.getRandom();

            for (int i = 0; i < 20; i++) {
                BlockPos pos = center.offset(
                        random.nextInt(40) - 20,
                        random.nextInt(6) - 3,
                        random.nextInt(40) - 20
                );

                while (level.getBlockState(pos).isAir() && pos.getY() > level.getMinBuildHeight()) {
                    pos = pos.below();
                }
                pos = pos.above();

                if (level.getMaxLocalRawBrightness(pos) < 7 &&
                        level.noCollision(AABB.ofSize(Vec3.atCenterOf(pos), 2, 3, 2))) {
                    return pos;
                }
            }

            return center.offset(10, 0, 10);
        }

        private void applyFogEffect(ServerPlayer player, ServerLevel level, BlockPos center) {
            List<ServerPlayer> nearbyPlayers = level.getEntitiesOfClass(ServerPlayer.class,
                    AABB.ofSize(Vec3.atCenterOf(center), 64, 64, 64));

            for (ServerPlayer nearbyPlayer : nearbyPlayers) {
                FogEffectPayload payload = new FogEffectPayload(
                        Vec3.atCenterOf(center),
                        64.0f,
                        600
                );
                PlatformHelper.sendPayloadToPlayer(nearbyPlayer, payload);

                nearbyPlayer.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 600, 0, false, false));
            }
        }
    }
}