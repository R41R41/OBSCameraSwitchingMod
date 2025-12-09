package com.obscamera;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OBSCameraSwitchingMod implements ModInitializer {
    public static final String MOD_ID = "obscameraswitching";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("OBS Camera Switching Mod initializing...");

        // ネットワークパケット登録
        com.obscamera.network.NetworkHandler.registerPackets();

        // サーバー側ハンドラー登録
        com.obscamera.network.ServerNetworkHandler.registerServerReceivers();

        LOGGER.info("OBS Camera Switching Mod initialized!");
    }
}
