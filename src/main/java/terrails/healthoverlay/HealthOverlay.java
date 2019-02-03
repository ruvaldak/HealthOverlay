package terrails.healthoverlay;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.events.ServerEvent;
import net.minecraft.server.MinecraftServer;

public class HealthOverlay implements ModInitializer {

    public static final String MOD_ID = "healthoverlay";
    public static final String MOD_NAME = "Health Overlay";

    @Override
    public void onInitialize() {
        SKConfig.initialize();
        HealthOverlay.initializeEvents();
    }

    private static void initializeEvents() {
        ServerEvent.START.register((MinecraftServer server) -> SKConfig.initialize());
    }
}
