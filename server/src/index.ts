import express from 'express';
import cors from 'cors';
import OBSWebSocket from 'obs-websocket-js';
import { config } from './config';

const app = express();
const obs = new OBSWebSocket();

app.use(cors());
app.use(express.json());

// OBS接続状態
let isConnected = false;
let currentActivePlayer: string | null = null;

// OBSに接続
async function connectToOBS() {
    try {
        await obs.connect(config.obs.url, config.obs.password);
        isConnected = true;
        console.log('✅ OBS WebSocketに接続しました');
    } catch (error) {
        console.error('❌ OBS WebSocket接続エラー:', error);
        isConnected = false;

        // 5秒後に再接続を試みる
        setTimeout(connectToOBS, 5000);
    }
}

// カメラ切り替えエンドポイント
app.post('/switch', async (req, res) => {
    const { playerName } = req.body;

    if (!playerName) {
        return res.status(400).json({ error: 'playerName is required' });
    }

    if (!isConnected) {
        return res.status(503).json({ error: 'OBS is not connected' });
    }

    console.log(`📹 カメラ切り替えリクエスト: ${playerName}`);
    currentActivePlayer = playerName;

    try {
        // プレイヤー名に対応するソース名を取得
        const sourceName = config.playerSources[playerName];

        if (!sourceName) {
            console.warn(`⚠️ プレイヤー "${playerName}" に対応するソースが見つかりません`);
            return res.status(404).json({ error: 'Player source not found' });
        }

        // 設定に応じて処理を分岐
        if (config.obs.switchMode === 'scene') {
            // シーン切り替えモード
            await obs.call('SetCurrentProgramScene', { sceneName: sourceName });
            console.log(`✅ シーンを切り替えました: ${sourceName}`);
        } else {
            // ソース表示切り替えモード（より高度）
            // 全てのソースを非表示にして、指定されたソースだけを表示
            const currentScene = await obs.call('GetCurrentProgramScene');
            const sceneName = currentScene.currentProgramSceneName as string;

            if (!sceneName) {
                return res.status(500).json({ error: 'Current scene name not found' });
            }

            const sceneItems = await obs.call('GetSceneItemList', { sceneName });

            // 全プレイヤーのソースを非表示に
            for (const item of sceneItems.sceneItems) {
                if (item.sourceName &&
                    item.sceneItemId !== null &&
                    item.sceneItemId !== undefined &&
                    Object.values(config.playerSources).includes(item.sourceName as string)) {
                    await obs.call('SetSceneItemEnabled', {
                        sceneName,
                        sceneItemId: item.sceneItemId as number,
                        sceneItemEnabled: false
                    });
                }
            }

            // 指定されたプレイヤーのソースだけを表示
            const targetItem = sceneItems.sceneItems.find(item => item.sourceName === sourceName);
            if (targetItem && targetItem.sceneItemId !== null && targetItem.sceneItemId !== undefined) {
                await obs.call('SetSceneItemEnabled', {
                    sceneName,
                    sceneItemId: targetItem.sceneItemId as number,
                    sceneItemEnabled: true
                });
                console.log(`✅ ソースを表示しました: ${sourceName}`);
            }
        }

        res.json({ success: true, player: playerName, source: sourceName });
    } catch (error: any) {
        console.error('❌ OBS操作エラー:', error);
        res.status(500).json({ error: error.message });
    }
});

// ステータス確認エンドポイント
app.get('/status', (req, res) => {
    res.json({
        obsConnected: isConnected,
        players: Object.keys(config.playerSources)
    });
});

// 現在のアクティブプレイヤー取得エンドポイント
app.get('/current', (req, res) => {
    res.json({
        currentPlayer: currentActivePlayer
    });
});

// 配信者に戻すエンドポイント
app.post('/return', async (req, res) => {
    if (!isConnected) {
        return res.status(503).json({ error: 'OBS is not connected' });
    }

    console.log(`🔄 配信者に戻します`);
    currentActivePlayer = null;

    try {
        await obs.call('SetCurrentProgramScene', { sceneName: config.obs.defaultScene });
        console.log(`✅ 配信者シーンに戻しました: ${config.obs.defaultScene}`);
        res.json({ success: true, scene: config.obs.defaultScene });
    } catch (error: any) {
        console.error('❌ OBS操作エラー:', error);
        res.status(500).json({ error: error.message });
    }
});

// サーバー起動
app.listen(config.server.port, () => {
    console.log(`🚀 サーバーが起動しました: http://localhost:${config.server.port}`);
    console.log(`📊 ステータス: http://localhost:${config.server.port}/status`);
    console.log(`\n設定されたプレイヤー:`);
    for (const [player, source] of Object.entries(config.playerSources)) {
        console.log(`  - ${player} → ${source}`);
    }
    console.log('\nOBSに接続中...');
    connectToOBS();
});

// OBS切断時の処理
obs.on('ConnectionClosed', () => {
    console.log('⚠️ OBS WebSocketから切断されました。再接続を試みます...');
    isConnected = false;
    setTimeout(connectToOBS, 5000);
});

