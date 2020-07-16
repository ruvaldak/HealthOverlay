package terrails.healthoverlay;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.google.common.collect.Lists;
import net.minecraft.util.text.Color;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
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

    private static final Runnable run;
    public static Color[] healthColors;
    public static Color[] poisonColors;
    public static Color[] witherColors;

    public static Color[] absorptionColors;

    static {
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("All the values are written as a Hexadecimal number in the '#RRGGBB' format").push("Heart colors");

        ForgeConfigSpec.ConfigValue<List<? extends String>> health = builder
                .comment("Colors for every 10 hearts (not counting the default red)")
                .defineList("heartColors", Lists.newArrayList(
                        Color.func_240743_a_(0xF06E14).func_240747_b_(), Color.func_240743_a_(0xF5DC23).func_240747_b_(),
                        Color.func_240743_a_(0x2DB928).func_240747_b_(), Color.func_240743_a_(0x1EAFBE).func_240747_b_(),
                        Color.func_240743_a_(0x7346E1).func_240747_b_(), Color.func_240743_a_(0xFA7DEB).func_240747_b_(),
                        Color.func_240743_a_(0xEB375A).func_240747_b_(), Color.func_240743_a_(0xFF8278).func_240747_b_(),
                        Color.func_240743_a_(0xAAFFFA).func_240747_b_(), Color.func_240743_a_(0xEBEBFF).func_240747_b_()),
                        o -> o != null && String.class.isAssignableFrom(o.getClass()));

        ForgeConfigSpec.ConfigValue<List<? extends String>> poison = builder
                .comment("Two alternating colors when poisoned")
                .defineList("poisonColors", Lists.newArrayList(
                        Color.func_240743_a_(0x739B00).func_240747_b_(), Color.func_240743_a_(0x96CD00).func_240747_b_()
                ), o -> o != null && String.class.isAssignableFrom(o.getClass()));

        ForgeConfigSpec.ConfigValue<List<? extends String>> wither = builder
                .comment("Two alternating colors when withered")
                .defineList("witherColors", Lists.newArrayList(
                        Color.func_240743_a_(0x0F0F0F).func_240747_b_(), Color.func_240743_a_(0x2D2D2D).func_240747_b_()
                ), o -> o != null && String.class.isAssignableFrom(o.getClass()));

        ForgeConfigSpec.ConfigValue<List<? extends String>> absorption = builder
                .comment("Colors for every 10 absorption hearts (not counting the default yellow)")
                .defineList("absorptionColors", Lists.newArrayList(
                        Color.func_240743_a_(0xE1FA9B).func_240747_b_(), Color.func_240743_a_(0xA0FFAF).func_240747_b_(),
                        Color.func_240743_a_(0xAAFFFA).func_240747_b_(), Color.func_240743_a_(0xAACDFF).func_240747_b_(),
                        Color.func_240743_a_(0xD7B4FF).func_240747_b_(), Color.func_240743_a_(0xFAA5FF).func_240747_b_(),
                        Color.func_240743_a_(0xFFB4B4).func_240747_b_(), Color.func_240743_a_(0xFFAA7D).func_240747_b_(),
                        Color.func_240743_a_(0xD7F0FF).func_240747_b_(), Color.func_240743_a_(0xEBFFFA).func_240747_b_()
                ), o -> o != null && String.class.isAssignableFrom(o.getClass()));

        builder.pop();

        run = (() -> {
            healthColors = getColors(health.get());
            poisonColors = getColors(poison.get());
            witherColors = getColors(wither.get());
            absorptionColors = getColors(absorption.get());
        });
        CONFIG_SPEC = builder.build();
    }

    private static Color[] getColors(List<? extends String> stringValues) {
        Color[] colorValues = new Color[10];
        if (stringValues != null && !stringValues.isEmpty()) {
            if (colorValues.length != stringValues.size() - 1) { colorValues = new Color[stringValues.size()]; }
            for (int i = 0; i < stringValues.size(); i++) {
                colorValues[i] = Color.func_240743_a_(Integer.decode(stringValues.get(i)));
            }
        }
        return colorValues;
    }

    @SubscribeEvent
    public static void configLoading(final ModConfig.ModConfigEvent event) {
        if (!event.getConfig().getModId().equals(HealthOverlay.MOD_ID))
            return;

        run.run();
        HealthOverlay.LOGGER.debug("Loaded {} config file {}", HealthOverlay.MOD_ID, event.getConfig().getFileName());
    }
}
