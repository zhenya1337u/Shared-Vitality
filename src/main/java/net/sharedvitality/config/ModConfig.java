package net.sharedvitality.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "sharedvitality.json");

    public double pool_scaling_per_player = 20.0;
    public double tether_distance_warning = 64.0;
    public double tether_distance_sever = 128.0;
    public boolean cross_dimension_decay = true;
    public boolean friendly_fire = false;

    // Persistent game & role state across restarts
    public boolean saved_game_started = false;
    public float saved_current_hp = 20.0f;
    public java.util.Map<String, String> saved_player_roles = new java.util.HashMap<>();
    public java.util.List<String> saved_chosen_once = new java.util.ArrayList<>();

    private static ModConfig INSTANCE = new ModConfig();

    public static ModConfig get() {
        return INSTANCE;
    }

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(reader, ModConfig.class);
                if (INSTANCE == null) {
                    INSTANCE = new ModConfig();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
