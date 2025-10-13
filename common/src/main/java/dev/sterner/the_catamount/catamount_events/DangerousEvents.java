package dev.sterner.the_catamount.catamount_events;

import dev.sterner.the_catamount.data_attachment.CatamountPlayerDataAttachment;
import dev.sterner.the_catamount.entity.CatamountEntity;
import dev.sterner.the_catamount.registry.TCEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DangerousEvents {

    public static List<CatamountEvent> getAll() {
        List<CatamountEvent> events = new ArrayList<>();

        events.addAll(DamagingEvents.getAll());

        events.add(new EyesInDarkEvent());
        events.add(new FeedingFrenzyEvent());

        return events;
    }

    public static class EyesInDarkEvent extends CatamountEvent {
        public EyesInDarkEvent() {
            super(EventType.DANGEROUS, true, 2);
        }

        @Override
        public void execute(ServerPlayer player) {
            BlockPos darkSpot = findDarkLocation(player);
            if (darkSpot == null) return;

            player.serverLevel().sendParticles(ParticleTypes.END_ROD,
                    darkSpot.getX() + 0.5, darkSpot.getY() + 1.5, darkSpot.getZ() + 0.5,
                    2, 0.1, 0.1, 0.1, 0.0);

        }

        private BlockPos findDarkLocation(ServerPlayer player) {
            ServerLevel level = player.serverLevel();
            RandomSource random = player.getRandom();

            for (int i = 0; i < 10; i++) {
                BlockPos pos = player.blockPosition().offset(
                        random.nextInt(20) - 10,
                        random.nextInt(6) - 3,
                        random.nextInt(20) - 10
                );

                if (level.getMaxLocalRawBrightness(pos) < 7) {
                    return pos;
                }
            }
            return null;
        }
    }

    public static class FeedingFrenzyEvent extends CatamountEvent {

        private final int HARD_COOLDOWN = 10 * 60 * 20;

        public FeedingFrenzyEvent() {
            super(EventType.DANGEROUS, true, 1);
        }

        @Override
        public boolean canExecute(ServerPlayer player) {
            CatamountPlayerDataAttachment.Data data = CatamountPlayerDataAttachment.getData(player);
            return data.deathCooldownTimer() <= 0
                    && data.catamountUUID().isEmpty()
                    && data.getFeedingFrenzyCooldown() <= 0;
        }

        @Override
        public void execute(ServerPlayer player) {
            ServerLevel level = player.serverLevel();
            BlockPos spawnPos = findValidSpawnPos(level, player.blockPosition());
            var data = CatamountPlayerDataAttachment.getData(player);

            CatamountEntity catamount = new CatamountEntity(TCEntityTypes.CATAMOUNT, level);
            catamount.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
            catamount.setStage(data.catamountStage());
            catamount.setOwnerUUID(Optional.of(player.getUUID()));
            catamount.setFeedingFrenzy(true);
            catamount.setFrenzyKillsRemaining(5);

            level.addFreshEntity(catamount);

            makeEntitiesPanic(player, 32.0);

            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    spawnPos.getX() + 0.5, spawnPos.getY() + 1, spawnPos.getZ() + 0.5,
                    30, 1.0, 1.0, 1.0, 0.05);

            level.playSound(null, spawnPos, SoundEvents.WARDEN_EMERGE,
                    SoundSource.HOSTILE, 2.0f, 0.7f);

            data = data.withFeedingFrenzyCooldown(HARD_COOLDOWN);
            CatamountPlayerDataAttachment.setData(player, data);
        }

        private BlockPos findValidSpawnPos(ServerLevel level, BlockPos center) {
            RandomSource random = level.getRandom();

            for (int i = 0; i < 20; i++) {
                BlockPos pos = center.offset(
                        random.nextInt(20) - 10,
                        random.nextInt(6) - 3,
                        random.nextInt(20) - 10
                );

                while (level.getBlockState(pos).isAir() && pos.getY() > level.getMinBuildHeight()) {
                    pos = pos.below();
                }
                pos = pos.above();

                if (level.noCollision(AABB.ofSize(Vec3.atCenterOf(pos), 1, 2, 1))) {
                    return pos;
                }
            }

            return center;
        }

        private void makeEntitiesPanic(ServerPlayer player, double radius) {
            List<PathfinderMob> mobs = player.level().getEntitiesOfClass(
                    PathfinderMob.class,
                    player.getBoundingBox().inflate(radius)
            );

            for (PathfinderMob mob : mobs) {
                if (mob instanceof Animal || mob instanceof Villager) {
                    mob.getNavigation().stop();
                }
            }
        }
    }
}
