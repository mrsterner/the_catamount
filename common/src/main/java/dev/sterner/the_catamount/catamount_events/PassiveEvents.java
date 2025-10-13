package dev.sterner.the_catamount.catamount_events;

import dev.sterner.the_catamount.data_attachment.CatamountPlayerDataAttachment;
import dev.sterner.the_catamount.listener.SoulConversionListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PassiveEvents {

    public static List<CatamountEvent> getAll() {
        return List.of(
                new SummonTheWindEvent(),
                new SoulFireConversionEvent(),
                new FaintSpiritEvent(),
                new BallOfLightEvent(),
                new AnimalStareEvent(),
                new VillagerSweatEvent(),
                new FootstepsEvent(),
                new PaleAnimalsEvent(),
                new ExtraDamageEvent()
        );
    }

    public static class SummonTheWindEvent extends CatamountEvent {
        public SummonTheWindEvent() {
            super(EventType.PASSIVE, false);
        }

        @Override
        public void execute(ServerPlayer player) {
            BlockPos location = getEventLocation(player);
            //TODO Spawn wind entity at location
        }
    }

    public static class SoulFireConversionEvent extends CatamountEvent {
        private static final Map<BlockPos, ConversionRecord> ACTIVE_CONVERSIONS = new HashMap<>();

        public SoulFireConversionEvent() {
            super(EventType.PASSIVE, false);
        }

        @Override
        public void execute(ServerPlayer player) {
            BlockPos center = getEventLocation(player);
            ServerLevel level = player.serverLevel();
            RandomSource random = player.getRandom();

            int duration = (10 + random.nextInt(41)) * 20;
            long endTime = level.getGameTime() + duration;

            boolean foundAny = false;

            for (BlockPos pos : BlockPos.betweenClosed(
                    center.offset(-10, -10, -10),
                    center.offset(10, 10, 10)
            )) {
                BlockState state = level.getBlockState(pos);
                Block block = state.getBlock();

                if (SoulConversionListener.CONVERSION_PAIR.containsKey(block)) {
                    BlockPos immutablePos = pos.immutable();
                    ACTIVE_CONVERSIONS.put(immutablePos, new ConversionRecord(state, endTime));

                    SoulConversionListener.convertBlock(level, immutablePos, block);
                    foundAny = true;
                }
            }

            if (foundAny) {
                level.playSound(null, center, SoundEvents.SOUL_ESCAPE.value(),
                        SoundSource.AMBIENT, 0.8f, 0.8f);
            }
        }

        public static void tickConversions(ServerLevel level) {
            if (ACTIVE_CONVERSIONS.isEmpty()) return;

            long currentTime = level.getGameTime();
            Iterator<Map.Entry<BlockPos, ConversionRecord>> iterator = ACTIVE_CONVERSIONS.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<BlockPos, ConversionRecord> entry = iterator.next();
                BlockPos pos = entry.getKey();
                ConversionRecord record = entry.getValue();

                if (currentTime >= record.endTime) {
                    level.setBlock(pos, record.originalState, 3);

                    RandomSource random = level.getRandom();
                    for (int i = 0; i < 3; i++) {
                        level.sendParticles(ParticleTypes.SMOKE,
                                pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.5,
                                pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 0.5,
                                pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.5,
                                1, 0, 0, 0, 0);
                    }

                    iterator.remove();
                }
            }
        }

        private record ConversionRecord(BlockState originalState, long endTime) {}
    }

    public static class FaintSpiritEvent extends CatamountEvent {
        public FaintSpiritEvent() {
            super(EventType.PASSIVE, false);
        }

        @Override
        public void execute(ServerPlayer player) {
            BlockPos location = getEventLocation(player);
            //TODO Spawn particle effect resembling a face
        }
    }

    public static class BallOfLightEvent extends CatamountEvent {
        public BallOfLightEvent() {
            super(EventType.PASSIVE, false);
        }

        @Override
        public void execute(ServerPlayer player) {
            BlockPos location = getEventLocation(player).above(20);
            // TODO Spawn light orb entity that glides
        }
    }

    public static class AnimalStareEvent extends CatamountEvent {
        public AnimalStareEvent() {
            super(EventType.PASSIVE, false);
        }

        @Override
        public void execute(ServerPlayer player) {
            List<Animal> animals = findNearbyEntities(player, Animal.class, 32.0);

            if (animals.isEmpty()) return;

            float yaw = player.getRandom().nextFloat() * 360f;

            for (Animal animal : animals) {
                animal.setYRot(yaw);
                animal.setYHeadRot(yaw);
                animal.getNavigation().stop();
                //TODO Set a timer to freeze the animal for a few seconds
            }
        }
    }

    public static class VillagerSweatEvent extends CatamountEvent {
        public VillagerSweatEvent() {
            super(EventType.PASSIVE, false);
        }

        @Override
        public void execute(ServerPlayer player) {
            List<Villager> villagers = findNearbyEntities(player, Villager.class, 32.0);

            for (Villager villager : villagers) {
                player.serverLevel().sendParticles(ParticleTypes.SPLASH,
                        villager.getX(), villager.getY() + 2, villager.getZ(),
                        3, 0.2, 0.2, 0.2, 0.01);
            }
        }
    }

    public static class FootstepsEvent extends CatamountEvent {
        public FootstepsEvent() {
            super(EventType.PASSIVE, false);
        }

        @Override
        public void execute(ServerPlayer player) {
            BlockPos nearbyPos = player.blockPosition().offset(
                    player.getRandom().nextInt(10) - 5,
                    0,
                    player.getRandom().nextInt(10) - 5
            );

            BlockState groundBlock = player.serverLevel().getBlockState(nearbyPos.below());
            SoundType soundType = groundBlock.getSoundType();

            player.serverLevel().playSound(null, nearbyPos,
                    soundType.getStepSound(),
                    SoundSource.AMBIENT,
                    0.5f, 1.0f);
        }
    }

    public static class PaleAnimalsEvent extends CatamountEvent {
        public PaleAnimalsEvent() {
            super(EventType.PASSIVE, false);
        }

        @Override
        public void execute(ServerPlayer player) {
            List<Animal> animals = findNearbyEntities(player, Animal.class, 32.0);

            for (Animal animal : animals) {
                // TODO Apply pale visual effect
                animal.hurt(player.damageSources().magic(), 0.0f);
            }
        }
    }

    public static class ExtraDamageEvent extends CatamountEvent {
        public ExtraDamageEvent() {
            super(EventType.PASSIVE, true, 2);
        }

        @Override
        public void execute(ServerPlayer player) {
            CatamountPlayerDataAttachment.Data data = CatamountPlayerDataAttachment.getData(player);
            CatamountPlayerDataAttachment.setData(player, data.withExtraDamageTimer(1200));
        }
    }
}