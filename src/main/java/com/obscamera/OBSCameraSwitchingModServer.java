package com.obscamera;

import net.fabricmc.api.DedicatedServerModInitializer;

public class OBSCameraSwitchingModServer implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        OBSCameraSwitchingMod.LOGGER.info("OBS Camera Switching Mod Server initialized!");
    }
}
