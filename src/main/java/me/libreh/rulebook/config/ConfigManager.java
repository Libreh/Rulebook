package me.libreh.rulebook.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.libreh.rulebook.Rulebook;
import net.fabricmc.loader.api.FabricLoader;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class ConfigManager {
    public static int VERSION = 1;
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("rulebook.json");
    private static final Path LEGACY_DATA = FabricLoader.getInstance().getGameDir().resolve("world/player-mod-data");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Config DEFAULT_CONFIG = new Config();

    private static Config CONFIG;

    public static Config getConfig() {
        if (CONFIG == null) {
            return DEFAULT_CONFIG;
        }
        return CONFIG;
    }

    public static boolean loadConfig() {
        boolean enabled;
        try {
            Files.createDirectories(CONFIG_PATH.getParent());

            CONFIG = CONFIG_PATH.toFile().exists() ? GSON.fromJson(new InputStreamReader(new FileInputStream(CONFIG_PATH.toFile()), "UTF-8"), Config.class) : new Config();
            if (Files.exists(LEGACY_DATA)) {
                startMigration(CONFIG);
            }
            CONFIG.version = VERSION;

            saveConfig();
            enabled = true;
        } catch(Throwable exception) {
            enabled = false;
            Rulebook.LOGGER.error("Something went wrong while reading config!");
            exception.printStackTrace();
        }
        return enabled;
    }

    public static void saveConfig() {
        try {
            Files.writeString(CONFIG_PATH, GSON.toJson(CONFIG));
        } catch (Exception exception) {
            Rulebook.LOGGER.error("Something went wrong while saving config!");
            exception.printStackTrace();
        }
    }

    private static void startMigration(Config config) throws IOException {
        Rulebook.LOGGER.info("PlayerDataAPI directory exists, starting migration...");
        Files.walk(LEGACY_DATA)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".json"))
                .forEach(jsonFilePath -> {
                    UUID playerUuid = UUID.fromString(jsonFilePath.getParent().getFileName().toString());
                    if (!config.acceptedPlayers.contains(playerUuid)) {
                        try(FileReader reader = new FileReader(jsonFilePath.toFile())) {
                            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

                            if (jsonObject.has("hasAccepted") && jsonObject.get("hasAccepted").getAsBoolean()) {
                                config.acceptedPlayers.add(playerUuid);
                                Rulebook.LOGGER.info("Migrating {}", jsonFilePath.getParent().getFileName().toString());
                            }
                        } catch (IOException e) {
                            Rulebook.LOGGER.info("Error migrating UUID {}", jsonFilePath.getParent().getFileName());
                            e.printStackTrace();
                        }
                    } else {
                        Rulebook.LOGGER.info("Skipping UUID {} as player has already accepted the rules", jsonFilePath.getParent().getFileName());
                    }
                });
    }
}
