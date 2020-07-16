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
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Random;

public class HealthRenderer {

    private static final ResourceLocation HEALTH_ICONS_LOCATION = new ResourceLocation("healthoverlay:textures/health.png");
    private static final ResourceLocation ABSORPTION_ICONS_LOCATION = new ResourceLocation("healthoverlay:textures/absorption.png");
    private static final ResourceLocation HALF_HEART_ICONS_LOCATION = new ResourceLocation("healthoverlay:textures/half_heart.png");

    private Minecraft client = Minecraft.getInstance();
    private IngameGui hud = client.ingameGUI;
    private Random random = new Random();

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
                hud.func_238474_b_(matrixStack, x, y, (highlight ? 1 : 0) * 9, 0, 9, 9);
                this.client.getTextureManager().bindTexture(AbstractGui.field_230665_h_);
            } else {
                hud.func_238474_b_(matrixStack, x, y, 16 + (highlight ? 1 : 0) * 9, 9 * hardcoreOffset, 9, 9);
            }

            // Highlight when damaged / regenerating
            if (highlight) {
                if (value < previousHealth) {
                    hud.func_238474_b_(matrixStack, x, y, effectOffset + 54, 9 * hardcoreOffset, 9, 9);
                }

                if (value == previousHealth) {
                    hud.func_238474_b_(matrixStack, x, y, effectOffset + 63, 9 * hardcoreOffset, 9, 9);
                }
            }

            // Absorption
            if (absorptionCount > 0) {
                if (absorptionCount == absorption && absorption % 2 == 1) {
                    hud.func_238474_b_(matrixStack, x, y, effectOffset + 153, 9 * hardcoreOffset, 9, 9);
                    --absorptionCount;
                } else {
                    hud.func_238474_b_(matrixStack, x, y, effectOffset + 144, 9 * hardcoreOffset, 9, 9);
                    absorptionCount -= 2;
                }
            } else {
                if (value < currentHealth) {
                    hud.func_238474_b_(matrixStack, x, y, effectOffset + 36, 9 * hardcoreOffset, 9, 9);
                }

                if (value == currentHealth) {
                    hud.func_238474_b_(matrixStack, x, y, effectOffset + 45, 9 * hardcoreOffset, 9, 9);
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
        if (currentValue < 0) return;

        this.client.getTextureManager().bindTexture(absorption ? ABSORPTION_ICONS_LOCATION : HEALTH_ICONS_LOCATION);
        int prevType = 0;
        for (int i = 0; i < MathHelper.ceil(currentValue / 2.0F); ++i) {
            GlStateManager.clearCurrentColor();
            GlStateManager.enableBlend();
            int value = i * 2 + 1;
            int regenOffset = !absorption && (i - (10 * (i / 10))) == regenHealth ? -2 : 0;

            int typeOffset = (value / 20) % (absorption ? HealthOverlay.absorptionColors.length : HealthOverlay.healthColors.length);
            GLColor heartColor = (absorption ? HealthOverlay.absorptionColors : HealthOverlay.healthColors)[typeOffset];
            if (typeOffset > prevType + 1 || typeOffset < prevType - 1) prevType = typeOffset;

            int yPos = yPosition + regenOffset;
            int xPos = xPosition + i % 10 * 8;

            // Color the hearts with a mixed color when an effect is active
            if (player.isPotionActive(Effects.POISON)) {
                xTex = 18;
                color(prevType != typeOffset ? HealthOverlay.poisonColors[0] : HealthOverlay.poisonColors[1]);
                //color(multiply(heartColor, new GLColor(/*35*/204, /*97*/204, /*36*/0), 150));
            } else if (player.isPotionActive(Effects.WITHER)) {
                xTex = 36;
                color(prevType != typeOffset ? HealthOverlay.witherColors[0] : HealthOverlay.witherColors[1]);
                //color(multiply(heartColor, new GLColor(1, 1, 1), 50));
            } else color(heartColor);

            // Full heart
            if (value < currentValue) {

                // Render heart
                hud.func_238474_b_(matrixStack, xPos, yPos, xTex, yTex, 9, 9);

                if (player.isPotionActive(Effects.WITHER)) {
                    colorAlpha(1);
                    hud.func_238474_b_(matrixStack, xPos, yPos, xTex, yTex + (yTex == 45 ? 27 : 18), 9, 9);
                } else {
                    // Add shading
                    colorAlpha(0.22F);
                    hud.func_238474_b_(matrixStack, xPos, yPos, xTex, yTex + 9, 9, 9);
                }

                // Add hardcore overlay
                if (yTex == 45 || yTex == 18) {
                    colorAlpha(0.7F);
                    hud.func_238474_b_(matrixStack, xPos, yPos, xTex, yTex + (absorption ? 9 : 18), 9, 9);
                } // Add white dot
                else {
                    colorAlpha(1.0F);
                    hud.func_238474_b_(matrixStack, xPos, yPos, (absorption ? 36 : 54), yTex, 9, 9);
                }
                // Half heart
            } else if (value == currentValue) {

                // Render heart
                hud.func_238474_b_(matrixStack, xPos, yPos, xTex + 9, yTex, 9, 9);

                if (player.isPotionActive(Effects.WITHER)) {
                    colorAlpha(1);
                    hud.func_238474_b_(matrixStack, xPos, yPos, xTex + 9, yTex + (yTex == 45 ? 27 : 18), 9, 9);
                } else {
                    // Add shading
                    colorAlpha(0.22F);
                    hud.func_238474_b_(matrixStack, xPos, yPos, xTex + 9, yTex + 9, 9, 9);
                }

                // Add hardcore overlay
                if (yTex == 45 || yTex == 18) {
                    colorAlpha(0.7F);
                    hud.func_238474_b_(matrixStack, xPos, yPos, xTex, yTex + (absorption ? 9 : 18), 9, 9);
                } // Add white dot
                else {
                    colorAlpha(1.0F);
                    hud.func_238474_b_(matrixStack, xPos, yPos, (absorption ? 36 : 54) + 9, yTex, 9, 9);
                }
            }
        }
        GlStateManager.clearCurrentColor();
        GlStateManager.disableBlend();
        this.client.getTextureManager().bindTexture(AbstractGui.field_230665_h_);
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
