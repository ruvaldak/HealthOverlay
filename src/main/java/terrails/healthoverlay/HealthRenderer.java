package terrails.healthoverlay;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;

import java.util.Random;

@Environment(EnvType.CLIENT)
public class HealthRenderer {

    public static HealthRenderer INSTANCE = new HealthRenderer();

    private static final Identifier HEALTH_ICONS_LOCATION = new Identifier("healthoverlay:textures/health.png");
    private static final Identifier ABSORPTION_ICONS_LOCATION = new Identifier("healthoverlay:textures/absorption.png");
    private static final Identifier HALF_HEART_ICONS_LOCATION = new Identifier("healthoverlay:textures/half_heart.png");

    private final MinecraftClient client = MinecraftClient.getInstance();
    private final InGameHud hud = client.inGameHud;
    private final Random random = new Random();

    private int prevHealth, health;
    private long prevSystemTime, healthTicks;

    public void render(MatrixStack matrixStack, PlayerEntity player) {
        int ticks = hud.getTicks();

        int currentHealth = MathHelper.ceil(player.getHealth());
        boolean highlight = this.healthTicks > (long) ticks && (this.healthTicks - (long) ticks) / 3L % 2L == 1L;
        long systemTime = Util.getMeasuringTimeMs();
        if (currentHealth < this.health && player.timeUntilRegen > 0) {
            this.prevSystemTime = systemTime;
            this.healthTicks = (ticks + 20);
        } else if (currentHealth > this.health && player.timeUntilRegen > 0) {
            this.prevSystemTime = systemTime;
            this.healthTicks = (ticks + 10);
        }

        if (systemTime - this.prevSystemTime > 1000L) {
            this.health = currentHealth;
            this.prevHealth = currentHealth;
            this.prevSystemTime = systemTime;
        }

        this.health = currentHealth;
        int previousHealth = this.prevHealth;
        this.random.setSeed(ticks * 312871);
        int xPos = this.client.getWindow().getScaledWidth() / 2 - 91;
        int yPos = this.client.getWindow().getScaledHeight() - 39;
        float maxHealth = player.getMaxHealth();
        int absorption = MathHelper.ceil(player.getAbsorptionAmount());

        currentHealth = Math.min(currentHealth, 20);
        previousHealth = Math.min(previousHealth, 20);
        maxHealth = Math.min(maxHealth, 20);
        absorption = Math.min(absorption, 20);
        int absorptionCount = absorption;

        int rowHeight = 10;
        int regenHealth = -1;
        if (player.hasStatusEffect(StatusEffects.REGENERATION)) {
            regenHealth = ticks % MathHelper.ceil(maxHealth + 5.0F);
        }

        int effectOffset = 16;
        if (player.hasStatusEffect(StatusEffects.POISON)) {
            effectOffset += 36;
        } else if (player.hasStatusEffect(StatusEffects.WITHER)) {
            effectOffset += 72;
        }

        int hardcoreOffset = 0;
        if (player.world.getLevelProperties().isHardcore()) {
            hardcoreOffset = 5;
        }

        for (int i = MathHelper.ceil((maxHealth + (float) absorption) / 2.0F) - 1; i >= 0; --i) {
            int value = i * 2 + 1;
            int x = xPos + i % 10 * 8;
            int y = yPos;
            if (currentHealth <= 4) {
                y += this.random.nextInt(2);
            }

            if (absorptionCount > 0) {
                x = xPos + (MathHelper.ceil((float) absorptionCount / 2.0F) - 1) % 10 * 8;
                y = yPos - 10;
            }

            if (absorptionCount <= 0 && i == regenHealth) {
                y -= 2;
            }

            // Regular half heart background
            if ((value % 2 == 1 && value == maxHealth) || (absorptionCount == absorption && absorption % 2 == 1)) {
                this.client.getTextureManager().bindTexture(HALF_HEART_ICONS_LOCATION);
                drawTexture(matrixStack, x, y, (highlight ? 1 : 0) * 9, 0);
                this.client.getTextureManager().bindTexture(DrawableHelper.GUI_ICONS_TEXTURE);
            } else {
                drawTexture(matrixStack, x, y, 16 + (highlight ? 1 : 0) * 9, 9 * hardcoreOffset);
            }

            // Highlight when damaged / regenerating
            if (highlight) {
                if (value < previousHealth) {
                    drawTexture(matrixStack, x, y, effectOffset + 54, 9 * hardcoreOffset);
                }

                if (value == previousHealth) {
                    drawTexture(matrixStack, x, y, effectOffset + 63, 9 * hardcoreOffset);
                }
            }

            // Absorption
            if (absorptionCount > 0) {
                if (absorptionCount == absorption && absorption % 2 == 1) {
                    drawTexture(matrixStack, x, y, effectOffset + 153, 9 * hardcoreOffset);
                    --absorptionCount;
                } else {
                    drawTexture(matrixStack, x, y, effectOffset + 144, 9 * hardcoreOffset);
                    absorptionCount -= 2;
                }
            } else {
                if (value < currentHealth) {
                    drawTexture(matrixStack, x, y, effectOffset + 36, 9 * hardcoreOffset);
                }

                if (value == currentHealth) {
                    drawTexture(matrixStack, x, y, effectOffset + 45, 9 * hardcoreOffset);
                }
            }
        }

        this.renderHearts(matrixStack, player, xPos, yPos, regenHealth, false);
        this.renderHearts(matrixStack, player, xPos, yPos - rowHeight, regenHealth, true);
    }

    private void renderHearts(MatrixStack matrixStack, PlayerEntity player,  int xPosition, int yPosition, int regenHealth, boolean absorption) {
        if (absorption && (player.hasStatusEffect(StatusEffects.POISON) || player.hasStatusEffect(StatusEffects.WITHER)))
            return;
        int yTex = player.world.getLevelProperties().isHardcore() ? (absorption ? 18 : 45) : 0;
        int xTex = 0;
        int currentValue = MathHelper.ceil(absorption ? player.getAbsorptionAmount() : player.getHealth()) - 20;
        if (currentValue < 0) return;

        RenderSystem.enableBlend();
        this.client.getTextureManager().bindTexture(absorption ? ABSORPTION_ICONS_LOCATION : HEALTH_ICONS_LOCATION);
        int prevType = 0;
        for (int i = 0; i < MathHelper.ceil(currentValue / 2.0F); ++i) {
            int value = i * 2 + 1;
            int regenOffset = !absorption && (i - (10 * (i / 10))) == regenHealth ? -2 : 0;

            int typeOffset = (value / 20) % (absorption ? HealthOverlay.absorptionColors.getValue().length : HealthOverlay.healthColors.getValue().length);
            int[] heartColor = (absorption ? HealthOverlay.absorptionColors.getValue() : HealthOverlay.healthColors.getValue())[typeOffset];
            if (typeOffset > prevType + 1 || typeOffset < prevType - 1) prevType = typeOffset;

            int yPos = yPosition + regenOffset;
            int xPos = xPosition + i % 10 * 8;

            // Color the hearts with a different color when an effect is active
            if (player.hasStatusEffect(StatusEffects.POISON)) {
                xTex = 18;
                heartColor = (prevType != typeOffset) ? HealthOverlay.poisonColors.getValue()[0] : HealthOverlay.poisonColors.getValue()[1];
            } else if (player.hasStatusEffect(StatusEffects.WITHER)) {
                xTex = 36;
                heartColor = (prevType != typeOffset) ? HealthOverlay.witherColors.getValue()[0] : HealthOverlay.witherColors.getValue()[1];
            }

            // Full heart
            if (value < currentValue) {

                // Render heart
                drawTexture(matrixStack, xPos, yPos, xTex, yTex, heartColor);

                if (player.hasStatusEffect(StatusEffects.WITHER)) {
                    drawTexture(matrixStack, xPos, yPos, xTex, yTex + (yTex == 45 ? 27 : 18), 255);
                } else {
                    // Add shading
                    drawTexture(matrixStack, xPos, yPos, xTex, yTex + 9, 56);
                }

                // Add hardcore overlay
                if (yTex == 45 || yTex == 18) {
                    drawTexture(matrixStack, xPos, yPos, xTex, yTex + (absorption ? 9 : 18), 178);
                } // Add white dot
                else {
                    drawTexture(matrixStack, xPos, yPos, (absorption ? 36 : 54), yTex, 255);
                }
                // Half heart
            } else if (value == currentValue) {

                // Render heart
                drawTexture(matrixStack, xPos, yPos, xTex + 9, yTex, heartColor);

                if (player.hasStatusEffect(StatusEffects.WITHER)) {
                    drawTexture(matrixStack, xPos, yPos, xTex + 9, yTex + (yTex == 45 ? 27 : 18), 255);
                } else {
                    // Add shading
                    drawTexture(matrixStack, xPos, yPos, xTex + 9, yTex + 9, 56);
                }

                // Add hardcore overlay
                if (yTex == 45 || yTex == 18) {
                    drawTexture(matrixStack, xPos, yPos, xTex, yTex + (absorption ? 9 : 18), 178);
                } // Add white dot
                else {
                    drawTexture(matrixStack, xPos, yPos, (absorption ? 36 : 54) + 9, yTex, 255);
                }
            }
        }
        RenderSystem.disableBlend();
        this.client.getTextureManager().bindTexture(DrawableHelper.GUI_ICONS_TEXTURE);
    }

    private void drawTexture(MatrixStack matrices, int x, int y, int u, int v) {
        drawTexture(matrices, x, y, u, v, 0, 0, 0, 0);
    }

    private void drawTexture(MatrixStack matrices, int x, int y, int u, int v, int alpha) {
        drawTexture(matrices, x, y, u, v, 255, 255, 255, alpha);
    }

    private void drawTexture(MatrixStack matrices, int x, int y, int u, int v, int[] color) {
        drawTexture(matrices, x, y, u, v, color[0], color[1], color[2], 255);
    }

    private void drawTexture(MatrixStack matrices, int x, int y, int u, int v, int red, int green, int blue, int alpha) {
        drawTexturedQuad(matrices.peek().getModel(),
                x, x + 9,
                y, y + 9,
                this.hud.getZOffset(),
                (u + 0.0F) / 256.0F, (u + (float) 9) / 256.0F,
                (v + 0.0F) / 256.0F, (v + (float) 9) / 256.0F,
                red, green, blue, alpha);
    }

    private static void drawTexturedQuad(Matrix4f matrices, int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1, int red, int green, int blue, int alpha) {
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(7, alpha != 0 ? VertexFormats.POSITION_COLOR_TEXTURE : VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrices, (float) x0, (float) y1, (float) z).color(red, green, blue, alpha).texture(u0, v1).next();
        bufferBuilder.vertex(matrices, (float) x1, (float) y1, (float) z).color(red, green, blue, alpha).texture(u1, v1).next();
        bufferBuilder.vertex(matrices, (float) x1, (float) y0, (float) z).color(red, green, blue, alpha).texture(u1, v0).next();
        bufferBuilder.vertex(matrices, (float) x0, (float) y0, (float) z).color(red, green, blue, alpha).texture(u0, v0).next();
        bufferBuilder.end();
        //noinspection deprecation
        RenderSystem.enableAlphaTest();
        BufferRenderer.draw(bufferBuilder);
    }
}
