package terrails.healthoverlay;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.List;

@Mod(HealthOverlay.MOD_ID)
@EventBusSubscriber(bus = Bus.MOD)
public class HealthOverlay {

    public static final Logger LOGGER = LogManager.getLogger("HealthOverlay");
    public static final String MOD_ID = "healthoverlay";

    private static final ForgeConfigSpec CONFIG_SPEC;

    public HealthOverlay() {
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CONFIG_SPEC, "healthoverlay.toml");
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new HealthRenderer());
        loadConfig(FMLPaths.CONFIGDIR.get().resolve("healthoverlay.toml"));
    }

    private static void loadConfig(Path path) {
        HealthOverlay.LOGGER.debug("Loading config file {}", path);

        final CommentedFileConfig configData = CommentedFileConfig.builder(path)
                .sync()
                .autosave()
                .writingMode(WritingMode.REPLACE)
                .build();

        HealthOverlay.LOGGER.debug("Built TOML config for {}", path.toString());
        configData.load();
        HealthOverlay.LOGGER.debug("Loaded TOML config file {}", path.toString());
        CONFIG_SPEC.setConfig(configData);
    }

    private static Runnable run;
    public static GLColor[] healthColors;
    public static GLColor[] poisonColors;
    public static GLColor[] witherColors;

    public static GLColor[] absorptionColors;

    static {
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("Heart colors");
        ForgeConfigSpec.ConfigValue<List<? extends String>> health = builder
                .comment("RGB values for every 10 hearts (not counting the default red)")
                .defineList("heartColors", Lists.newArrayList("240,110,20", "245,220,35", "45,185,40", "30,175,190", "115,70,225",
                        "250,125,235", "235,55,90", "255,130,120", "170,255,250", "235,235,255"), o -> o != null && String.class.isAssignableFrom(o.getClass()));

        ForgeConfigSpec.ConfigValue<List<? extends String>> poison = builder
                .comment("Two alternating RGB values when poisoned")
                .defineList("poisonColors", Lists.newArrayList("115,155,0", "150,205,0"), o -> o != null && String.class.isAssignableFrom(o.getClass()));

        ForgeConfigSpec.ConfigValue<List<? extends String>> wither = builder
                .comment("Two alternating RGB values when withered")
                .defineList("witherColors", Lists.newArrayList("15,15,15", "45,45,45"), o -> o != null && String.class.isAssignableFrom(o.getClass()));

        ForgeConfigSpec.ConfigValue<List<? extends String>> absorption = builder
                .comment("RGB values for every 10 absorption hearts (not counting the default red)")
                .defineList("absorptionColors", Lists.newArrayList("225,250,155", "160,255,175", "170,255,250", "170,205,255", "215,180,255",
                        "250,165,255", "255,180,180", "255,170,125", "215,240,255", "235,255,250"), o -> o != null && String.class.isAssignableFrom(o.getClass()));
        builder.pop();

        run = (() -> {
            healthColors = getColors(health.get());
            poisonColors = getColors(poison.get());
            witherColors = getColors(wither.get());
            absorptionColors = getColors(absorption.get());
        });
        CONFIG_SPEC = builder.build();
    }

    private static GLColor[] getColors(List<? extends String> heartValues) {
        GLColor[] heartColors = new GLColor[10];
        if (heartValues != null && !heartValues.isEmpty()) {
            if (heartColors.length != heartValues.size() - 1) { heartColors = new GLColor[heartValues.size()]; }
            for (int i = 0; i < heartValues.size(); i++) {
                String[] values = heartValues.get(i).split(",");
                values[0] = values[0].replaceAll("\\s+","");
                values[1] = values[1].replaceAll("\\s+","");
                values[2] = values[2].replaceAll("\\s+","");
                heartColors[i] = new GLColor(Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]), 255);
            }
        }
        return heartColors;
    }

    @SubscribeEvent
    public static void configLoading(final ModConfig.ModConfigEvent event) {
        if (!event.getConfig().getModId().equals(HealthOverlay.MOD_ID))
            return;

        run.run();
        HealthOverlay.LOGGER.debug("Loaded {} config file {}", HealthOverlay.MOD_ID, event.getConfig().getFileName());
    }
}
