package dev.sterner.the_catamount.catamount_events;

import dev.sterner.the_catamount.data_attachment.CatamountPlayerDataAttachment;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

import java.util.List;

public abstract class CatamountEvent {

    private final EventType type;
    private final boolean countsTowardsPoints;
    private final int weight;

    public CatamountEvent(EventType type, boolean countsTowardsPoints, int weight) {
        this.type = type;
        this.countsTowardsPoints = countsTowardsPoints;
        this.weight = weight;
    }

    public CatamountEvent(EventType type, boolean countsTowardsPoints) {
        this(type, countsTowardsPoints, 1);
    }

    public EventType getType() {
        return type;
    }

    public boolean countsTowardsPoints() {
        return countsTowardsPoints;
    }

    public int getWeight() {
        return weight;
    }

    public abstract void execute(ServerPlayer player);

    public boolean canExecute(ServerPlayer player) {
        return true;
    }

    protected void addPoints(ServerPlayer player, int points) {
        CatamountPlayerDataAttachment.Data data = CatamountPlayerDataAttachment.getData(player);
        CatamountPlayerDataAttachment.setData(player, data.addPoints(points));
    }

    protected BlockPos getEventLocation(ServerPlayer player) {
        RandomSource random = player.getRandom();

        return player.blockPosition();
        /*TODO add random when we know this works
        if (random.nextBoolean()) {
            return player.blockPosition().offset(
                    random.nextInt(32) - 16,
                    random.nextInt(16) - 8,
                    random.nextInt(32) - 16
            );
        } else {
            BlockPos spawnPoint = player.getRespawnPosition();
            if (spawnPoint == null) {
                spawnPoint = player.serverLevel().getSharedSpawnPos();
            }
            return spawnPoint.offset(
                    random.nextInt(32) - 16,
                    random.nextInt(16) - 8,
                    random.nextInt(32) - 16
            );
        }

         */
    }

    protected <T extends Entity> List<T> findNearbyEntities(ServerPlayer player, Class<T> entityClass, double radius) {
        return player.level().getEntitiesOfClass(entityClass,
                player.getBoundingBox().inflate(radius));
    }
}