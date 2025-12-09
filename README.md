# OBS Camera Switching Mod

マイクラ内でショートカットキーを押すことで、配信者のOBSで自分の画面に切り替えられるFabric MODです。

## 機能

- **ショートカットキー**: `K`キー（変更可能）を押すと、配信者のOBSが自分の画面に切り替わる
- **HTTP通信**: 中継サーバーにリクエストを送信し、OBSを制御
- **設定ファイル**: サーバーURL等を設定可能

## 必要環境

- Minecraft 1.21.4
- Fabric Loader 0.16.10+
- Fabric API 0.119.2+
- Java 21

## インストール

1. `.minecraft/mods/`フォルダに`.jar`ファイルを配置
2. Minecraftを起動し、`.minecraft/config/obscameraswitching.json`を編集
3. サーバーURLを設定

## 設定例

`.minecraft/config/obscameraswitching.json`:
```json
{
  "serverUrl": "http://192.168.1.100:3000",
  "playerName": "YourName"
}
```

## システム構成

```
[プレイヤーA Minecraft + MOD] --HTTP--> [中継サーバー] --OBS WebSocket--> [配信者のOBS]
[プレイヤーB Minecraft + MOD] --HTTP--> 
[プレイヤーC Minecraft + MOD] --HTTP--> 
```

### 各プレイヤー
- 自分のPCでOBSを起動し、マイクラをウィンドウキャプチャ
- 映像を配信者に送信（NDI/配信/画面共有など）

### 配信者
- 各プレイヤーの映像をOBSのソースとして受信
- obs-websocketプラグインをインストール
- 中継サーバーからの命令でシーン/ソースを自動切り替え

## 中継サーバー

別途、Node.jsやPythonで中継サーバーを立てる必要があります。
`/server/`フォルダのREADMEを参照してください。

## ビルド方法

```bash
./gradlew build
```

生成されたJARファイルは`build/libs/`に出力されます。

## ライセンス

CC0-1.0

## 作者

R41R41

