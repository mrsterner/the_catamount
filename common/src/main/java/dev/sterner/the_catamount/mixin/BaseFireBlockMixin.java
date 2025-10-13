package dev.sterner.the_catamount.mixin;

import dev.sterner.the_catamount.registry.TCItems;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.SoulFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BaseFireBlock.class)
public class BaseFireBlockMixin {

    @Inject(
            method = "entityInside",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true)
    private void the_catamount$entityInside(BlockState state, Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
        BaseFireBlock fire = (BaseFireBlock) (Object) this;
        if (fire instanceof SoulFireBlock) {

            if (entity instanceof ItemEntity itemEntity) {
                ItemStack stack = itemEntity.getItem();
                if (stack.is(TCItems.WHITE_ASH)) {
                    ci.cancel();
                }

                if (stack.getItem() instanceof BlockItem blockItem) {

                    if (blockItem.getBlock().defaultBlockState().is(BlockTags.LOGS_THAT_BURN)) {

                        stack = new ItemStack(TCItems.WHITE_ASH, stack.getCount());
                        itemEntity.setItem(stack);

                        double dx = (entity.getRandom().nextDouble() - 0.5) * 0.3;
                        double dy = 0.2 + entity.getRandom().nextDouble() * 0.15;
                        double dz = (entity.getRandom().nextDouble() - 0.5) * 0.3;
                        itemEntity.setDeltaMovement(dx, dy, dz);

                        ci.cancel();
                    }
                }
            }
        }
    }
}
