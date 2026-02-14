package com.obscamera;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class OBSCameraSwitchingModClient implements ClientModInitializer {

    private static KeyBinding switchCameraKey;

    @Override
    public void onInitializeClient() {
        // キーバインド登録（デフォルト: Kキー）
        switchCameraKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.obscameraswitching.switch_camera",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                new KeyBinding.Category(Identifier.of(OBSCameraSwitchingMod.MOD_ID, "general"))));

        // クライアントティックイベントでキー押下を検出
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (switchCameraKey.wasPressed()) {
                if (client.player != null) {
                    // カメラ切り替えリクエストを送信
                    CameraSwitchHandler.sendSwitchRequest(client.player.getName().getString());
                }
            }
        });

        // HUD描画の登録（録画中インジケーター）
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MISC_OVERLAYS,
                Identifier.of(OBSCameraSwitchingMod.MOD_ID, "recording_indicator"),
                RecordingIndicatorRenderer::render
        );

        // クライアントネットワークハンドラー登録
        com.obscamera.network.ClientNetworkHandler.registerClientReceivers();

        // サーバー接続時にOBS接続を試みる
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            com.obscamera.config.ModConfig config = com.obscamera.config.ModConfig.getInstance();
            if (config.isStreamer) {
                OBSCameraSwitchingMod.LOGGER.info("配信者モードが有効です。OBSに接続します...");
                // 少し遅延させて接続
                new Thread(() -> {
                    try {
                        Thread.sleep(2000); // 2秒待機
                        com.obscamera.obs.OBSController.getInstance().connect();
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }).start();
            }
        });

        OBSCameraSwitchingMod.LOGGER.info("OBS Camera Switching Mod Client initialized!");
    }
}
