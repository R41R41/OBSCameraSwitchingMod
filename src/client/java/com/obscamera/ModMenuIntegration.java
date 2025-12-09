package com.obscamera;

import com.obscamera.config.ModConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ModConfig config = ModConfig.getInstance();
            MinecraftClient client = MinecraftClient.getInstance();
            String myName = client.player != null ? client.player.getName().getString() : "Player";

            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.literal("OBS Camera Switching 設定"))
                    .setSavingRunnable(config::save);

            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            // ===== 使い方タブ（全員表示）=====
            ConfigCategory guide = builder.getOrCreateCategory(Text.literal("使い方"));

            guide.addEntry(entryBuilder.startTextDescription(
                    Text.literal("§6§l【OBS Camera Switching MOD】")).build());
            guide.addEntry(entryBuilder.startTextDescription(
                    Text.literal("")).build());
            guide.addEntry(entryBuilder.startTextDescription(
                    Text.literal("§e配信中にKキーを押すだけで")).build());
            guide.addEntry(entryBuilder.startTextDescription(
                    Text.literal("§e自分の画面に配信を切り替えられます！")).build());
            guide.addEntry(entryBuilder.startTextDescription(
                    Text.literal("")).build());

            // プレイヤー向け
            guide.addEntry(entryBuilder.startTextDescription(
                    Text.literal("§b§l━━━ プレイヤー向け ━━━")).build());
            guide.addEntry(entryBuilder.startTextDescription(
                    Text.literal("")).build());
            guide.addEntry(entryBuilder.startTextDescription(
                    Text.literal("§f1. VDO.Ninjaでマイクラ画面を配信者に送る")).build());
            guide.addEntry(entryBuilder.startTextDescription(
                    Text.literal("§7   - OBSで仮想カメラを開始")).build());
            guide.addEntry(entryBuilder.startTextDescription(
                    Text.literal("§7   - vdo.ninja でカメラ配信開始")).build());
            guide.addEntry(entryBuilder.startTextDescription(
                    Text.literal("§7   - リンクを配信者に送る")).build());
            guide.addEntry(entryBuilder.startTextDescription(
                    Text.literal("")).build());
            guide.addEntry(entryBuilder.startTextDescription(
                    Text.literal("§f2. ゲーム中")).build());
            guide.addEntry(entryBuilder.startTextDescription(
                    Text.literal("§a   Kキー §7→ 自分の画面に切り替え")).build());
            guide.addEntry(entryBuilder.startTextDescription(
                    Text.literal("§e   もう一度Kキー §7→ 配信者に戻す")).build());
            guide.addEntry(entryBuilder.startTextDescription(
                    Text.literal("")).build());
            guide.addEntry(entryBuilder.startTextDescription(
                    Text.literal("§f3. 設定不要！")).build());
            guide.addEntry(entryBuilder.startTextDescription(
                    Text.literal("§7   配信者モードはOFFのままでOK")).build());
            guide.addEntry(entryBuilder.startTextDescription(
                    Text.literal("")).build());

            // 配信者向け
            guide.addEntry(entryBuilder.startTextDescription(
                    Text.literal("§d§l━━━ 配信者向け ━━━")).build());
            guide.addEntry(entryBuilder.startTextDescription(
                    Text.literal("")).build());
            guide.addEntry(entryBuilder.startTextDescription(
                    Text.literal("§f1. OBSの設定")).build());
            guide.addEntry(entryBuilder.startTextDescription(
                    Text.literal("§7   - ツール→WebSocketサーバー設定")).build());
            guide.addEntry(entryBuilder.startTextDescription(
                    Text.literal("§7   - サーバーを有効化、パスワード設定")).build());
            guide.addEntry(entryBuilder.startTextDescription(
                    Text.literal("")).build());
            guide.addEntry(entryBuilder.startTextDescription(
                    Text.literal("§f2. OBSでシーンを作成")).build());
            guide.addEntry(entryBuilder.startTextDescription(
                    Text.literal("§7   - 自分用: MainScene等")).build());
            guide.addEntry(entryBuilder.startTextDescription(
                    Text.literal("§7   - 各プレイヤー用: PlayerA_Scene等")).build());
            guide.addEntry(entryBuilder.startTextDescription(
                    Text.literal("§7   - VDO.Ninjaのリンクをブラウザソースで追加")).build());
            guide.addEntry(entryBuilder.startTextDescription(
                    Text.literal("")).build());
            guide.addEntry(entryBuilder.startTextDescription(
                    Text.literal("§f3. MOD設定")).build());
            guide.addEntry(entryBuilder.startTextDescription(
                    Text.literal("§7   - 配信者モード: ON")).build());
            guide.addEntry(entryBuilder.startTextDescription(
                    Text.literal("§7   - OBSパスワードを入力")).build());
            guide.addEntry(entryBuilder.startTextDescription(
                    Text.literal("§7   - プレイヤーマッピングでシーン名を設定")).build());

            // ===== 設定タブ =====
            ConfigCategory main = builder.getOrCreateCategory(Text.literal("設定"));

            main.addEntry(entryBuilder.startBooleanToggle(Text.literal("配信者モード"), config.isStreamer)
                    .setDefaultValue(false)
                    .setTooltip(
                            Text.literal("配信者の場合はONにしてください"),
                            Text.literal("ONにするとOBSに接続します"),
                            Text.literal("プレイヤーはOFFのままでOK"))
                    .setSaveConsumer(newValue -> {
                        config.isStreamer = newValue;
                        if (newValue) {
                            // 配信者モードON時、自分をマッピングに追加
                            if (config.getPlayerScene(myName) == null) {
                                config.setPlayerScene(myName, "MainScene");
                            }
                            com.obscamera.obs.OBSController.getInstance().connect();
                        } else {
                            com.obscamera.obs.OBSController.getInstance().disconnect();
                        }
                    })
                    .build());

            // 配信者モードONの時のみ追加設定を表示
            if (config.isStreamer) {
                main.addEntry(entryBuilder.startTextDescription(
                        Text.literal("")).build());
                main.addEntry(entryBuilder.startTextDescription(
                        Text.literal("§e【OBS接続設定】")).build());

                main.addEntry(entryBuilder.startStrField(Text.literal("OBSパスワード"), config.obsPassword)
                        .setDefaultValue("")
                        .setTooltip(
                                Text.literal("OBS WebSocketのパスワード"),
                                Text.literal("OBS→ツール→WebSocketサーバー設定で確認"))
                        .setSaveConsumer(newValue -> {
                            config.obsPassword = newValue;
                            com.obscamera.obs.OBSController.getInstance().reconnect();
                        })
                        .build());

                // ステータス表示
                main.addEntry(entryBuilder.startTextDescription(
                        Text.literal("")).build());
                main.addEntry(entryBuilder.startTextDescription(
                        Text.literal("§e【接続状態】")).build());
                main.addEntry(entryBuilder.startTextDescription(
                        Text.literal("OBS: " + (com.obscamera.obs.OBSController.getInstance().isConnected()
                                ? "§a接続中 ✓"
                                : "§c切断 ✗")))
                        .build());
            }

            // ===== プレイヤーマッピングタブ（配信者のみ・編集可能）=====
            if (config.isStreamer) {
                ConfigCategory mapping = builder.getOrCreateCategory(Text.literal("プレイヤーマッピング"));

                mapping.addEntry(entryBuilder.startTextDescription(
                        Text.literal("§e【シーン割り当て】")).build());
                mapping.addEntry(entryBuilder.startTextDescription(
                        Text.literal("§7プレイヤー名とOBSのシーン名を設定")).build());
                mapping.addEntry(entryBuilder.startTextDescription(
                        Text.literal("")).build());

                // 配信者自身のシーン（必ず最初に表示）
                String myScene = config.getPlayerScene(myName);
                if (myScene == null) {
                    myScene = "MainScene";
                    config.setPlayerScene(myName, myScene);
                }

                mapping.addEntry(entryBuilder.startStrField(
                        Text.literal("§a" + myName + " §7(自分)"),
                        myScene)
                        .setDefaultValue("MainScene")
                        .setTooltip(Text.literal("配信者（あなた）のシーン名"),
                                Text.literal("Kキー2度押しで戻るシーン"))
                        .setSaveConsumer(newValue -> config.setPlayerScene(myName, newValue))
                        .build());

                mapping.addEntry(entryBuilder.startTextDescription(
                        Text.literal("")).build());
                mapping.addEntry(entryBuilder.startTextDescription(
                        Text.literal("§e【他のプレイヤー】")).build());

                // 他のプレイヤーのマッピング（編集可能）
                List<String> otherPlayers = new ArrayList<>();
                for (String playerName : config.getPlayerNames()) {
                    if (!playerName.equals(myName)) {
                        otherPlayers.add(playerName);
                    }
                }

                if (otherPlayers.isEmpty()) {
                    mapping.addEntry(entryBuilder.startTextDescription(
                            Text.literal("§7まだ他のプレイヤーがいません")).build());
                    mapping.addEntry(entryBuilder.startTextDescription(
                            Text.literal("§7誰かがKキーを押すと追加されます")).build());
                } else {
                    for (String playerName : otherPlayers) {
                        String sceneName = config.getPlayerScene(playerName);
                        mapping.addEntry(entryBuilder.startStrField(
                                Text.literal("§f" + playerName),
                                sceneName != null ? sceneName : playerName + "_Scene")
                                .setDefaultValue(playerName + "_Scene")
                                .setTooltip(Text.literal(playerName + " のシーン名"))
                                .setSaveConsumer(newValue -> config.setPlayerScene(playerName, newValue))
                                .build());
                    }
                }

                // 新しいプレイヤー追加用の説明
                mapping.addEntry(entryBuilder.startTextDescription(
                        Text.literal("")).build());
                mapping.addEntry(entryBuilder.startTextDescription(
                        Text.literal("§7※新しいプレイヤーがKキーを押すと")).build());
                mapping.addEntry(entryBuilder.startTextDescription(
                        Text.literal("§7自動的にここに追加されます")).build());
            }

            return builder.build();
        };
    }
}
