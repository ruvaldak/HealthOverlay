package terrails.healthoverlay;

import io.github.fablabsmc.fablabs.api.fiber.v1.builder.ConfigTreeBuilder;
import io.github.fablabsmc.fablabs.api.fiber.v1.exception.ValueDeserializationException;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigTypes;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ListConfigType;
import io.github.fablabsmc.fablabs.api.fiber.v1.serialization.FiberSerialization;
import io.github.fablabsmc.fablabs.api.fiber.v1.serialization.JanksonValueSerializer;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HealthOverlay implements ClientModInitializer {

    public static final Logger LOGGER = LogManager.getLogger("HealthOverlay");

    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDirectory(), "healthoverlay.json5");
    private static final JanksonValueSerializer CONFIG_SERIALIZER = new JanksonValueSerializer(false);
    private static final ConfigBranch CONFIG_NODE;

    // Two dimensional array RGB for each row
    public static final PropertyMirror<int[][]> healthColors;
    public static final PropertyMirror<int[][]> absorptionColors;

    public static final PropertyMirror<int[][]> poisonColors;
    public static final PropertyMirror<int[][]> witherColors;

    static {
        ListConfigType<int[][], List<BigDecimal>> TWO_DIMENSIONAL_ARRAY = ConfigTypes.makeArray(ConfigTypes.makeIntArray(ConfigTypes.INTEGER.withValidRange(0, 255, 1)).withMinSize(3).withMaxSize(3));

        healthColors = PropertyMirror.create(TWO_DIMENSIONAL_ARRAY);
        absorptionColors = PropertyMirror.create(TWO_DIMENSIONAL_ARRAY);
        poisonColors = PropertyMirror.create(TWO_DIMENSIONAL_ARRAY);
        witherColors = PropertyMirror.create(TWO_DIMENSIONAL_ARRAY);

        ConfigTreeBuilder tree = ConfigTree.builder();

        tree.beginValue("health_colors", TWO_DIMENSIONAL_ARRAY, new int[][] {
                {240, 110, 20}, {245, 220, 35}, {45, 185, 40}, {30, 175, 190}, {115, 70, 225},
                {250, 125, 235}, {235, 55, 90}, {255, 130, 120}, {170, 255, 250}, {235, 235, 255}
        }).withComment("Colors for each new row of health [R, G, B]").finishValue(healthColors::mirror);

        tree.beginValue("absorption_colors", TWO_DIMENSIONAL_ARRAY, new int[][] {
                {225, 250, 155}, {160, 255, 175}, {170, 255, 250}, {170, 205, 255}, {215, 180, 255},
                {250, 165, 255}, {255, 180, 180}, {255, 170, 125}, {215, 240, 255}, {235, 255, 250}
        }).withComment("Colors for each new row of absorption [R, G, B]").finishValue(absorptionColors::mirror);

        tree.beginValue("poison_colors", TWO_DIMENSIONAL_ARRAY.withMinSize(2).withMaxSize(2), new int[][] {
                {115, 155, 0}, {150, 205, 0}
        }).withComment("Colors for two different rows when poisoned for easier distinction [R, G, B]").finishValue(poisonColors::mirror);

        tree.beginValue("wither_colors", TWO_DIMENSIONAL_ARRAY.withMinSize(2).withMaxSize(2), new int[][] {
                {15, 15, 15}, {45, 45, 45}
        }).withComment("Colors for two different rows when withered for easier distinction [R, G, B]").finishValue(witherColors::mirror);

        CONFIG_NODE = tree.build();
    }

    @Override
    public void onInitializeClient() {
        boolean recreate = false;
        while (true) {
            try {
                if (!CONFIG_FILE.exists() || recreate) {
                    FiberSerialization.serialize(CONFIG_NODE, Files.newOutputStream(CONFIG_FILE.toPath()), CONFIG_SERIALIZER);
                    LOGGER.info("Successfully created the config file in '{}'", CONFIG_FILE.toString());
                    break;
                } else {
                    try {
                        FiberSerialization.deserialize(CONFIG_NODE, Files.newInputStream(CONFIG_FILE.toPath()), CONFIG_SERIALIZER);

                        // Checks values and makes a copy of the config file before fixing the errors via the next method call
                        //          ....

                        // Load current values and write to the file again in case a new value was added
                        // TODO: Add some kind of error checking to the values in the file and rename the file before loading the corrected values from the branch
                        // FiberSerialization.serialize(branch, Files.newOutputStream(file.toPath()), serializer);
                        break;
                    } catch (ValueDeserializationException e) {
                        String fileName = ("healthoverlay-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss")) + ".json5");
                        LOGGER.error("Found a syntax error in the config.");
                        if (CONFIG_FILE.renameTo(new File(CONFIG_FILE.getParent(), fileName))) { LOGGER.info("Config file successfully renamed to '{}'.", fileName); }
                        recreate = true;
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
