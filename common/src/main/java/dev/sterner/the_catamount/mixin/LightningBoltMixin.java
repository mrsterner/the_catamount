package dev.sterner.the_catamount.mixin;

import dev.sterner.the_catamount.registry.TCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightningBolt.class)
public class LightningBoltMixin {

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LightningBolt;powerLightningRod()V"))
    private void the_catamount$tick(CallbackInfo ci) {
        LightningBolt lightning = (LightningBolt) (Object) this;
        Level level = lightning.level();
        BlockPos pos = lightning.blockPosition();

        for (BlockPos offset : BlockPos.betweenClosed(pos.offset(-1, 0, -1), pos.offset(1, 0, 1))) {
            BlockState nearbyState = level.getBlockState(offset);
            if (nearbyState.is(BlockTags.LOGS_THAT_BURN) || nearbyState.is(BlockTags.LEAVES)) {
                level.setBlockAndUpdate(offset, TCBlocks.WHITE_ASH.defaultBlockState());
            }
        }
    }
}
