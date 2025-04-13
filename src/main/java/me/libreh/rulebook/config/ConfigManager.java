package me.libreh.rulebook.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import me.libreh.rulebook.Rulebook;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static me.libreh.rulebook.Rulebook.MOD_ID;

public class ConfigManager {
    public static int VERSION = 1;
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private static Config CONFIG;

    public static Config getConfig() {
        if (CONFIG == null) {
            return Config.DEFAULT;
        }
        return CONFIG;
    }

    public static boolean loadConfig() {
        boolean ENABLED;

        try {
            Config config;

            if (Files.exists(CONFIG_PATH)) {
                config = GSON.fromJson(Files.readString(CONFIG_PATH), Config.class);
            } else {
                config = new Config();
            }
            config.version = VERSION;
            var playerModDataDir = FabricLoader.getInstance().getGameDir().resolve("world/player-mod-data");
            if (Files.exists(playerModDataDir)) {
                Rulebook.LOGGER.info("PlayerDataAPI directory exists, starting migration...");
                Files.walk(playerModDataDir)
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".json"))
                        .forEach(jsonFilePath -> {
                            UUID playerUuid = UUID.fromString(jsonFilePath.getParent().getFileName().toString());
                            if (!config.acceptedPlayers.contains(playerUuid)) {
                                try(FileReader reader = new FileReader(jsonFilePath.toFile())) {
                                    JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

                                    if (jsonObject.has("hasAccepted") && jsonObject.get("hasAccepted").getAsBoolean()) {
                                        config.acceptedPlayers.add(playerUuid);
                                        Rulebook.LOGGER.info("Migrating " + jsonFilePath.getParent().getFileName().toString());
                                    }
                                } catch (IOException e) {
                                    Rulebook.LOGGER.info("Error migrating UUID " + jsonFilePath.getParent().getFileName());
                                    e.printStackTrace();
                                }
                            } else {
                                Rulebook.LOGGER.info("Skipping UUID " + jsonFilePath.getParent().getFileName() + " as player has already accepted the rules");
                            }
                        });
            }
            overrideConfig(config);
            CONFIG = config;

            ENABLED = true;
        } catch(Throwable exception) {
            ENABLED = false;
            Rulebook.LOGGER.error("Something went wrong while reading config!");
            exception.printStackTrace();
        }

        return ENABLED;
    }

    public static void overrideConfig() {
        overrideConfig(CONFIG);
    }

    public static void overrideConfig(Config configData) {
        try {
            Files.writeString(CONFIG_PATH, GSON.toJson(configData));
            CONFIG = configData;
        } catch (Exception e) {
            Rulebook.LOGGER.error("Something went wrong while saving config!");
            e.printStackTrace();
        }
    }

    public static boolean hasAccepted(ServerPlayerEntity player) {
        return getConfig().acceptedPlayers.contains(player.getUuid());
    }

    public static void accept(ServerPlayerEntity player) {
        if (!getConfig().acceptedPlayers.contains(player.getUuid())) {
            getConfig().acceptedPlayers.add(player.getUuid());
            overrideConfig();
        }
    }

    public static void unaccept(ServerPlayerEntity player) {
        getConfig().acceptedPlayers.remove(player.getUuid());
        overrideConfig();
        player.networkHandler.disconnect(Placeholders.parseText(Rulebook.PARSER.parseNode(CONFIG.kickMessages.updatedRules), PlaceholderContext.of(player)));
    }
}
