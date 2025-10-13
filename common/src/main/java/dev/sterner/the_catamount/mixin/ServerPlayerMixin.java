package dev.sterner.the_catamount.mixin;

import dev.sterner.the_catamount.data_attachment.CatamountPlayerDataAttachment;
import dev.sterner.the_catamount.events.ModEventHandlers;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void the_catamount$tick(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        ModEventHandlers.onServerTickPlayer(player);
    }
}