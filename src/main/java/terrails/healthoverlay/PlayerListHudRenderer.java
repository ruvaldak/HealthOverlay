package terrails.healthoverlay;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
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

    public void render(ScoreboardObjective scoreboardObjective, int y, String string, int x, int k, PlayerListEntry playerListEntry, long showTime) {
        int scoreValue = scoreboardObjective.getScoreboard().getPlayerScore(string, scoreboardObjective).getScore();

        // Make sure to only render the first 10 vanilla hearts
        scoreValue = Math.min(scoreValue, 20);

        this.client.getTextureManager().bindTexture(DrawableHelper.GUI_ICONS_LOCATION);
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
        boolean bl = playerListEntry.method_2961() > (long) this.inGameHud.getTicks() && (playerListEntry.method_2961() - (long) this.inGameHud.getTicks()) / 3L % 2L == 1L;
        if (n > 0) {
            int p = MathHelper.floor(Math.min((float) (k - x - 4) / (float) o, 9.0F));
            if (p > 3) {
                int r;
                for (r = n; r < o; ++r) {
                    this.dh.blit(x + r * p, y, bl ? 25 : 16, 0, 9, 9);
                }

                for (r = 0; r < n; ++r) {
                    this.dh.blit(x + r * p, y, bl ? 25 : 16, 0, 9, 9);
                    if (bl) {
                        if (r * 2 + 1 < method_2960) {
                            this.dh.blit(x + r * p, y, 70, 0, 9, 9);
                        }

                        if (r * 2 + 1 == method_2960) {
                            this.dh.blit(x + r * p, y, 79, 0, 9, 9);
                        }
                    }

                    if (r * 2 + 1 < scoreValue) {
                        this.dh.blit(x + r * p, y, r >= 10 ? 160 : 52, 0, 9, 9);
                    }

                    if (r * 2 + 1 == scoreValue) {
                        this.dh.blit(x + r * p, y, r >= 10 ? 169 : 61, 0, 9, 9);
                    }
                }

                // Render additional hearts
                scoreValue = scoreboardObjective.getScoreboard().getPlayerScore(string, scoreboardObjective).getScore();
                renderHearts(scoreValue, x, y, p);


            } else {
                float f = MathHelper.clamp((float) scoreValue / 20.0F, 0.0F, 1.0F);
                int s = (int) ((1.0F - f) * 255.0F) << 16 | (int) (f * 255.0F) << 8;
                String string2 = "" + (float) scoreValue / 2.0F;
                if (k - this.client.textRenderer.getStringWidth(string2 + "hp") >= x) {
                    string2 = string2 + "hp";
                }

                this.client.textRenderer.drawWithShadow(string2, (float) ((k + x) / 2 - this.client.textRenderer.getStringWidth(string2) / 2), (float) y, s);
            }
        }
    }

    private void renderHearts(int score, int xPosition, int yPos, int p) {
        int yTex = 0;
        int xTex = 0;
        int currentValue = score - 20;
        if (currentValue <= 0) return;

        this.client.getTextureManager().bindTexture(HEALTH_ICONS_LOCATION);
        int prevType = 0;
        for (int i = 0; i < MathHelper.ceil(currentValue / 2.0F); ++i) {
            RenderSystem.clearCurrentColor();
            RenderSystem.enableBlend();
            int value = i * 2 + 1;

            int typeOffset = (value / 20) % (HealthOverlay.healthColors.length);
            GLColor heartColor = (HealthOverlay.healthColors)[typeOffset];
            if (typeOffset > prevType + 1 || typeOffset < prevType - 1) prevType = typeOffset;

            int xPos = xPosition + i % 10 * p;

            // Color the hearts with a mixed color when an effect is active
            color(heartColor);

            // Full heart
            if (value < currentValue) {

                // Render heart
                inGameHud.blit(xPos, yPos, xTex, yTex, 9, 9);

                // Add shading
                colorAlpha(0.22F);
                inGameHud.blit(xPos, yPos, xTex, yTex + 9, 9, 9);

                // Add white dot
                colorAlpha(1.0F);
                inGameHud.blit(xPos, yPos, 54, yTex, 9, 9);
                // Half heart
            } else if (value == currentValue) {

                // Render heart
                inGameHud.blit(xPos, yPos, xTex + 9, yTex, 9, 9);

                // Add shading
                colorAlpha(0.22F);
                inGameHud.blit(xPos, yPos, xTex + 9, yTex + 9, 9, 9);

                // Add white dot
                colorAlpha(1.0F);
                inGameHud.blit(xPos, yPos, 54 + 9, yTex, 9, 9);
            }
        }
        RenderSystem.clearCurrentColor();
        RenderSystem.disableBlend();
        this.client.getTextureManager().bindTexture(DrawableHelper.GUI_ICONS_LOCATION);
    }

    private void color(GLColor color) {
        RenderSystem.color4f(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    private void colorAlpha(float alpha) {
        RenderSystem.color4f(1, 1, 1, alpha);
    }
}
