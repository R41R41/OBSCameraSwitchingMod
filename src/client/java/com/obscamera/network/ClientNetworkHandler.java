package com.obscamera.network;

import com.obscamera.ActiveStateManager;
import com.obscamera.CameraSwitchHandler;
import com.obscamera.OBSCameraSwitchingMod;
import com.obscamera.config.ModConfig;
import com.obscamera.obs.OBSController;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

public class ClientNetworkHandler {

    public static void registerClientReceivers() {
        // カメラ切り替え通知受信
        ClientPlayNetworking.registerGlobalReceiver(
                NetworkHandler.CameraSwitchedPayload.ID,
                (payload, context) -> {
                    ModConfig config = ModConfig.getInstance();
                    String playerName = payload.playerName();
                    boolean isReturn = payload.isReturn();

                    context.client().execute(() -> {
                        String myName = context.player() != null ? context.player().getName().getString() : "";
                        String sceneName = payload.sceneName();

                        if (isReturn) {
                            // デフォルトシーン（配信者自身のシーン）に戻す
                            if (config.isStreamer) {
                                // 配信者自身のシーン名を取得
                                String streamerScene = config.getStreamerScene(myName);
                                OBSCameraSwitchingMod.LOGGER.info("🔄 配信者のシーンに戻します: {}", streamerScene);
                                OBSController.getInstance().switchToScene(streamerScene);
                            }
                            ActiveStateManager.setInactive();
                            CameraSwitchHandler.sendMessage("§e[OBS] 配信者に戻りました");
                        } else {
                            // プレイヤーのシーンに切り替え
                            if (config.isStreamer) {
                                // マッピングからシーン名を取得（なければプレイヤー名をそのまま使用）
                                String targetScene = config.getPlayerScene(playerName);
                                if (targetScene == null || targetScene.isEmpty()) {
                                    targetScene = playerName + "_Scene";
                                    // 新しいプレイヤーをマッピングに追加
                                    config.setPlayerScene(playerName, targetScene);
                                    config.save();
                                }

                                OBSCameraSwitchingMod.LOGGER.info("📹 シーン切り替え: {} → {}", playerName, targetScene);
                                OBSController.getInstance().switchToScene(targetScene);
                            }

                            // 自分自身なら表示を出す
                            if (myName.equals(playerName)) {
                                ActiveStateManager.setActive();
                                CameraSwitchHandler.sendMessage("§a[OBS] カメラを切り替えました！");
                            } else {
                                // 他の人が切り替わった
                                ActiveStateManager.setInactive();
                                CameraSwitchHandler.sendMessage("§e[OBS] " + playerName + " に切り替わりました");
                            }
                        }
                    });
                });

        OBSCameraSwitchingMod.LOGGER.info("Client network handlers registered");
    }

    /**
     * 配信者自身をマッピングに追加（初期化時に呼び出す）
     */
    public static void ensureStreamerInMapping() {
        ModConfig config = ModConfig.getInstance();
        if (config.isStreamer) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                String myName = client.player.getName().getString();
                if (config.getPlayerScene(myName) == null) {
                    config.setPlayerScene(myName, "MainScene");
                    config.save();
                    OBSCameraSwitchingMod.LOGGER.info("配信者をマッピングに追加: {} → MainScene", myName);
                }
            }
        }
    }
}
