package dev.sterner.the_catamount.payload;

import dev.sterner.the_catamount.ClientCatamountConfig;
import dev.sterner.the_catamount.TheCatamount;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class EventTriggeredPayload implements CustomPacketPayload {

    private final String eventName;

    public static final Type<EventTriggeredPayload> ID =
            new Type<>(TheCatamount.id("event_triggered"));

    public static final StreamCodec<RegistryFriendlyByteBuf, EventTriggeredPayload> STREAM_CODEC =
            CustomPacketPayload.codec(
                    EventTriggeredPayload::write,
                    EventTriggeredPayload::new
            );

    public EventTriggeredPayload(String eventName) {
        this.eventName = eventName;
    }

    public EventTriggeredPayload(RegistryFriendlyByteBuf buf) {
        this.eventName = buf.readUtf();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(eventName);
    }

    public void handleS2C() {
        Minecraft client = Minecraft.getInstance();

        client.execute(() -> {
            ClientCatamountConfig.addEvent(eventName);
        });
    }
}