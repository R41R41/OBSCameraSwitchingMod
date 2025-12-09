# テストガイド

## 1人でローカルテストする方法

システムが正しく動作するか、1台のPCで確認する手順です。

## 準備

### 1. MODのビルド（完了✅）

```bash
cd /home/azureuser/OBSCameraSwitchingMod
./gradlew build
```

生成されたJAR: `build/libs/obscameraswitching-1.0.0.jar`

### 2. Minecraftへのインストール

```bash
# JARファイルをmodsフォルダにコピー
cp build/libs/obscameraswitching-1.0.0.jar ~/.minecraft/mods/
```

### 3. OBSのセットアップ

#### 3-1. obs-websocketの有効化

1. OBS Studio 28.0以降を起動
2. **ツール → WebSocketサーバー設定**
3. ✅ サーバーを有効にする
4. ポート: `4455` (デフォルト)
5. パスワード: `test123` (テスト用)
6. 「適用」をクリック

#### 3-2. テスト用シーンの作成

1. OBSで以下のシーンを作成:
   - `TestScene1`
   - `TestScene2`
   - `TestScene3`

2. 各シーンに適当なソース（テキストや色など）を追加して区別できるようにする

### 4. 中継サーバーのセットアップ

#### 4-1. 依存関係のインストール

```bash
cd /home/azureuser/OBSCameraSwitchingMod/server
npm install
```

#### 4-2. 設定ファイルの編集

`server/src/config.ts`を編集:

```typescript
export const config: Config = {
  server: {
    port: 3000
  },
  obs: {
    url: 'ws://localhost:4455',
    password: 'test123',  // OBSで設定したパスワード
    switchMode: 'scene'
  },
  playerSources: {
    'TestPlayer': 'TestScene1',
    'Player2': 'TestScene2',
    'Player3': 'TestScene3',
  }
};
```

#### 4-3. サーバー起動

```bash
npm run dev
```

成功すると以下のように表示されます:

```
🚀 サーバーが起動しました: http://localhost:3000
📊 ステータス: http://localhost:3000/status
設定されたプレイヤー:
  - TestPlayer → TestScene1
  - Player2 → TestScene2
  - Player3 → TestScene3
OBSに接続中...
✅ OBS WebSocketに接続しました
```

### 5. Minecraftでのテスト

#### 5-1. 設定ファイルの作成

Minecraftを一度起動すると、設定ファイルが自動生成されます。

`.minecraft/config/obscameraswitching.json`を編集:

```json
{
  "serverUrl": "http://localhost:3000",
  "playerName": "TestPlayer"
}
```

**重要**: `playerName`は`server/src/config.ts`の`playerSources`に登録した名前と一致させてください。

#### 5-2. Minecraftを起動

1. Fabric 1.21.4でMinecraftを起動
2. ワールドに入る
3. **Kキー**を押す

#### 5-3. 確認ポイント

✅ **Minecraftのチャット欄**:
```
[OBS] カメラ切り替えリクエスト送信中...
[OBS] カメラを切り替えました！
```

✅ **サーバーのログ**:
```
📹 カメラ切り替えリクエスト: TestPlayer
✅ シーンを切り替えました: TestScene1
```

✅ **OBS**:
- シーンが`TestScene1`に切り替わる

## トラブルシューティング

### ❌ 「サーバーURLが設定されていません」

**原因**: 設定ファイルが正しく読み込めていない

**解決策**:
1. `.minecraft/config/obscameraswitching.json`が存在するか確認
2. JSONの形式が正しいか確認（カンマ、括弧など）

### ❌ 「Connection refused」

**原因**: 中継サーバーが起動していない

**解決策**:
1. `server`フォルダで`npm run dev`を実行
2. `http://localhost:3000/status`にアクセスして確認

### ❌ 「OBS WebSocket接続エラー」

**原因**: OBSのWebSocket設定が正しくない

**解決策**:
1. OBSでWebSocketサーバーが有効になっているか確認
2. パスワードが`config.ts`と一致しているか確認
3. OBSが起動しているか確認

### ❌ 「Player source not found」

**原因**: プレイヤー名がサーバーの設定に登録されていない

**解決策**:
1. `.minecraft/config/obscameraswitching.json`の`playerName`を確認
2. `server/src/config.ts`の`playerSources`に同じ名前があるか確認

### ❌ シーンが切り替わらない

**原因**: OBSにシーンが存在しない

**解決策**:
1. OBSで指定されたシーン名が存在するか確認
2. スペルミス、大文字小文字が一致しているか確認

## 手動テスト（curlコマンド）

サーバーが正しく動作しているか、curlで直接テストできます:

### ステータス確認

```bash
curl http://localhost:3000/status
```

期待される出力:
```json
{
  "obsConnected": true,
  "players": ["TestPlayer", "Player2", "Player3"]
}
```

### カメラ切り替え

```bash
curl -X POST http://localhost:3000/switch \
  -H "Content-Type: application/json" \
  -d '{"playerName":"TestPlayer"}'
```

期待される出力:
```json
{
  "success": true,
  "player": "TestPlayer",
  "source": "TestScene1"
}
```

OBSのシーンが切り替わることを確認してください。

## 次のステップ

テストが成功したら、実際の環境でのセットアップに進みましょう:

1. 各プレイヤーのPCにMODをインストール
2. 各プレイヤーのOBSでNDI出力を設定
3. 配信者のOBSでNDI受信を設定
4. 実際のプレイヤー名で設定を更新

詳細は`SETUP_GUIDE.md`を参照してください。

