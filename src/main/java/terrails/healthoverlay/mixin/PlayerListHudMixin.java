package terrails.healthoverlay.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import terrails.healthoverlay.PlayerListHudRenderer;

@Mixin(PlayerListHud.class)
public abstract class PlayerListHudMixin extends DrawableHelper {

    @Shadow private @Final MinecraftClient client;
    @Shadow private @Final InGameHud inGameHud;
    @Shadow private long showTime;

    private void renderScoreboardObjective(ScoreboardObjective scoreboardObjective, int y, String string, int x, int k, PlayerListEntry playerListEntry, MatrixStack matrixStack) {
        if (PlayerListHudRenderer.INSTANCE == null) {
            PlayerListHudRenderer.INSTANCE = new PlayerListHudRenderer(this.client, this.inGameHud, this);
        }
        PlayerListHudRenderer.INSTANCE.render(scoreboardObjective, y, string, x, k, playerListEntry, matrixStack, showTime);
    }
}
