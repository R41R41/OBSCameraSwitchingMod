package com.obscamera.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.obscamera.OBSCameraSwitchingMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ModConfig {

    // 配信者専用設定
    public boolean isStreamer = false;
    public String obsPassword = "";

    // プレイヤー名 → シーン名のマッピング（配信者が管理）
    // 配信者自身のエントリーがデフォルトシーンとなる
    public Map<String, String> playerScenes = new HashMap<>();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(
            FabricLoader.getInstance().getConfigDir().toFile(),
            "obscameraswitching.json");

    private static ModConfig instance;

    public static ModConfig load() {
        if (instance != null) {
            return instance;
        }

        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                instance = GSON.fromJson(reader, ModConfig.class);
                return instance;
            } catch (IOException e) {
                OBSCameraSwitchingMod.LOGGER.error("Failed to load config, using defaults", e);
            }
        }

        // デフォルト設定を作成して保存
        instance = new ModConfig();
        instance.save();
        return instance;
    }

    public void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(this, writer);
            OBSCameraSwitchingMod.LOGGER.info("Config saved to {}", CONFIG_FILE.getAbsolutePath());
        } catch (IOException e) {
            OBSCameraSwitchingMod.LOGGER.error("Failed to save config", e);
        }
    }

    public static ModConfig getInstance() {
        if (instance == null) {
            return load();
        }
        return instance;
    }

    public String getPlayerScene(String playerName) {
        return playerScenes.get(playerName);
    }

    public void setPlayerScene(String playerName, String sceneName) {
        playerScenes.put(playerName, sceneName);
    }

    public void removePlayerScene(String playerName) {
        playerScenes.remove(playerName);
    }

    public Set<String> getPlayerNames() {
        return playerScenes.keySet();
    }

    /**
     * 配信者（自分）のシーン名を取得（デフォルトシーンとして使用）
     */
    public String getStreamerScene(String streamerName) {
        String scene = playerScenes.get(streamerName);
        return scene != null ? scene : "MainScene";
    }
}
