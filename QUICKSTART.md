# クイックスタート

最速でセットアップして試してみるガイドです。

## ⚡ 5分でセットアップ

### 1. MODをビルド（1分）

```bash
cd /home/azureuser/OBSCameraSwitchingMod
./gradlew build
```

### 2. サーバーをセットアップ（2分）

```bash
cd server
npm install
```

`server/src/config.ts`を編集:
```typescript
playerSources: {
  'あなたのマイクラ名': 'OBSのシーン名',
}
```

### 3. 起動（1分）

**サーバー起動:**
```bash
cd server
npm run dev
```

**OBS設定:**
1. ツール → WebSocketサーバー設定 → 有効化
2. パスワードをメモ
3. `config.ts`にパスワードを記入

**Minecraft:**
1. `build/libs/obscameraswitching-1.0.0.jar`を`.minecraft/mods/`にコピー
2. Minecraft起動
3. `.minecraft/config/obscameraswitching.json`を編集:
   ```json
   {
     "serverUrl": "http://localhost:3000",
     "playerName": "あなたのマイクラ名"
   }
   ```

### 4. テスト（1分）

1. Minecraftでゲーム開始
2. **Kキー**を押す
3. OBSのシーンが切り替わる！✨

## 💡 テスト用の最小構成

**1人でテストする場合:**
- 自分のPC: Minecraft + MOD + サーバー + OBS
- OBSに自分用のシーンを1つ作成
- Kキーでシーン切り替えを確認

**複数人でテストする場合:**
- 各プレイヤー: Minecraft + MOD + OBS（NDI出力）
- 配信者: サーバー + OBS（NDI受信 + WebSocket）

## 🎬 動作イメージ

```
[プレイヤーAがKキーを押す]
         ↓
チャット「カメラ切り替えリクエスト送信中...」
         ↓
サーバー「📹 カメラ切り替えリクエスト: PlayerA」
         ↓
OBS「✅ シーンを切り替えました: PlayerA_Scene」
         ↓
配信画面がPlayerAに切り替わる！🎉
```

## 問題が起きたら

- **「サーバーURLが設定されていません」** 
  → config.jsonを確認

- **「Connection refused」** 
  → サーバーが起動しているか確認

- **「OBS WebSocket接続エラー」** 
  → OBSのWebSocket設定を確認

詳細は`SETUP_GUIDE.md`を参照してください。

