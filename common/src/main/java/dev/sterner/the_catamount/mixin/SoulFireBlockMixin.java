package dev.sterner.the_catamount.mixin;

import dev.sterner.the_catamount.TheCatamount;
import dev.sterner.the_catamount.data_attachment.SoulConversionDataAttachment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoulFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoulFireBlock.class)
public abstract class SoulFireBlockMixin {

    @Inject(
            method = "canSurvive",
            at = @At("HEAD"),
            cancellable = true
    )
    private void the_catamount$allowSurvivalDuringConversion(BlockState state, LevelReader level, BlockPos pos,
                                                             CallbackInfoReturnable<Boolean> cir) {
        if (level instanceof ServerLevel serverLevel) {
            SoulConversionDataAttachment.Data data = SoulConversionDataAttachment.getData(serverLevel);

            if (data.activeConversions().containsKey(pos)) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(
            method = "updateShape",
            at = @At("HEAD"),
            cancellable = true
    )
    private void the_catamount$preventRemovalDuringConversion(BlockState state, Direction direction,
                                                              BlockState neighborState, LevelAccessor level,
                                                              BlockPos pos, BlockPos neighborPos,
                                                              CallbackInfoReturnable<BlockState> cir) {
        if (direction == Direction.DOWN && level instanceof ServerLevel serverLevel) {
            SoulConversionDataAttachment.Data data = SoulConversionDataAttachment.getData(serverLevel);

            if (data.activeConversions().containsKey(pos)) {
                cir.setReturnValue(state);
            }
        }
    }
}