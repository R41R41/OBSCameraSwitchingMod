package com.obscamera;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class RecordingIndicatorRenderer implements HudRenderCallback {

    private static final int INDICATOR_SIZE = 8; // 小さく変更
    private static final int PADDING = 8;
    private static final int RED_COLOR = 0xFFFF0000; // 赤色
    private static final int DARK_RED_COLOR = 0xFF880000; // 暗い赤色（点滅用）

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        if (!ActiveStateManager.isActive()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options.hudHidden) {
            return;
        }

        // 点滅効果（1秒周期）
        long currentTime = System.currentTimeMillis();
        boolean blink = (currentTime / 500) % 2 == 0; // 0.5秒ごとに切り替え
        int color = blink ? RED_COLOR : DARK_RED_COLOR;

        // 画面左上に赤い丸を描画
        int x = PADDING;
        int y = PADDING;

        // 外側の円（より大きく）
        drawFilledCircle(drawContext, x + INDICATOR_SIZE / 2, y + INDICATOR_SIZE / 2, INDICATOR_SIZE / 2, color);

        // テキスト「録画中」を表示（小さく）
        String text = "配信中";
        int textX = x + INDICATOR_SIZE + 5;
        int textY = y + (INDICATOR_SIZE - 8) / 2; // より小さく
        // スケールを小さくして描画
        var matrices = drawContext.getMatrices();
        matrices.push();
        matrices.translate(textX, textY, 0);
        matrices.scale(0.8f, 0.8f, 1.0f); // 80%サイズ
        drawContext.drawText(client.textRenderer, text, 0, 0, color, true);
        matrices.pop();
    }

    private void drawFilledCircle(DrawContext context, int centerX, int centerY, int radius, int color) {
        // 円を四角形の集合として描画（簡易版）
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                if (x * x + y * y <= radius * radius) {
                    context.fill(centerX + x, centerY + y, centerX + x + 1, centerY + y + 1, color);
                }
            }
        }
    }
}
