package terrails.healthoverlay;

import io.github.fablabsmc.fablabs.api.fiber.v1.builder.ConfigTreeBuilder;
import io.github.fablabsmc.fablabs.api.fiber.v1.exception.ValueDeserializationException;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.StringSerializableType;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigTypes;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ListConfigType;
import io.github.fablabsmc.fablabs.api.fiber.v1.serialization.FiberSerialization;
import io.github.fablabsmc.fablabs.api.fiber.v1.serialization.JanksonValueSerializer;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.TextColor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class HealthOverlay implements ClientModInitializer {

    public static final Logger LOGGER = LogManager.getLogger("HealthOverlay");

    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "healthoverlay.json5");
    private static final JanksonValueSerializer CONFIG_SERIALIZER = new JanksonValueSerializer(false);
    private static final ConfigBranch CONFIG_NODE;

    public static final PropertyMirror<TextColor[]> healthColors;
    public static final PropertyMirror<TextColor[]> absorptionColors;

    public static final PropertyMirror<TextColor[]> poisonColors;
    public static final PropertyMirror<TextColor[]> witherColors;

    static {
        ListConfigType<TextColor[], String> COLOR = ConfigTypes.makeArray(ConfigTypes.STRING
                .withType(new StringSerializableType(7, 7, Pattern.compile("^#[0-9a-fA-F]{6}+$")))
                .derive(TextColor.class, s -> TextColor.fromRgb(Integer.decode(s)), TextColor::toString));

        healthColors = PropertyMirror.create(COLOR);
        absorptionColors = PropertyMirror.create(COLOR);
        poisonColors = PropertyMirror.create(COLOR);
        witherColors = PropertyMirror.create(COLOR);

        ConfigTreeBuilder tree = ConfigTree.builder();

        tree.beginValue("health_colors", COLOR, new TextColor[] {
                TextColor.fromRgb(0xF06E14), TextColor.fromRgb(0xF5DC23), TextColor.fromRgb(0x2DB928), TextColor.fromRgb(0x1EAFBE), TextColor.fromRgb(0x7346E1),
                TextColor.fromRgb(0xFA7DEB), TextColor.fromRgb(0xEB375A), TextColor.fromRgb(0xFF8278), TextColor.fromRgb(0xAAFFFA), TextColor.fromRgb(0xEBEBFF)
        }).withComment("Colors for each new row of health (Hexadecimal)").finishValue(healthColors::mirror);

        tree.beginValue("absorption_colors", COLOR, new TextColor[] {
                TextColor.fromRgb(0xE1FA9B), TextColor.fromRgb(0xA0FFAF), TextColor.fromRgb(0xAAFFFA), TextColor.fromRgb(0xAACDFF), TextColor.fromRgb(0xD7B4FF),
                TextColor.fromRgb(0xFAA5FF), TextColor.fromRgb(0xFFB4B4), TextColor.fromRgb(0xFFAA7D), TextColor.fromRgb(0xD7F0FF), TextColor.fromRgb(0xEBFFFA)
        }).withComment("Colors for each new row of absorption (Hexadecimal)").finishValue(absorptionColors::mirror);

        tree.beginValue("poison_colors", COLOR.withMinSize(2).withMaxSize(2), new TextColor[] { TextColor.fromRgb(0x739B00), TextColor.fromRgb(0x96CD00) }
        ).withComment("Colors for two different rows when poisoned for easier distinction (Hexadecimal)").finishValue(poisonColors::mirror);

        tree.beginValue("wither_colors", COLOR.withMinSize(2).withMaxSize(2), new TextColor[] { TextColor.fromRgb(0x0F0F0F), TextColor.fromRgb(0x2D2D2D) }
        ).withComment("Colors for two different rows when withered for easier distinction (Hexadecimal)").finishValue(witherColors::mirror);

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
