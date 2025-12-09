package com.obscamera.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.obscamera.OBSCameraSwitchingMod;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;

public class OBSWebSocketClient extends WebSocketClient {

    private final String password;
    private final Runnable onConnected;
    private final Runnable onDisconnected;
    private boolean isAuthenticated = false;

    public OBSWebSocketClient(String uri, String password, Runnable onConnected, Runnable onDisconnected) {
        super(URI.create(uri));
        this.password = password;
        this.onConnected = onConnected;
        this.onDisconnected = onDisconnected;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        OBSCameraSwitchingMod.LOGGER.info("WebSocket opened");
    }

    @Override
    public void onMessage(String message) {
        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();

            // Hello message（認証チャレンジ）
            if (json.has("op") && json.get("op").getAsInt() == 0) {
                handleHello(json);
            }
            // Identified message（認証成功）
            else if (json.has("op") && json.get("op").getAsInt() == 2) {
                isAuthenticated = true;
                if (onConnected != null) {
                    onConnected.run();
                }
            }
            // Response message
            else if (json.has("op") && json.get("op").getAsInt() == 7) {
                // レスポンス処理（必要に応じて）
            }

        } catch (Exception e) {
            OBSCameraSwitchingMod.LOGGER.error("Error processing message", e);
        }
    }

    private void handleHello(JsonObject helloMessage) {
        try {
            JsonObject d = helloMessage.getAsJsonObject("d");

            // 認証が必要ない場合
            if (!d.has("authentication")) {
                // Identify (op=1)
                JsonObject identify = new JsonObject();
                identify.addProperty("op", 1);
                JsonObject identifyData = new JsonObject();
                identifyData.addProperty("rpcVersion", 1);
                identify.add("d", identifyData);
                send(identify.toString());
                return;
            }

            // 認証が必要な場合
            JsonObject auth = d.getAsJsonObject("authentication");
            String challenge = auth.get("challenge").getAsString();
            String salt = auth.get("salt").getAsString();

            // パスワードハッシュを計算
            String secret = generateAuthString(password, salt, challenge);

            // Identify with auth (op=1)
            JsonObject identify = new JsonObject();
            identify.addProperty("op", 1);
            JsonObject identifyData = new JsonObject();
            identifyData.addProperty("rpcVersion", 1);
            identifyData.addProperty("authentication", secret);
            identify.add("d", identifyData);
            send(identify.toString());

        } catch (Exception e) {
            OBSCameraSwitchingMod.LOGGER.error("Authentication error", e);
        }
    }

    private String generateAuthString(String password, String salt, String challenge) throws Exception {
        // secret = base64(sha256(password + salt))
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        String passwordSalt = password + salt;
        byte[] hash1 = sha256.digest(passwordSalt.getBytes(StandardCharsets.UTF_8));
        String secret = Base64.getEncoder().encodeToString(hash1);

        // authString = base64(sha256(secret + challenge))
        String secretChallenge = secret + challenge;
        byte[] hash2 = sha256.digest(secretChallenge.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash2);
    }

    public void setCurrentScene(String sceneName) {
        if (!isAuthenticated) {
            OBSCameraSwitchingMod.LOGGER.warn("Not authenticated yet");
            return;
        }

        try {
            // Request (op=6)
            JsonObject request = new JsonObject();
            request.addProperty("op", 6);

            JsonObject d = new JsonObject();
            d.addProperty("requestType", "SetCurrentProgramScene");
            d.addProperty("requestId", UUID.randomUUID().toString());

            JsonObject requestData = new JsonObject();
            requestData.addProperty("sceneName", sceneName);
            d.add("requestData", requestData);

            request.add("d", d);

            send(request.toString());

        } catch (Exception e) {
            OBSCameraSwitchingMod.LOGGER.error("Error setting scene", e);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        OBSCameraSwitchingMod.LOGGER.info("WebSocket closed: {} - {}", code, reason);
        isAuthenticated = false;
        if (onDisconnected != null) {
            onDisconnected.run();
        }
    }

    @Override
    public void onError(Exception ex) {
        OBSCameraSwitchingMod.LOGGER.error("WebSocket error", ex);
    }

    public boolean isConnected() {
        return isOpen() && isAuthenticated;
    }
}
