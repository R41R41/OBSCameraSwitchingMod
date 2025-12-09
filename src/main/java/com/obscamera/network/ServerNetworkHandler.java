package com.obscamera.network;

import com.obscamera.OBSCameraSwitchingMod;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public class ServerNetworkHandler {

    public static void registerServerReceivers() {
        // カメラ切り替えリクエスト受信
        ServerPlayNetworking.registerGlobalReceiver(
                NetworkHandler.SwitchCameraPayload.ID,
                (payload, context) -> {
                    String playerName = payload.playerName();
                    OBSCameraSwitchingMod.LOGGER.info("📹 カメラ切り替えリクエスト: {}", playerName);

                    context.server().execute(() -> {
                        // 全プレイヤーに通知を送信（シーン名は配信者側で解決）
                        for (ServerPlayerEntity player : context.server().getPlayerManager().getPlayerList()) {
                            ServerPlayNetworking.send(player,
                                    new NetworkHandler.CameraSwitchedPayload(playerName, "", false));
                        }
                        OBSCameraSwitchingMod.LOGGER.info("✅ 全プレイヤーに通知を送信 ({}人)",
                                context.server().getPlayerManager().getPlayerList().size());
                    });
                });

        // カメラを戻すリクエスト受信
        ServerPlayNetworking.registerGlobalReceiver(
                NetworkHandler.ReturnCameraPayload.ID,
                (payload, context) -> {
                    OBSCameraSwitchingMod.LOGGER.info("🔄 カメラを戻すリクエスト");

                    context.server().execute(() -> {
                        // 全プレイヤーに通知を送信
                        for (ServerPlayerEntity player : context.server().getPlayerManager().getPlayerList()) {
                            ServerPlayNetworking.send(player,
                                    new NetworkHandler.CameraSwitchedPayload("", "", true));
                        }
                        OBSCameraSwitchingMod.LOGGER.info("✅ 全プレイヤーに戻す通知を送信");
                    });
                });

        OBSCameraSwitchingMod.LOGGER.info("Server network handlers registered");
    }
}
