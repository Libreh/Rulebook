package me.libreh.rulebook.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.libreh.rulebook.RulebookMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public final class ConfigManager {
    public static final int VERSION = 1;
    private final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("rulebook.json");
    private final Path LEGACY_DATA = FabricLoader.getInstance().getGameDir().resolve("world/player-mod-data");

    private static ConfigManager instance;

    private final Gson gson;
    private final Config defaultConfig;
    private volatile Config config;

    private ConfigManager() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();
        this.defaultConfig = new Config();
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    public Config getConfig() {
        if (config == null) {
            return defaultConfig;
        }
        return config;
    }

    public boolean loadConfig() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());

            if (CONFIG_PATH.toFile().exists()) {
                config = loadFromFile();
            } else {
                config = new Config();
            }

            if (Files.exists(LEGACY_DATA)) {
                migrateLegacyData(config);
            }

            config.setVersion(VERSION);
            saveConfig();

            return true;
        } catch (Exception e) {
            RulebookMod.LOGGER.error("Failed to load configuration from {}", CONFIG_PATH, e);
            return false;
        }
    }

    public void saveConfig() {
        try {
            if (config != null) {
                Files.writeString(CONFIG_PATH, gson.toJson(config), StandardCharsets.UTF_8);
                RulebookMod.LOGGER.debug("Configuration saved successfully to {}", CONFIG_PATH);
            }
        } catch (Exception e) {
            RulebookMod.LOGGER.error("Failed to save configuration to {}", CONFIG_PATH, e);
        }
    }

    private Config loadFromFile() throws IOException {
        try (InputStreamReader reader = new InputStreamReader(
                new FileInputStream(CONFIG_PATH.toFile()), StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, Config.class);
        }
    }

    private void migrateLegacyData(Config config) throws IOException {
        RulebookMod.LOGGER.info("Legacy PlayerDataAPI directory found, starting migration...");
        
        Files.walk(LEGACY_DATA)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".json"))
                .forEach(jsonFilePath -> {
                    try {
                        UUID playerUuid = extractPlayerUuid(jsonFilePath);
                        if (playerUuid != null && !config.isPlayerAccepted(playerUuid)) {
                            if (hasAcceptedRules(jsonFilePath)) {
                                config.addAcceptedPlayer(playerUuid);
                                RulebookMod.LOGGER.info("Migrated player data for UUID: {}", playerUuid);
                            }
                        } else if (playerUuid != null) {
                            RulebookMod.LOGGER.debug("Skipping UUID {} as player has already accepted the rules", playerUuid);
                        }
                    } catch (Exception e) {
                        RulebookMod.LOGGER.warn("Error migrating player data from {}", jsonFilePath, e);
                    }
                });
    }

    private UUID extractPlayerUuid(Path jsonFilePath) {
        try {
            String fileName = jsonFilePath.getParent().getFileName().toString();
            return UUID.fromString(fileName);
        } catch (IllegalArgumentException e) {
            RulebookMod.LOGGER.warn("Invalid UUID format in path: {}", jsonFilePath);
            return null;
        }
    }

    private boolean hasAcceptedRules(Path jsonFilePath) {
        try (FileReader reader = new FileReader(jsonFilePath.toFile())) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            return jsonObject.has("hasAccepted") && jsonObject.get("hasAccepted").getAsBoolean();
        } catch (Exception e) {
            RulebookMod.LOGGER.warn("Failed to parse legacy player data from {}", jsonFilePath, e);
            return false;
        }
    }
}
