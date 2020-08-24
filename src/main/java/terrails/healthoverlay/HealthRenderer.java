package terrails.healthoverlay;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.Color;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Random;

public class HealthRenderer {

    private static final ResourceLocation HEALTH_ICONS_LOCATION = new ResourceLocation("healthoverlay:textures/health.png");
    private static final ResourceLocation ABSORPTION_ICONS_LOCATION = new ResourceLocation("healthoverlay:textures/absorption.png");
    private static final ResourceLocation HALF_HEART_ICONS_LOCATION = new ResourceLocation("healthoverlay:textures/half_heart.png");

    private final Minecraft client = Minecraft.getInstance();
    private final IngameGui hud = client.ingameGUI;
    private final Random random = new Random();

    private int prevHealth, health;
    private long prevSystemTime, healthTicks;

    @SubscribeEvent(priority = EventPriority.LOW)
    public void render(RenderGameOverlayEvent.Pre event) {
        MatrixStack matrixStack = event.getMatrixStack();
        Entity renderEntity = client.getRenderViewEntity();
        if (event.getType() != RenderGameOverlayEvent.ElementType.HEALTH || !(renderEntity instanceof PlayerEntity) || event.isCanceled()
                || HealthOverlay.healthColors.length == 0 || HealthOverlay.absorptionColors.length == 0) {
            return;
        }
        PlayerEntity player = (PlayerEntity) renderEntity;
        int ticks = hud.getTicks();

        int currentHealth = MathHelper.ceil(player.getHealth());
        boolean highlight = this.healthTicks > (long) ticks && (this.healthTicks - (long) ticks) / 3L % 2L == 1L;
        long systemTime = Util.milliTime();
        if (currentHealth < this.health && player.hurtResistantTime > 0) {
            this.prevSystemTime = systemTime;
            this.healthTicks = (ticks + 20);
        } else if (currentHealth > this.health && player.hurtResistantTime > 0) {
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
        int xPos = this.client.getMainWindow().getScaledWidth() / 2 - 91;
        int yPos = this.client.getMainWindow().getScaledHeight() - 39;
        float maxHealth = player.getMaxHealth();
        int absorption = MathHelper.ceil(player.getAbsorptionAmount());

        currentHealth = Math.min(currentHealth, 20);
        previousHealth = Math.min(previousHealth, 20);
        maxHealth = Math.min(maxHealth, 20);
        absorption = Math.min(absorption, 20);
        int absorptionCount = absorption;

        int rowHeight = 10;
        int regenHealth = -1;
        // Armor gets rendered in the same row as health if this isn't set
        ForgeIngameGui.left_height += rowHeight;
        if (player.isPotionActive(Effects.REGENERATION)) {
            regenHealth = ticks % MathHelper.ceil(maxHealth + 5.0F);
        }

        int effectOffset = 16;
        if (player.isPotionActive(Effects.POISON)) {
            effectOffset += 36;
        } else if (player.isPotionActive(Effects.WITHER)) {
            effectOffset += 72;
        }

        int hardcoreOffset = 0;
        if (player.world.getWorldInfo().isHardcore()) {
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
            if ((value % 2 == 1 && value == maxHealth) || absorptionCount == absorption && absorption % 2 == 1) {
                this.client.getTextureManager().bindTexture(HALF_HEART_ICONS_LOCATION);
                drawTexture(matrixStack, x, y, (highlight ? 1 : 0) * 9, 0);
                this.client.getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION);
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
        event.setCanceled(true);
    }

    private void renderHearts(MatrixStack matrixStack, PlayerEntity player, int xPosition, int yPosition, int regenHealth, boolean absorption) {
        if (absorption && (player.isPotionActive(Effects.POISON) || player.isPotionActive(Effects.WITHER)))
            return;
        int yTex = player.world.getWorldInfo().isHardcore() ? (absorption ? 18 : 45) : 0;
        int xTex = 0;
        int currentValue = MathHelper.ceil(absorption ? player.getAbsorptionAmount() : player.getHealth()) - 20;
        if (currentValue <= 0) return;

        GlStateManager.enableBlend();
        this.client.getTextureManager().bindTexture(absorption ? ABSORPTION_ICONS_LOCATION : HEALTH_ICONS_LOCATION);
        int prevType = 0;
        for (int i = 0; i < MathHelper.ceil(currentValue / 2.0F); ++i) {
            int value = i * 2 + 1;
            int regenOffset = !absorption && (i - (10 * (i / 10))) == regenHealth ? -2 : 0;

            int typeOffset = (value / 20) % (absorption ? HealthOverlay.absorptionColors.length : HealthOverlay.healthColors.length);
            Color heartColor = (absorption ? HealthOverlay.absorptionColors : HealthOverlay.healthColors)[typeOffset];
            if (typeOffset > prevType + 1 || typeOffset < prevType - 1) prevType = typeOffset;

            int yPos = yPosition + regenOffset;
            int xPos = xPosition + i % 10 * 8;

            // Color the hearts with a mixed color when an effect is active
            if (player.isPotionActive(Effects.POISON)) {
                xTex = 18;
                heartColor = (prevType != typeOffset) ? HealthOverlay.poisonColors[0] : HealthOverlay.poisonColors[1];
                //color(multiply(heartColor, new GLColor(/*35*/204, /*97*/204, /*36*/0), 150));
            } else if (player.isPotionActive(Effects.WITHER)) {
                xTex = 36;
                heartColor = (prevType != typeOffset) ? HealthOverlay.witherColors[0] : HealthOverlay.witherColors[1];
                //color(multiply(heartColor, new GLColor(1, 1, 1), 50));
            }

            // Full heart
            if (value < currentValue) {

                // Render heart
                drawTexture(matrixStack, xPos, yPos, xTex, yTex, heartColor);

                if (player.isPotionActive(Effects.WITHER)) {
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

                if (player.isPotionActive(Effects.WITHER)) {
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
        GlStateManager.disableBlend();
        this.client.getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION);
    }

    private void drawTexture(MatrixStack matrices, int x, int y, int u, int v) {
        drawTexture(matrices, x, y, u, v, 0, 0, 0, 0);
    }

    private void drawTexture(MatrixStack matrices, int x, int y, int u, int v, int alpha) {
        drawTexture(matrices, x, y, u, v, 255, 255, 255, alpha);
    }

    private void drawTexture(MatrixStack matrices, int x, int y, int u, int v, Color color) {
        int rgb = color.func_240742_a_();
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        drawTexture(matrices, x, y, u, v, r, g, b, 255);
    }

    private void drawTexture(MatrixStack matrices, int x, int y, int u, int v, int red, int green, int blue, int alpha) {
        RenderUtils.drawColoredTexturedQuad(matrices.getLast().getMatrix(),
                x, x + 9,
                y, y + 9,
                this.hud.getBlitOffset(),
                (u + 0.0F) / 256.0F, (u + (float) 9) / 256.0F,
                (v + 0.0F) / 256.0F, (v + (float) 9) / 256.0F,
                red, green, blue, alpha);
    }
}
