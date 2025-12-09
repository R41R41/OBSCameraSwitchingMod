# セットアップガイド

このガイドでは、OBS Camera Switching Modシステム全体のセットアップ方法を説明します。

## 📋 システム全体の構成

```
[プレイヤーA PC]                [プレイヤーB PC]                [配信者 PC]
  ↓                               ↓                               ↓
Minecraft + MOD                 Minecraft + MOD                 OBS Studio
  ↓                               ↓                            + obs-websocket
OBS (ウィンドウキャプチャ)      OBS (ウィンドウキャプチャ)    + 各プレイヤー映像
  ↓                               ↓                               ↑
NDI出力 ────────────────────────────────────────────────────────┘
  ↓                               ↓
HTTP Request ──────────┐   HTTP Request
                        ↓           ↓
                   [中継サーバー]
                        ↓
                   OBS WebSocket API
                        ↓
                   [配信者のOBS]
```

## 🎯 セットアップ手順

### ステップ1: 各プレイヤーのPC（全員）

#### 1-1. MODのインストール

1. ビルド:
   ```bash
   cd /home/azureuser/OBSCameraSwitchingMod
   ./gradlew build
   ```

2. 生成された`build/libs/obscameraswitching-1.0.0.jar`を各プレイヤーの`.minecraft/mods/`フォルダにコピー

3. Minecraftを起動（Fabric 1.21.4）

#### 1-2. 設定ファイルの編集

`.minecraft/config/obscameraswitching.json`を編集:

```json
{
  "serverUrl": "http://192.168.1.100:3000",
  "playerName": "YourPlayerName"
}
```

- `serverUrl`: 中継サーバーのURL（配信者のPCのIPアドレス）
- `playerName`: 自分のマイクラプレイヤー名

#### 1-3. OBSのセットアップ（各プレイヤー）

1. OBS Studioをインストール
2. ソースを追加 → ウィンドウキャプチャ → Minecraftを選択
3. **NDIプラグインをインストール**:
   - https://github.com/obs-ndi/obs-ndi/releases からダウンロード
   - インストール後、OBSを再起動
4. **NDI出力を有効化**:
   - ツール → NDI Output Settings
   - "Main Output"にチェック
   - Output Nameを設定（例: "PlayerA"）

### ステップ2: 配信者のPC

#### 2-1. OBS WebSocketの設定

1. OBS Studio 28.0以降をインストール（WebSocket標準搭載）
2. ツール → WebSocketサーバー設定
   - サーバーを有効化
   - ポート: 4455（デフォルト）
   - パスワードを設定（メモしておく）

#### 2-2. 各プレイヤーの映像を受信

**方法A: NDI経由（推奨）**

1. OBS NDIプラグインをインストール
2. ソースを追加 → NDI Source
3. 各プレイヤーのNDI出力を追加（例: "PlayerA", "PlayerB", "PlayerC"）

**方法B: Discord/Zoom画面共有**

1. 各プレイヤーがDiscord/Zoomで画面共有
2. OBSでウィンドウキャプチャまたはブラウザソースで受信

#### 2-3. シーンの設定

**簡単な方法（シーン切り替えモード）:**

1. 各プレイヤー用のシーンを作成:
   - `PlayerA_Scene`
   - `PlayerB_Scene`
   - `PlayerC_Scene`

2. 各シーンにそれぞれのプレイヤーの映像ソースを配置

**高度な方法（ソース切り替えモード）:**

1. 1つのメインシーンを作成
2. 全プレイヤーの映像ソースを同じシーンに配置
3. ソース名を設定（例: `PlayerA_Source`, `PlayerB_Source`）
4. サーバーの設定で`switchMode: 'source'`に変更

#### 2-4. 中継サーバーのセットアップ

1. Node.jsをインストール（v18以降推奨）

2. サーバーディレクトリに移動:
   ```bash
   cd /home/azureuser/OBSCameraSwitchingMod/server
   ```

3. 依存関係をインストール:
   ```bash
   npm install
   ```

4. 設定ファイルを編集:
   `src/config.ts`を開き、以下を設定:

   ```typescript
   export const config: Config = {
     server: {
       port: 3000
     },
     obs: {
       url: 'ws://localhost:4455',
       password: 'your_obs_password',  // OBSで設定したパスワード
       switchMode: 'scene'  // or 'source'
     },
     playerSources: {
       'PlayerA': 'PlayerA_Scene',  // マイクラ名 → OBSシーン/ソース名
       'PlayerB': 'PlayerB_Scene',
       'PlayerC': 'PlayerC_Scene',
     }
   };
   ```

5. サーバーを起動:
   ```bash
   npm run dev
   ```

   本番環境では:
   ```bash
   npm run build
   npm start
   ```

### ステップ3: 動作確認

1. 配信者がOBSを起動
2. 中継サーバーを起動
3. 各プレイヤーがMinecraftにログイン
4. プレイヤーがゲーム内で`K`キーを押す
5. 配信者のOBSが該当プレイヤーのシーンに切り替わる！

## 🎮 使い方

### プレイヤー側

- Minecraftプレイ中に**Kキー**を押す
- チャット欄に「カメラ切り替えリクエスト送信中...」と表示される
- 成功すると「カメラを切り替えました！」と表示される

### キーバインドの変更

1. Minecraft → 設定 → キー設定
2. 「OBSカメラ切り替え」カテゴリ
3. 「自分のカメラに切り替え」のキーを変更

## ⚙️ トラブルシューティング

### MOD側のエラー

**「サーバーURLが設定されていません」**
- `.minecraft/config/obscameraswitching.json`を確認
- `serverUrl`が正しく設定されているか確認

**「エラー: Connection refused」**
- 中継サーバーが起動しているか確認
- サーバーURLのIPアドレスが正しいか確認
- ファイアウォールでポート3000が開いているか確認

### サーバー側のエラー

**「OBS WebSocket接続エラー」**
- OBSが起動しているか確認
- WebSocketサーバーが有効になっているか確認
- パスワードが正しいか確認

**「Player source not found」**
- `config.ts`の`playerSources`にプレイヤー名が登録されているか確認
- プレイヤー名のスペルが完全一致しているか確認

**「シーン/ソースが見つからない」**
- OBS側でシーン/ソース名が正しく設定されているか確認

### NDI関連

**「NDI Sourceが見つからない」**
- 全PCが同じネットワーク上にあるか確認
- ファイアウォールでNDI通信が許可されているか確認
- NDI出力が有効になっているか確認

## 🔧 カスタマイズ

### 複数人で使う場合

`server/src/config.ts`の`playerSources`に全員を追加:

```typescript
playerSources: {
  'Player1': 'Player1_Scene',
  'Player2': 'Player2_Scene',
  'Player3': 'Player3_Scene',
  'Player4': 'Player4_Scene',
  // 何人でも追加可能
}
```

### 自動復帰機能（オプション）

一定時間後に自動で元のシーンに戻る機能を追加する場合は、
`server/src/index.ts`を編集してタイマー機能を追加できます。

## 📝 ライセンス

CC0-1.0

