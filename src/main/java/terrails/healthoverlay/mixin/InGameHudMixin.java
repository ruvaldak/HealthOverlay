package terrails.healthoverlay.mixin;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import terrails.healthoverlay.HealthRenderer;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Redirect(method = "renderStatusBars", slice = @Slice(from = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=health")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;ceil(F)I", ordinal = 0))
    private int runDefaultRenderer(float def) {
        return -1;
    }

    @ModifyVariable(method = "renderStatusBars", at = @At(value = "STORE", ordinal = 0))
    private float modifyMaxHealth(float float_1) {
        return Math.min(20.0F, float_1);
    }

    @ModifyVariable(method = "renderStatusBars", at = @At(value = "STORE"), ordinal = 6)
    private int modifyAbsorption(int int_7) {
        return Math.min(20, int_7);
    }

    @Inject(method = "renderStatusBars", locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=health"))
    private void render(CallbackInfo info, PlayerEntity player) {
        HealthRenderer.INSTANCE.render(player);
    }
}
