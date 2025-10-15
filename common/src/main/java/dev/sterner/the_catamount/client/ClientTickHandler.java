package dev.sterner.the_catamount.client;

import dev.sterner.the_catamount.ClientCatamountConfig;

public class ClientTickHandler {

    public static void onClientTick() {
        ClientPaleAnimalTracker.tick();
        ClientCatamountConfig.tick();
    }
}