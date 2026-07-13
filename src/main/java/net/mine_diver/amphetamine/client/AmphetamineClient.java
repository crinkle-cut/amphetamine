package net.mine_diver.amphetamine.client;

import net.fabricmc.api.ClientModInitializer;
import net.mine_diver.amphetamine.client.render.Shaders;

public class AmphetamineClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Shaders.init();
    }
}
