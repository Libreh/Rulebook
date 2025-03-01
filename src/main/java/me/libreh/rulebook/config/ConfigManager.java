package me.libreh.rulebook.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.TextParserUtils;
import me.libreh.rulebook.Rulebook;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;

import java.nio.file.Files;
import java.nio.file.Path;

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
            overrideConfig(getConfig());
        }
    }

    public static void unaccept(ServerPlayerEntity player) {
        getConfig().acceptedPlayers.remove(player.getUuid());
        overrideConfig(getConfig());
        player.networkHandler.disconnect(Placeholders.parseText(TextParserUtils.formatText(CONFIG.kickMessages.updatedRules), PlaceholderContext.of(player)));
    }
}
