export interface Config {
    server: {
        port: number;
    };
    obs: {
        url: string;
        password: string;
        switchMode: 'scene' | 'source'; // 'scene'=シーン切り替え, 'source'=ソース表示切り替え
        defaultScene: string; // 配信者のデフォルトシーン
    };
    playerSources: {
        [playerName: string]: string; // プレイヤー名 → OBSソース名/シーン名
    };
}

export const config: Config = {
    server: {
        port: 3000
    },
    obs: {
        url: 'ws://localhost:4455',  // OBS WebSocketのURL（デフォルト）
        password: '',                 // OBS WebSocketのパスワード
        switchMode: 'scene',          // 'scene' or 'source'
        defaultScene: 'MainScene'     // 配信者のデフォルトシーン（変更してください）
    },
    playerSources: {
        'TestPlayer': 'TestScene1',
        'Player2': 'TestScene2',
        'Player3': 'TestScene3',
    }
};

// 環境変数からの読み込み（オプション）
if (process.env.SERVER_PORT) {
    config.server.port = parseInt(process.env.SERVER_PORT);
}
if (process.env.OBS_URL) {
    config.obs.url = process.env.OBS_URL;
}
if (process.env.OBS_PASSWORD) {
    config.obs.password = process.env.OBS_PASSWORD;
}

