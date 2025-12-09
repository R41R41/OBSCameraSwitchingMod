package com.obscamera.obs;

import com.obscamera.CameraSwitchHandler;
import com.obscamera.OBSCameraSwitchingMod;
import com.obscamera.config.ModConfig;
import com.obscamera.server.OBSWebSocketClient;

public class OBSController {

    private static OBSController instance;
    private OBSWebSocketClient websocketClient;

    private OBSController() {
    }

    public static OBSController getInstance() {
        if (instance == null) {
            instance = new OBSController();
        }
        return instance;
    }

    public void connect() {
        ModConfig config = ModConfig.getInstance();

        if (!config.isStreamer) {
            OBSCameraSwitchingMod.LOGGER.info("配信者モードではないため、OBS接続をスキップします");
            return;
        }

        // 既に接続中なら何もしない
        if (websocketClient != null && websocketClient.isConnected()) {
            OBSCameraSwitchingMod.LOGGER.info("既にOBSに接続済みです");
            return;
        }

        try {
            String password = config.obsPassword != null ? config.obsPassword : "";

            OBSCameraSwitchingMod.LOGGER.info("OBSに接続中... (パスワード: {})", password.isEmpty() ? "なし" : "設定済み");
            CameraSwitchHandler.sendMessage("§e[OBS] OBSに接続中...");

            websocketClient = new OBSWebSocketClient("ws://localhost:4455", password, () -> {
                OBSCameraSwitchingMod.LOGGER.info("✅ OBS WebSocketに接続しました");
                CameraSwitchHandler.sendMessage("§a[OBS] OBSに接続しました");
            }, () -> {
                OBSCameraSwitchingMod.LOGGER.warn("⚠️ OBS WebSocketから切断されました");
                CameraSwitchHandler.sendMessage("§e[OBS] OBSから切断されました。再接続します...");
                // 再接続
                new Thread(() -> {
                    try {
                        Thread.sleep(5000);
                        connect();
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }).start();
            });

            websocketClient.connect();

        } catch (Exception e) {
            OBSCameraSwitchingMod.LOGGER.error("❌ OBS WebSocket接続エラー:", e);
            CameraSwitchHandler.sendMessage("§c[OBS] OBS接続エラー: " + e.getMessage());
        }
    }

    public void switchToScene(String sceneName) {
        if (websocketClient != null && websocketClient.isConnected()) {
            websocketClient.setCurrentScene(sceneName);
            OBSCameraSwitchingMod.LOGGER.info("✅ シーンを切り替えました: {}", sceneName);
        } else {
            OBSCameraSwitchingMod.LOGGER.warn("⚠️ OBSに接続されていません");
        }
    }

    public void disconnect() {
        if (websocketClient != null) {
            websocketClient.close();
            websocketClient = null;
        }
    }

    public void reconnect() {
        disconnect();
        new Thread(() -> {
            try {
                Thread.sleep(500);
                connect();
            } catch (InterruptedException e) {
                // ignore
            }
        }).start();
    }

    public boolean isConnected() {
        return websocketClient != null && websocketClient.isConnected();
    }
}
