package terrails.healthoverlay;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

import java.util.Random;

@Environment(EnvType.CLIENT)
public class HealthRenderer {

    public static HealthRenderer INSTANCE = new HealthRenderer();

    private static final Identifier HEALTH_ICONS_LOCATION = new Identifier("healthoverlay:textures/health.png");
    private static final Identifier ABSORPTION_ICONS_LOCATION = new Identifier("healthoverlay:textures/absorption.png");

    private MinecraftClient client = MinecraftClient.getInstance();
    private InGameHud hud = client.inGameHud;
    private Random random = new Random();
    private PlayerEntity player;

    private int prevHealth, health;
    private long prevSystemTime, healthTicks;

    public void render(PlayerEntity player) {
        this.player = player;
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
        float maxHealth = player.getMaximumHealth();
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

            /*
            == Ghost hearts ==
            this.client.getTextureManager().bindTexture(HEART_ICONS_LOCATION);
            for (int j = MathHelper.ceil((20.0F - float_1) / 2.0F) - 1; j >= 0 && int_17 == 0 && HealthRendererConfiguration.GHOST_HEARTS; --j) {
                int jx = int_4 + (MathHelper.ceil((float_1) / 2.0f) + j) % 10 * 8;
                hud.blit(jx, int_6 - (int_12 > 0 ? 10 : 0), 0, 0, 9, 9);
            }
            this.client.getTextureManager().bindTexture(DrawableHelper.GUI_ICONS_LOCATION);
            */

            // Regular half heart background
            if (value % 2 == 1 && value == maxHealth) {
                this.client.getTextureManager().bindTexture(HEALTH_ICONS_LOCATION);
                hud.blit(x, y, (highlight ? 1 : 0) * 9, 0, 9, 9);
                this.client.getTextureManager().bindTexture(DrawableHelper.GUI_ICONS_LOCATION);
            } else if (absorptionCount == absorption && absorption % 2 == 1) {
                this.client.getTextureManager().bindTexture(ABSORPTION_ICONS_LOCATION);
                hud.blit(x, y, (highlight ? 1 : 0) * 9, 0, 9, 9);
                this.client.getTextureManager().bindTexture(DrawableHelper.GUI_ICONS_LOCATION);
            } else {
                hud.blit(x, y, 16 + (highlight ? 1 : 0) * 9, 9 * hardcoreOffset, 9, 9);
            }

            // Highlight when damaged / regenerating
            if (highlight) {
                if (value < previousHealth) {
                    hud.blit(x, y, effectOffset + 54, 9 * hardcoreOffset, 9, 9);
                }

                if (value == previousHealth) {
                    hud.blit(x, y, effectOffset + 63, 9 * hardcoreOffset, 9, 9);
                }
            }

            // Absorption
            if (absorptionCount > 0) {
                if (absorptionCount == absorption && absorption % 2 == 1) {
                    hud.blit(x, y, effectOffset + 153, 9 * hardcoreOffset, 9, 9);
                    --absorptionCount;
                } else {
                    hud.blit(x, y, effectOffset + 144, 9 * hardcoreOffset, 9, 9);
                    absorptionCount -= 2;
                }
            } else {
                if (value < currentHealth) {
                    hud.blit(x, y, effectOffset + 36, 9 * hardcoreOffset, 9, 9);
                }

                if (value == currentHealth) {
                    hud.blit(x, y, effectOffset + 45, 9 * hardcoreOffset, 9, 9);
                }
            }
        }

        this.renderHearts(xPos, yPos, regenHealth, false);
        this.renderHearts(xPos, yPos - rowHeight, regenHealth, true);
    }

    private void renderHearts(int xPosition, int yPosition, int regenHealth, boolean absorption) {
        if (absorption && (player.hasStatusEffect(StatusEffects.POISON) || player.hasStatusEffect(StatusEffects.WITHER)))
            return;
        int yTex = 9;
        int xTex = this.player.world.getLevelProperties().isHardcore() ? 18 : 0;
        int currentValue = MathHelper.ceil(absorption ? this.player.getAbsorptionAmount() : this.player.getHealth()) - 20;
        if (currentValue < 0) return;

        this.client.getTextureManager().bindTexture(absorption ? ABSORPTION_ICONS_LOCATION : HEALTH_ICONS_LOCATION);
        for (int i = 0; i < MathHelper.ceil(currentValue / 2.0F); ++i) {
            GlStateManager.clearCurrentColor();
            int value = i * 2 + 1;
            int regenOffset = !absorption && (i - (10 * (i / 10))) == regenHealth ? -2 : 0;
            int typeOffset = (value / 20) % (absorption ? HealthOverlay.absorptionColors.length : HealthOverlay.healthColors.length);
            GLColor heartColor = (absorption ? HealthOverlay.absorptionColors : HealthOverlay.healthColors)[typeOffset];

            int yPos = yPosition + regenOffset;
            int xPos = xPosition + i % 10 * 8;

            // Color the hearts with a mixed color when an effect is active
            if (this.player.hasStatusEffect(StatusEffects.POISON)) {
                color(multiply(heartColor, new GLColor(35, 97, 36), 150));
            } else if (this.player.hasStatusEffect(StatusEffects.WITHER)) {
                color(multiply(heartColor, new GLColor(20, 20, 20), 200));
            } else color(heartColor);

            // Full heart
            if (value < currentValue) {

                // Render heart
                hud.blit(xPos, yPos, xTex, yTex, 9, 9);

                // Add shading
                colorAlpha(0.22F);
                hud.blit(xPos, yPos, xTex, yTex + 9, 9, 9);

                // Add hardcore overlay
                if (xTex == 18) {
                    colorAlpha(0.7F);
                    hud.blit(xPos, yPos, xTex, yTex + 18, 9, 9);
                } // Add white dot
                else {
                    colorAlpha(1.0F);
                    hud.blit(xPos, yPos, 36, yTex, 9, 9);
                }
                // Half heart
            } else if (value == currentValue) {

                // Render heart
                hud.blit(xPos, yPos, xTex + 9, yTex, 9, 9);

                // Add shading
                colorAlpha(0.22F);
                hud.blit(xPos, yPos, xTex + 9, yTex + 9, 9, 9);

                // Add hardcore overlay
                if (xTex == 18) {
                    colorAlpha(0.7F);
                    hud.blit(xPos, yPos, xTex + 9, yTex + 18, 9, 9);
                } // Add white dot
                else {
                    colorAlpha(1.0F);
                    hud.blit(xPos, yPos, 36, yTex, 9, 9);
                }
            }
        }
        GlStateManager.clearCurrentColor();
        this.client.getTextureManager().bindTexture(DrawableHelper.GUI_ICONS_LOCATION);
    }

    private GLColor multiply(GLColor color1, GLColor color2, int multiply) {
        float red = (color1.getRed() * color2.getRed()) * multiply;
        float green = (color1.getGreen() * color2.getGreen()) * multiply;
        float blue = (color1.getBlue() * color1.getBlue()) * multiply;
        return new GLColor(red, green, blue);
    }

    private void color(GLColor color) {
        GlStateManager.color4f(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    private void colorAlpha(float alpha) {
        GlStateManager.color4f(1, 1, 1, alpha);
    }
}
