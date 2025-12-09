package com.obscamera;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class CameraSwitchHandler {

	public static void sendSwitchRequest(String playerName) {
		// 既にアクティブなら、配信者に戻す
		if (ActiveStateManager.isActive()) {
			sendReturnToStreamerRequest();
			return;
		}

		sendMessage("§a[OBS] カメラ切り替えリクエスト送信中...");

		// プレイヤー名のみ送信（シーン名は配信者側で解決）
		net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
				new com.obscamera.network.NetworkHandler.SwitchCameraPayload(playerName));

		OBSCameraSwitchingMod.LOGGER.info("Camera switch request sent for player: {}", playerName);
	}

	private static void sendReturnToStreamerRequest() {
		sendMessage("§e[OBS] 配信者に戻します...");

		// Fabricネットワーキングでサーバーに送信
		net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
				new com.obscamera.network.NetworkHandler.ReturnCameraPayload());

		ActiveStateManager.setInactive();
		OBSCameraSwitchingMod.LOGGER.info("Return to streamer request sent");
	}

	public static void sendMessage(String message) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player != null) {
			client.player.sendMessage(Text.literal(message), false);
		}
	}
}
