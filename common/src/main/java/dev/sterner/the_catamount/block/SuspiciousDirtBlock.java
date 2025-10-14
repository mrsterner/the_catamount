package dev.sterner.the_catamount.block;

import dev.sterner.the_catamount.registry.TCBlockEntityTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.*;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;

public class SuspiciousDirtBlock extends BaseEntityBlock {

    public static final IntegerProperty DUSTED = BlockStateProperties.DUSTED;

    public static final MapCodec<SuspiciousDirtBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("turns_into")
                            .forGetter(block -> block.turnsInto),
                    BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("brush_sound")
                            .forGetter(block -> block.brushSound),
                    BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("brush_completed_sound")
                            .forGetter(block -> block.brushCompletedSound),
                    propertiesCodec()
            ).apply(instance, SuspiciousDirtBlock::new)
    );

    final Block turnsInto;
    private final SoundEvent brushSound;
    private final SoundEvent brushCompletedSound;

    public SuspiciousDirtBlock(Block turnsInto, SoundEvent brushSound, SoundEvent brushCompletedSound, Properties properties) {
        super(properties);
        this.turnsInto = turnsInto;
        this.brushSound = brushSound;
        this.brushCompletedSound = brushCompletedSound;
        this.registerDefaultState(this.stateDefinition.any().setValue(DUSTED, 0));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return TCBlockEntityTypes.SUSPICIOUS_DIRT.create(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DUSTED);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof SuspiciousDirtBlockEntity brushableBlockEntity) {
            brushableBlockEntity.resetBrushingState();
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(16) == 0) {
            BlockPos below = pos.below();
            if (FallingBlock.isFree(level.getBlockState(below))) {
                double d = pos.getX() + random.nextDouble();
                double e = pos.getY() - 0.05;
                double f = pos.getZ() + random.nextDouble();
                level.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, state), d, e, f, 0.0, 0.0, 0.0);
            }
        }
    }
}
