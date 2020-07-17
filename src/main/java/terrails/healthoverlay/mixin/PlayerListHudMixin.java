package terrails.healthoverlay.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import terrails.healthoverlay.PlayerListHudRenderer;

@Mixin(PlayerListHud.class)
public abstract class PlayerListHudMixin extends DrawableHelper {

    @Shadow private @Final MinecraftClient client;
    @Shadow private @Final InGameHud inGameHud;
    @Shadow private long showTime;

    @Inject(method = "renderScoreboardObjective", cancellable = true, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/MinecraftClient;getTextureManager()Lnet/minecraft/client/texture/TextureManager;", shift = At.Shift.BEFORE))
    private void renderScoreboardObjective(ScoreboardObjective scoreboardObjective, int y, String string, int x, int k, PlayerListEntry playerListEntry, MatrixStack matrixStack, CallbackInfo callbackInfo) {
        if (PlayerListHudRenderer.INSTANCE == null) {
            PlayerListHudRenderer.INSTANCE = new PlayerListHudRenderer(this.client, this.inGameHud, this);
        }
        PlayerListHudRenderer.INSTANCE.render(scoreboardObjective, y, string, x, k, playerListEntry, matrixStack, showTime);
        callbackInfo.cancel();
    }
}
