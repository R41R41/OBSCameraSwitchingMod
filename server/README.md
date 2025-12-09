# OBS Camera Switching Server

Minecraftの各プレイヤーからのカメラ切り替えリクエストを受け取り、OBS WebSocketを通じて配信者のOBSを制御する中継サーバーです。

## セットアップ

### 1. 依存関係のインストール

```bash
cd server
npm install
```

### 2. 設定ファイルの編集

`src/config.ts`を編集して、以下を設定します：

```typescript
export const config: Config = {
  server: {
    port: 3000  // サーバーのポート番号
  },
  obs: {
    url: 'ws://localhost:4455',  // OBS WebSocketのURL
    password: 'your_password',    // OBS WebSocketのパスワード
    switchMode: 'scene'           // 'scene' or 'source'
  },
  playerSources: {
    'PlayerA': 'PlayerA_Scene',   // プレイヤー名 → OBSのシーン/ソース名
    'PlayerB': 'PlayerB_Scene',
    'PlayerC': 'PlayerC_Scene',
  }
};
```

### 3. OBS WebSocketの設定

配信者のOBSで以下を設定します：

1. **obs-websocketプラグインのインストール**
   - OBS Studio 28.0以降には標準搭載
   - それ以前のバージョンは手動インストールが必要

2. **WebSocketサーバーの有効化**
   - OBS → ツール → WebSocketサーバー設定
   - サーバーを有効化
   - ポート: 4455（デフォルト）
   - パスワードを設定

3. **シーン/ソースの設定**

   **方法A: シーン切り替えモード（推奨・簡単）**
   - 各プレイヤー用のシーンを作成
   - 例: `PlayerA_Scene`, `PlayerB_Scene`, `PlayerC_Scene`
   - 各シーンにそれぞれのプレイヤーの映像ソースを配置

   **方法B: ソース切り替えモード（1シーン内で切り替え）**
   - 1つのメインシーン内に全プレイヤーのソースを配置
   - ソース名を設定（例: `PlayerA_Source`, `PlayerB_Source`）
   - サーバーが自動でソースの表示/非表示を切り替え

## 各プレイヤーの映像取り込み方法

各プレイヤーがローカルでOBSでウィンドウキャプチャしている場合、配信者のOBSに映像を送る方法：

### 方法1: NDI (推奨)
- 各プレイヤー: OBS NDIプラグインをインストール → NDI出力を有効化
- 配信者: NDIソースとして各プレイヤーの映像を受信

### 方法2: OBS VirtualCam + 画面共有
- 各プレイヤー: OBS VirtualCamを起動 → Discord/Zoom等で共有
- 配信者: ウィンドウキャプチャで受信

### 方法3: RTMP配信
- 各プレイヤー: ローカルRTMPサーバーに配信
- 配信者: メディアソースでRTMPストリームを受信

## 起動方法

### 開発モード（TypeScript直接実行）
```bash
npm run dev
```

### 本番ビルド
```bash
npm run build
npm start
```

## API仕様

### POST /switch
カメラを切り替えます。

**リクエスト:**
```json
{
  "playerName": "PlayerA"
}
```

**レスポンス（成功時）:**
```json
{
  "success": true,
  "player": "PlayerA",
  "source": "PlayerA_Scene"
}
```

### GET /status
サーバーの状態を確認します。

**レスポンス:**
```json
{
  "obsConnected": true,
  "players": ["PlayerA", "PlayerB", "PlayerC"]
}
```

## トラブルシューティング

### OBSに接続できない
- OBS WebSocketが有効になっているか確認
- ポート番号が正しいか確認（デフォルト: 4455）
- パスワードが正しいか確認
- ファイアウォール設定を確認

### プレイヤーが見つからない
- `config.ts`の`playerSources`にプレイヤー名が登録されているか確認
- プレイヤー名が完全一致しているか確認（大文字小文字も区別）

### シーン/ソースが見つからない
- OBS側でシーン/ソース名が正しく設定されているか確認
- スペルミスがないか確認

## ライセンス

CC0-1.0

