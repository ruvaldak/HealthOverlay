package terrails.healthoverlay;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

public class PlayerListHudRenderer {

    public static PlayerListHudRenderer INSTANCE;

    private static final Identifier HEALTH_ICONS_LOCATION = new Identifier("healthoverlay:textures/health.png");

    private final MinecraftClient client;
    private final InGameHud inGameHud;
    private final DrawableHelper dh;

    public PlayerListHudRenderer(MinecraftClient client, InGameHud inGameHud, DrawableHelper dh) {
        this.client = client;
        this.inGameHud = inGameHud;
        this.dh = dh;
    }

    public void render(ScoreboardObjective scoreboardObjective, int y, String string, int x, int k, PlayerListEntry playerListEntry, MatrixStack matrixStack, long showTime) {
        int scoreValue = scoreboardObjective.getScoreboard().getPlayerScore(string, scoreboardObjective).getScore();

        // Make sure to only render the first 10 vanilla hearts
        scoreValue = Math.min(scoreValue, 20);

        this.client.getTextureManager().bindTexture(DrawableHelper.GUI_ICONS_TEXTURE);
        long m = Util.getMeasuringTimeMs();
        if (showTime == playerListEntry.method_2976()) {
            if (scoreValue < playerListEntry.method_2973()) {
                playerListEntry.method_2978(m);
                playerListEntry.method_2975((this.inGameHud.getTicks() + 20));
            } else if (scoreValue > playerListEntry.method_2973()) {
                playerListEntry.method_2978(m);
                playerListEntry.method_2975((this.inGameHud.getTicks() + 10));
            }
        }

        if (m - playerListEntry.method_2974() > 1000L || showTime != playerListEntry.method_2976()) {
            playerListEntry.method_2972(scoreValue);
            playerListEntry.method_2965(scoreValue);
            playerListEntry.method_2978(m);
        }

        playerListEntry.method_2964(showTime);
        playerListEntry.method_2972(scoreValue);

        // Also limit this
        int method_2960 = Math.min(playerListEntry.method_2960(), 20);

        int n = MathHelper.ceil((float) Math.max(scoreValue, method_2960) / 2.0F);
        int o = Math.max(MathHelper.ceil((float) (scoreValue / 2)), Math.max(MathHelper.ceil((float) (method_2960 / 2)), 10));
        boolean highlight = playerListEntry.method_2961() > (long) this.inGameHud.getTicks() && (playerListEntry.method_2961() - (long) this.inGameHud.getTicks()) / 3L % 2L == 1L;
        if (n > 0) {
            int p = MathHelper.floor(Math.min((float) (k - x - 4) / (float) o, 9.0F));
            int r;
            for (r = n; r < o; ++r) {
                this.dh.drawTexture(matrixStack, x + r * p, y, highlight ? 25 : 16, 0, 9, 9);
            }

            for (r = 0; r < n; ++r) {
                this.dh.drawTexture(matrixStack, x + r * p, y, highlight ? 25 : 16, 0, 9, 9);
                if (highlight) {
                    if (r * 2 + 1 < method_2960) {
                        this.dh.drawTexture(matrixStack, x + r * p, y, 70, 0, 9, 9);
                    }

                    if (r * 2 + 1 == method_2960) {
                        this.dh.drawTexture(matrixStack, x + r * p, y, 79, 0, 9, 9);
                    }
                }

                if (r * 2 + 1 < scoreValue) {
                    this.dh.drawTexture(matrixStack, x + r * p, y, r >= 10 ? 160 : 52, 0, 9, 9);
                }

                if (r * 2 + 1 == scoreValue) {
                    this.dh.drawTexture(matrixStack, x + r * p, y, r >= 10 ? 169 : 61, 0, 9, 9);
                }
            }

            // Render additional hearts
            scoreValue = scoreboardObjective.getScoreboard().getPlayerScore(string, scoreboardObjective).getScore();
            renderHearts(matrixStack, scoreValue, x, y, p);
        }
    }

    private void renderHearts(MatrixStack matrixStack, int score, int xPosition, int yPos, int p) {
        int yTex = 0;
        int xTex = 0;
        int currentValue = score - 20;
        if (currentValue <= 0) return;

        RenderSystem.enableBlend();
        this.client.getTextureManager().bindTexture(HEALTH_ICONS_LOCATION);
        int prevType = 0;
        for (int i = 0; i < MathHelper.ceil(currentValue / 2.0F); ++i) {
            int value = i * 2 + 1;

            int typeOffset = (value / 20) % (HealthOverlay.healthColors.getValue().length);
            TextColor heartColor = (HealthOverlay.healthColors.getValue())[typeOffset];
            if (typeOffset > prevType + 1 || typeOffset < prevType - 1) prevType = typeOffset;

            int xPos = xPosition + (i % 10 * p);

            // Full heart
            if (value < currentValue) {

                // Render heart
                drawTexture(matrixStack, xPos, yPos, xTex, yTex, heartColor);

                // Add shading
                drawTexture(matrixStack, xPos, yPos, xTex, yTex + 9, 56);

                // Add white dot
                drawTexture(matrixStack, xPos, yPos, 54, yTex, 255);

                // Half heart
            } else if (value == currentValue) {

                // Render heart
                drawTexture(matrixStack, xPos, yPos, xTex + 9, yTex, heartColor);

                // Add shading
                drawTexture(matrixStack, xPos, yPos, xTex + 9, yTex + 9, 56);

                // Add white dot
                drawTexture(matrixStack, xPos, yPos, 54 + 9, yTex, 255);
            }
        }
        RenderSystem.disableBlend();
        this.client.getTextureManager().bindTexture(DrawableHelper.GUI_ICONS_TEXTURE);
    }

    private void drawTexture(MatrixStack matrices, int x, int y, int u, int v, int alpha) {
        drawTexture(matrices, x, y, u, v, 255, 255, 255, alpha);
    }

    private void drawTexture(MatrixStack matrices, int x, int y, int u, int v, TextColor color) {
        int rgb = color.getRgb();
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        drawTexture(matrices, x, y, u, v, r, g, b, 255);
    }

    private void drawTexture(MatrixStack matrices, int x, int y, int u, int v, int red, int green, int blue, int alpha) {
        RenderUtils.drawColoredTexturedQuad(matrices.peek().getModel(),
                x, x + 9,
                y, y + 9,
                this.dh.getZOffset(),
                (u + 0.0F) / 256.0F, (u + (float) 9) / 256.0F,
                (v + 0.0F) / 256.0F, (v + (float) 9) / 256.0F,
                red, green, blue, alpha);
    }
}
