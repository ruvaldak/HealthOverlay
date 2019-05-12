package terrails.healthoverlay;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.SystemUtil;
import net.minecraft.util.math.MathHelper;
import terrails.healthoverlay.api.HealthRendererConfiguration;

import java.util.Random;

@Environment(EnvType.CLIENT)
public class HealthRenderer {

    public static HealthRenderer INSTANCE = new HealthRenderer();

    private static final Identifier HEART_ICONS_LOCATION = new Identifier("healthoverlay:textures/hearts.png");

    private MinecraftClient client = MinecraftClient.getInstance();
    private InGameHud hud = client.inGameHud;
    private Random random = new Random();
    private PlayerEntity player;

    private int prevHealth, health;
    private long prevSystemTime, healthTicks;

    /**
     * Copied from InGameHud, still need to find
     * some reasonable names for the variables
     */
    public void render(PlayerEntity player) {
        this.player = player;
        int ticks = hud.getTicks();

        int int_1 = MathHelper.ceil(player.getHealth());
        boolean boolean_1 = this.healthTicks > (long) ticks && (this.healthTicks - (long) ticks) / 3L % 2L == 1L;
        long long_1 = SystemUtil.getMeasuringTimeMs();
        if (int_1 < this.health && player.field_6008 > 0) {
            this.prevSystemTime = long_1;
            this.healthTicks = (long)(ticks + 20);
        } else if (int_1 > this.health && player.field_6008 > 0) {
            this.prevSystemTime = long_1;
            this.healthTicks = (long)(ticks + 10);
        }

        if (long_1 - this.prevSystemTime > 1000L) {
            this.health = int_1;
            this.prevHealth = int_1;
            this.prevSystemTime = long_1;
        }

        this.health = int_1;
        int int_2 = this.prevHealth;
        random.setSeed((long)(ticks * 312871));
        int int_4 = this.client.window.getScaledWidth() / 2 - 91;
        int int_6 = this.client.window.getScaledHeight() - 39;
        float float_1 = Math.min(20, player.getHealthMaximum());
        int int_7 = Math.min(20, MathHelper.ceil(player.getAbsorptionAmount()));
        int int_8 = MathHelper.ceil((float_1 + (float)int_7) / 2.0F / 10.0F);
        int int_9 = Math.max(10 - (int_8 - 2), 3);
        int int_12 = int_7;
        int int_14 = -1;
        if (player.hasStatusEffect(StatusEffects.REGENERATION)) {
            int_14 = ticks % MathHelper.ceil(float_1 + 5.0F);
        }

        for(int int_17 = MathHelper.ceil((float_1 + (float)int_7) / 2.0F) - 1; int_17 >= 0; --int_17) {
            int int_18 = 16;
            if (player.hasStatusEffect(StatusEffects.POISON)) {
                int_18 += 36;
            } else if (player.hasStatusEffect(StatusEffects.WITHER)) {
                int_18 += 72;
            }

            int int_19 = 0;
            if (boolean_1) {
                int_19 = 1;
            }

            int int_26 = MathHelper.ceil((float)(int_17 + 1) / 10.0F) - 1;
            int int_21 = int_4 + int_17 % 10 * 8;
            int int_22 = int_6 - int_26 * int_9;
            // Moves the absorption up one row. Absorption is never rendered in the same row as regular health
            if (int_12 > 0) {
                int_22 = int_6 - 10;
                int_21 = int_4 + (MathHelper.ceil(int_12 / 2) - 1) % 10 * 8;
            }
            if (int_1 <= 4) {
                int_22 += random.nextInt(2);
            }

            if (int_12 <= 0 && int_17 == int_14) {
                int_22 -= 2;
            }

            int int_23 = 0;
            if (player.world.getLevelProperties().isHardcore()) {
                int_23 = 5;
            }

            // Modified to make the ghost hearts appear
            this.client.getTextureManager().bindTexture(HEART_ICONS_LOCATION);
            for (int j = MathHelper.ceil((20.0F - float_1) / 2.0F) - 1; j >= 0 && int_17 == 0 && HealthRendererConfiguration.GHOST_HEARTS; --j) {
                int jx = int_4 + (MathHelper.ceil((float_1) / 2.0f) + j) % 10 * 8;
                hud.blit(jx, int_6 - (int_12 > 0 ? 10 : 0), 0, 0, 9, 9);
            }
            this.client.getTextureManager().bindTexture(DrawableHelper.GUI_ICONS_LOCATION);

            if (int_17 * 2 + 1 > float_1 - 1 || (int_12 == int_7 && int_7 % 2 == 1)) {
                this.client.getTextureManager().bindTexture(HEART_ICONS_LOCATION);
                hud.blit(int_21, int_22, 9 + int_19 * 9, 0, 9, 9);
                this.client.getTextureManager().bindTexture(DrawableHelper.GUI_ICONS_LOCATION);
            } else {
                hud.blit(int_21, int_22, 16 + int_19 * 9, 9 * int_23, 9, 9);
            }


            if (boolean_1) {
                if (int_17 * 2 + 1 < int_2) {
                    hud.blit(int_21, int_22, int_18 + 54, 9 * int_23, 9, 9);
                }

                if (int_17 * 2 + 1 == int_2) {
                    hud.blit(int_21, int_22, int_18 + 63, 9 * int_23, 9, 9);
                }
            }

            if (int_12 > 0) {
                if (int_12 == int_7 && int_7 % 2 == 1) {
                    hud.blit(int_21, int_22, int_18 + 153, 9 * int_23, 9, 9);
                    --int_12;
                } else {
                    hud.blit(int_21, int_22, int_18 + 144, 9 * int_23, 9, 9);
                    int_12 -= 2;
                }
            } else {
                if (int_17 * 2 + 1 < int_1) {
                    hud.blit(int_21, int_22, int_18 + 36, 9 * int_23, 9, 9);
                }

                if (int_17 * 2 + 1 == int_1) {
                    hud.blit(int_21, int_22, int_18 + 45, 9 * int_23, 9, 9);
                }
            }
        }

        this.renderHearts(int_4, int_6, int_14, false);
        this.renderHearts(int_4, int_6 - 10, int_14, true);
    }

    private void renderHearts(int int_4, int int_6, int int_14, boolean absorption) {
        if (this.player.hasStatusEffect(StatusEffects.WITHER)) {
            return;
        }
        int y = absorption ? 75 : 10;
        if (this.player.hasStatusEffect(StatusEffects.POISON)) {
            y += 9;
        }

        if (this.player.world.getLevelProperties().isHardcore()) {
            y += 27;
        }

        int currentValue = MathHelper.ceil(absorption ? this.player.getAbsorptionAmount() : this.player.getHealth()) - 20;
        this.client.getTextureManager().bindTexture(HEART_ICONS_LOCATION);
        for (int i = 0; i < MathHelper.ceil(currentValue / 2.0F); ++i) {
            int value = i * 2 + 1;
            int regenOffset = !absorption && (i - (10 * (i / 10))) == int_14 ? -2 : 0;
            int typeOffset = (value / 20) % 12;

            if (value == currentValue) {
                hud.blit(int_4 + i % 10 * 8, int_6 + regenOffset, 9 + 18 * typeOffset, y, 9, 9);
            } else if (absorption || value < currentValue) {
                hud.blit(int_4 + i % 10 * 8, int_6 + regenOffset, 18 * typeOffset, y, 9, 9);
            }
        }
        this.client.getTextureManager().bindTexture(DrawableHelper.GUI_ICONS_LOCATION);
    }
}
