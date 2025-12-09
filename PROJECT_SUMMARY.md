# OBS Camera Switching Mod - プロジェクト概要

## 📝 プロジェクト説明

複数人でマイクラをプレイしながら、各プレイヤーがゲーム内でショートカットキー（デフォルト: K）を押すことで、配信者のOBSが自動的に自分の画面に切り替わるシステムです。

## 🏗️ アーキテクチャ

### 3層構成

1. **クライアント層（Fabric MOD）**
   - 各プレイヤーのMinecraft内で動作
   - キー入力を検出
   - HTTP APIでサーバーに通知

2. **サーバー層（Node.js）**
   - HTTP APIサーバー
   - OBS WebSocketクライアント
   - プレイヤー名 → OBSシーン/ソースのマッピング

3. **制御層（OBS）**
   - obs-websocketプラグイン
   - シーン/ソースの自動切り替え
   - 各プレイヤーの映像受信（NDI/ウィンドウキャプチャ等）

## 📂 ファイル構造

```
OBSCameraSwitchingMod/
├── src/main/java/com/obscamera/          # MODのソースコード
│   ├── OBSCameraSwitchingMod.java        # メインクラス
│   ├── OBSCameraSwitchingModClient.java  # クライアント初期化
│   ├── CameraSwitchHandler.java          # HTTP通信処理
│   └── config/
│       └── ModConfig.java                # 設定ファイル管理
│
├── src/main/resources/
│   ├── fabric.mod.json                   # MODメタデータ
│   └── assets/obscameraswitching/lang/
│       ├── en_us.json                    # 英語翻訳
│       └── ja_jp.json                    # 日本語翻訳
│
├── server/                               # 中継サーバー
│   ├── src/
│   │   ├── index.ts                      # サーバーメイン
│   │   └── config.ts                     # サーバー設定
│   ├── package.json                      # npm依存関係
│   └── tsconfig.json                     # TypeScript設定
│
├── build.gradle                          # Gradleビルド設定
├── gradle.properties                     # バージョン情報
├── README.md                             # プロジェクト説明
├── SETUP_GUIDE.md                        # 詳細セットアップ
└── QUICKSTART.md                         # クイックスタート
```

## 🔧 技術スタック

### MOD (クライアント)
- **言語**: Java 21
- **フレームワーク**: Fabric 1.21.4
- **依存**: Fabric API, Fabric Loader
- **HTTP**: Java 11+ HttpClient

### サーバー (中継)
- **言語**: TypeScript
- **ランタイム**: Node.js
- **フレームワーク**: Express
- **ライブラリ**: obs-websocket-js

### OBS (配信)
- **プラグイン**: obs-websocket v5.x
- **映像入力**: NDI / ウィンドウキャプチャ / RTMP

## 🌊 データフロー

```
1. プレイヤーがKキーを押す
   ↓
2. OBSCameraSwitchingModClient がイベント検出
   ↓
3. CameraSwitchHandler が HTTP POSTリクエスト送信
   POST http://server:3000/switch
   Body: {"playerName": "PlayerA"}
   ↓
4. Express サーバーがリクエスト受信
   ↓
5. config.playerSources からOBSシーン名を取得
   ↓
6. obs-websocket-js がOBSにコマンド送信
   SetCurrentProgramScene("PlayerA_Scene")
   ↓
7. OBSがシーンを切り替え
   ↓
8. HTTPレスポンスをMODに返す
   ↓
9. プレイヤーにチャットメッセージ表示
```

## 🎯 主要機能

### MOD側
- ✅ カスタムキーバインド（Fabricキーバインド）
- ✅ 非同期HTTP通信（CompletableFuture）
- ✅ チャットフィードバック
- ✅ JSON設定ファイル（Gson）
- ✅ 多言語対応（英語・日本語）

### サーバー側
- ✅ REST API（Express）
- ✅ OBS WebSocket接続管理
- ✅ 自動再接続機能
- ✅ 2つの切り替えモード（シーン/ソース）
- ✅ ステータスAPI

### OBS側
- ✅ シーン自動切り替え
- ✅ ソース表示/非表示制御
- ✅ NDI映像受信対応

## 📊 システム要件

### 最小要件
- Minecraft 1.21.4
- Fabric Loader 0.16.10+
- Java 21+
- Node.js 18+
- OBS Studio 28.0+

### 推奨要件
- 有線LAN接続（NDI使用時）
- 8GB RAM（OBS + Minecraft）
- デュアルモニター（配信者）

## 🔐 セキュリティ

- ✅ OBS WebSocketはパスワード認証
- ✅ CORSサポート
- ⚠️ HTTPSは未実装（ローカルネットワーク前提）
- ⚠️ 認証機能なし（信頼できるネットワーク内での使用を推奨）

## 🚀 拡張可能性

### 今後追加できる機能

1. **自動復帰機能**
   - N秒後に元のシーンに戻る

2. **権限管理**
   - 特定のプレイヤーのみカメラ切り替え可能

3. **クールダウン**
   - 連続押下防止

4. **統計情報**
   - 誰が何回切り替えたかの記録

5. **Web UI**
   - ブラウザから手動切り替え可能

6. **Discord連携**
   - 切り替え通知をDiscordに送信

7. **複数シーン対応**
   - プレイヤー単独表示 / 全員表示の切り替え

## 📜 ライセンス

CC0-1.0 (パブリックドメイン)

## 👨‍💻 開発者

R41R41

## 📚 参考資料

- [Fabric Wiki](https://fabricmc.net/wiki/)
- [OBS WebSocket Protocol](https://github.com/obsproject/obs-websocket/blob/master/docs/generated/protocol.md)
- [obs-websocket-js](https://github.com/obs-websocket-community-projects/obs-websocket-js)
- [NDI SDK](https://www.ndi.tv/)

