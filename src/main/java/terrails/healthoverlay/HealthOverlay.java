package terrails.healthoverlay;

import me.zeroeightsix.fiber.exception.FiberException;
import me.zeroeightsix.fiber.serialization.JanksonSerializer;
import me.zeroeightsix.fiber.tree.ConfigNode;
import me.zeroeightsix.fiber.tree.ConfigValue;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HealthOverlay implements ClientModInitializer {

    public static final Logger LOGGER = LogManager.getLogger("HealthOverlay");

    public static GLColor[] healthColors;
    public static GLColor[] poisonColors;
    public static GLColor[] witherColors;

    public static GLColor[] absorptionColors;

    @Override
    public void onInitializeClient() {
        ConfigNode node = new ConfigNode();

        ConfigValue<String[]> healthRGB = ConfigValue.builder(String[].class)
                .withName("healthColors")
                .withParent(node)
                .withDefaultValue(new String[]{
                        "240,110,20", "245,220,35", "45,185,40", "30,175,190", "115,70,225",
                        "250,125,235", "235,55,90", "255,130,120", "170,255,250", "235,235,255"
                }).build();

        ConfigValue<String[]> poisonRGB = ConfigValue.builder(String[].class)
                .withName("alternatingPoisonedColors")
                .withParent(node)
                .withDefaultValue(new String[]{
                        "115,155,0", "150,205,0"
                }).build();

        ConfigValue<String[]> witherRGB = ConfigValue.builder(String[].class)
                .withName("alternatingWitheredColors")
                .withParent(node)
                .withDefaultValue(new String[]{
                        "15,15,15", "45,45,45"
                }).build();

        ConfigValue<String[]> absorptionRGB = ConfigValue.builder(String[].class)
                .withName("absorptionColors")
                .withParent(node)
                .withDefaultValue(new String[]{
                        "225,250,155", "160,255,175", "170,255,250", "170,205,255", "215,180,255",
                        "250,165,255", "255,180,180", "255,170,125", "215,240,255", "235,255,250"
                }).build();

        JanksonSerializer config = new JanksonSerializer();

        boolean recreate = false;
        while (true) {
            try {
                File file = new File(FabricLoader.getInstance().getConfigDirectory(), "healthoverlay.json");
                if (!file.exists() || recreate) {
                    config.serialize(node, Files.newOutputStream(file.toPath()));
                    LOGGER.info("Successfully created the config file in '{}'", file.toString());
                    break;
                } else {
                    try {
                        config.deserialize(node, Files.newInputStream(file.toPath()));
                        // Load current values and write to the file again in case a new value was added
                        config.serialize(node, Files.newOutputStream(file.toPath()));
                        break;
                    } catch (FiberException e) {
                        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss"));
                        String fileName = ("healthoverlay-" + time + ".json");
                        LOGGER.error("Found a syntax error in the config.");
                        if (file.renameTo(new File(file.getParent(), fileName))) { LOGGER.info("Config file successfully renamed to '{}'.", fileName); }
                        recreate = true;
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }

        healthColors = getColors(healthRGB.getValue());
        poisonColors = getColors(poisonRGB.getValue());
        witherColors = getColors(witherRGB.getValue());
        absorptionColors = getColors(absorptionRGB.getValue());
    }

    private static GLColor[] getColors(String[] heartValues) {
        GLColor[] heartColors = new GLColor[10];
        if (heartValues != null) {
            if (heartColors.length != heartValues.length) { heartColors = new GLColor[heartValues.length]; }
            for (int i = 0; i < heartValues.length; i++) {
                String[] values = heartValues[i].split(",");
                values[0] = values[0].replaceAll("\\s+","");
                values[1] = values[1].replaceAll("\\s+","");
                values[2] = values[2].replaceAll("\\s+","");
                heartColors[i] = new GLColor(Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]), 255);
            }
        }
        return heartColors;
    }
}
