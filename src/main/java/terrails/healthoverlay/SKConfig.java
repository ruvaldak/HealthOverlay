package terrails.healthoverlay;

import com.google.gson.*;
import net.fabricmc.loader.FabricLoader;

import java.io.*;

public class SKConfig {

    public static SKConfig instance;

    public boolean render_missing = true;

    static void initialize() {
        File configFile = new File(FabricLoader.INSTANCE.getConfigDirectory(), "healthoverlay.json");

        if (configFile.exists()) {
            try {
                Gson gson = new GsonBuilder().create();
                instance = gson.fromJson(new FileReader(configFile), SKConfig.class);
            } catch (FileNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }

        try {
            if (instance == null) instance = new SKConfig();
            FileWriter writer = new FileWriter(configFile);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(instance, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
