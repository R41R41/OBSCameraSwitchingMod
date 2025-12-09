package com.obscamera.network;

import com.obscamera.OBSCameraSwitchingMod;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class NetworkHandler {

    // カメラ切り替えリクエスト（C2S）
    public static final Identifier SWITCH_CAMERA_ID = Identifier.of(OBSCameraSwitchingMod.MOD_ID, "switch_camera");

    // カメラ切り替え通知（S2C）
    public static final Identifier CAMERA_SWITCHED_ID = Identifier.of(OBSCameraSwitchingMod.MOD_ID, "camera_switched");

    // カメラを戻すリクエスト（C2S）
    public static final Identifier RETURN_CAMERA_ID = Identifier.of(OBSCameraSwitchingMod.MOD_ID, "return_camera");

    public static void registerPackets() {
        // C2S packets
        PayloadTypeRegistry.playC2S().register(SwitchCameraPayload.ID, SwitchCameraPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ReturnCameraPayload.ID, ReturnCameraPayload.CODEC);

        // S2C packets
        PayloadTypeRegistry.playS2C().register(CameraSwitchedPayload.ID, CameraSwitchedPayload.CODEC);

        OBSCameraSwitchingMod.LOGGER.info("Network packets registered");
    }

    // カメラ切り替えリクエスト（プレイヤー名のみ送信、シーン名は配信者側で解決）
    public record SwitchCameraPayload(String playerName) implements CustomPayload {
        public static final CustomPayload.Id<SwitchCameraPayload> ID = new CustomPayload.Id<>(SWITCH_CAMERA_ID);

        public static final PacketCodec<RegistryByteBuf, SwitchCameraPayload> CODEC = PacketCodec.tuple(
                PacketCodecs.STRING, SwitchCameraPayload::playerName,
                SwitchCameraPayload::new);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    // カメラ切り替え通知（全員に送信）
    public record CameraSwitchedPayload(String playerName, String sceneName, boolean isReturn)
            implements CustomPayload {
        public static final CustomPayload.Id<CameraSwitchedPayload> ID = new CustomPayload.Id<>(CAMERA_SWITCHED_ID);

        public static final PacketCodec<RegistryByteBuf, CameraSwitchedPayload> CODEC = PacketCodec.tuple(
                PacketCodecs.STRING, CameraSwitchedPayload::playerName,
                PacketCodecs.STRING, CameraSwitchedPayload::sceneName,
                PacketCodecs.BOOLEAN, CameraSwitchedPayload::isReturn,
                CameraSwitchedPayload::new);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    // カメラを戻すリクエスト
    public record ReturnCameraPayload() implements CustomPayload {
        public static final CustomPayload.Id<ReturnCameraPayload> ID = new CustomPayload.Id<>(RETURN_CAMERA_ID);

        public static final PacketCodec<RegistryByteBuf, ReturnCameraPayload> CODEC = PacketCodec
                .unit(new ReturnCameraPayload());

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
}
