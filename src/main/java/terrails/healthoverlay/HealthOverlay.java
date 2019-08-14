package terrails.healthoverlay;

import me.zeroeightsix.fiber.JanksonSettings;
import me.zeroeightsix.fiber.exception.FiberException;
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

    public static Color[] healthColors = new Color[10];
    public static Color[] absorptionColors = new Color[10];

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

        ConfigValue<String[]> absorptionRGB = ConfigValue.builder(String[].class)
                .withName("absorptionColors")
                .withParent(node)
                .withDefaultValue(new String[]{
                        "225,250,155", "160,255,175", "170,255,250", "170,205,255", "215,180,255",
                        "250,165,255", "255,180,180", "255,170,125", "215,240,255", "235,255,250"
                }).build();

        JanksonSettings config = new JanksonSettings();

        boolean recreate = false;
        while (true) {
            try {
                File file = new File(FabricLoader.getInstance().getConfigDirectory(), "healthoverlay.json");
                if (!file.exists() || recreate) {
                    config.serialize(node, Files.newOutputStream(file.toPath()), false);
                    LOGGER.info("Successfully created the config file in '{}'", file.toString());
                    break;
                } else {
                    try {
                        config.deserialize(node, Files.newInputStream(file.toPath()));
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

        String[] healthValues = healthRGB.getValue();
        if (healthValues != null) {
            if (healthColors.length != healthValues.length) { healthColors = new Color[healthValues.length]; }
            for (int i = 0; i < healthValues.length; i++) {
                String[] values = healthValues[i].split(",");
                values[0] = values[0].replaceAll("\\s+","");
                values[1] = values[1].replaceAll("\\s+","");
                values[2] = values[2].replaceAll("\\s+","");
                healthColors[i] = new Color(Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]), 255);
            }
        }

        String[] absorptionValues = absorptionRGB.getValue();
        if (absorptionValues != null ) {
            if (absorptionColors.length != absorptionValues.length) { absorptionColors = new Color[absorptionValues.length]; }
            for (int i = 0; i < absorptionValues.length; i++) {
                String[] values = absorptionValues[i].split(",");
                values[0] = values[0].replaceAll("\\s+","");
                values[1] = values[1].replaceAll("\\s+","");
                values[2] = values[2].replaceAll("\\s+","");
                absorptionColors[i] = new Color(Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]), 255);
            }
        }
    }
}
