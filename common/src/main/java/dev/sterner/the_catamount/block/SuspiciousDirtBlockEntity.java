package dev.sterner.the_catamount.block;


import dev.sterner.the_catamount.registry.TCBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Objects;

import static java.lang.Math.max;

public class SuspiciousDirtBlockEntity extends BlockEntity {

    private int brushProgress = 0;
    private long brushResetTime = 0L;
    private long coolDownEndsAtTick = 0L;

    private ItemStack storedItem = ItemStack.EMPTY;
    private Direction hitDirection = null;
    private ResourceKey<LootTable> lootTable = null;
    private long lootTableSeed = 0L;

    public SuspiciousDirtBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntityTypes.SUSPICIOUS_DIRT, pos, state);
    }

    public boolean brush(long startTick, Player player, Direction hitDirection) {
        if (this.hitDirection == null) {
            this.hitDirection = hitDirection;
        }

        this.brushResetTime = startTick + 40L;

        if (startTick >= this.coolDownEndsAtTick && this.level instanceof ServerLevel serverLevel) {
            this.coolDownEndsAtTick = startTick + 10L;
            this.unpackLootTable(player);

            int prevState = this.getCompletionState();
            this.brushProgress++;

            if (this.brushProgress >= 10) {
                this.onBrushingCompleted(player);
                return true;
            } else {
                level.scheduleTick(this.worldPosition, this.getBlockState().getBlock(), 2);
                int newState = this.getCompletionState();
                if (prevState != newState) {
                    BlockState newBlockState = this.getBlockState().setValue(BlockStateProperties.DUSTED, newState);
                    level.setBlock(this.worldPosition, newBlockState, 3);
                }
                return false;
            }
        }
        return false;
    }

    private void unpackLootTable(Player player) {
        if (this.level instanceof ServerLevel serverLevel && this.storedItem.isEmpty()) {
            List<ItemStack> list = List.of(
                    Items.BONE.getDefaultInstance(),
                    Items.ROTTEN_FLESH.getDefaultInstance(),
                    Items.SKELETON_SKULL.getDefaultInstance()
            );

            RandomSource random = serverLevel.getRandom();
            this.storedItem = list.get(random.nextInt(list.size() - 1));
            this.lootTable = null;
            this.setChanged();
        }
    }

    private void onBrushingCompleted(Player player) {
        if (this.level instanceof ServerLevel serverLevel) {
            this.dropLoot(player);
            BlockState currentState = this.getBlockState();
            level.levelEvent(3008, this.worldPosition, Block.getId(currentState));

            Block replacementBlock;
            if (currentState.getBlock() instanceof SuspiciousDirtBlock graveyardBlock) {
                replacementBlock = graveyardBlock.turnsInto;
            } else {
                replacementBlock = Blocks.AIR;
            }

            level.setBlock(this.worldPosition, replacementBlock.defaultBlockState(), 3);
        }
    }

    private void dropLoot(Player player) {
        if (this.level instanceof ServerLevel serverLevel) {
            this.unpackLootTable(player);
            if (!storedItem.isEmpty()) {
                double entityWidth = EntityType.ITEM.getWidth();
                double posOffset = 1.0 - entityWidth;
                double halfWidth = entityWidth / 2.0;
                Direction dropDir = Objects.requireNonNullElse(this.hitDirection, Direction.UP);
                BlockPos dropPos = this.worldPosition.relative(dropDir, 1);

                double x = dropPos.getX() + 0.5 * posOffset + halfWidth;
                double y = dropPos.getY() + 0.5 + (EntityType.ITEM.getHeight() / 2.0f);
                double z = dropPos.getZ() + 0.5 * posOffset + halfWidth;

                ItemEntity itemEntity = new ItemEntity(
                        this.level,
                        x, y, z,
                        storedItem.split(serverLevel.getRandom().nextInt(21) + 10)
                );

                itemEntity.setDeltaMovement(Vec3.ZERO);
                level.addFreshEntity(itemEntity);
                this.storedItem = ItemStack.EMPTY;
            }
        }
    }

    public void resetBrushingState() {
        if (this.level == null) return;

        long gameTime = this.level.getGameTime();

        if (this.brushProgress != 0 && gameTime >= this.brushResetTime) {
            int oldState = this.getCompletionState();
            this.brushProgress = (int) max(0.0, this.brushProgress - 2.0);
            int newState = this.getCompletionState();

            if (oldState != newState) {
                this.level.setBlock(this.worldPosition,
                        this.getBlockState().setValue(BlockStateProperties.DUSTED, newState), 3);
            }

            this.brushResetTime = gameTime + 4L;
        }

        if (this.brushProgress == 0) {
            this.hitDirection = null;
            this.brushResetTime = 0L;
        } else {
            this.level.scheduleTick(this.worldPosition, this.getBlockState().getBlock(), 2);
        }
    }

    private boolean tryLoadLootTable(CompoundTag tag) {
        if (tag.contains("LootTable", 8)) {
            this.lootTable = ResourceKey.create(
                    Registries.LOOT_TABLE,
                    ResourceLocation.parse(tag.getString("LootTable"))
            );
            this.lootTableSeed = tag.getLong("LootTableSeed");
            return true;
        }
        return false;
    }

    private boolean trySaveLootTable(CompoundTag tag) {
        if (this.lootTable == null) return false;

        tag.putString("LootTable", this.lootTable.location().toString());
        if (this.lootTableSeed != 0L) {
            tag.putLong("LootTableSeed", this.lootTableSeed);
        }
        return true;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);

        if (this.hitDirection != null) {
            tag.putInt("hit_direction", this.hitDirection.ordinal());
        }

        if (!this.storedItem.isEmpty()) {
            tag.put("item", this.storedItem.save(registries));
        }

        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (!this.tryLoadLootTable(tag) && tag.contains("item")) {
            this.storedItem = ItemStack.parse(registries, tag.getCompound("item"))
                    .orElse(ItemStack.EMPTY);
        } else {
            this.storedItem = ItemStack.EMPTY;
        }

        if (tag.contains("hit_direction")) {
            this.hitDirection = Direction.values()[tag.getInt("hit_direction")];
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.trySaveLootTable(tag) && !this.storedItem.isEmpty()) {
            tag.put("item", this.storedItem.save(registries));
        }
    }

    public void setLootTable(ResourceKey<LootTable> lootTable, long seed) {
        this.lootTable = lootTable;
        this.lootTableSeed = seed;
    }

    private int getCompletionState() {
        if (this.brushProgress == 0) return 0;
        else if (this.brushProgress < 3) return 1;
        else if (this.brushProgress < 6) return 2;
        else return 3;
    }

    // Getters
    public ItemStack getStoredItem() {
        return storedItem;
    }

    public Direction getHitDirection() {
        return hitDirection;
    }
}