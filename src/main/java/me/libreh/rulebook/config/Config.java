package me.libreh.rulebook.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.TextParserUtils;
import me.libreh.rulebook.Rulebook;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private static Config CONFIG;

    public static Config getConfig() {
        return CONFIG;
    }

    public String rulesHeader = "Rules Header\n";
    public String ruleSchema = "%rule_number%. %rule_title%\n%rule_description%\n";
    public String finalPage = "By closing the rulebook <bold>%player:name%</bold> you hereby agree to <underline>all the rules</underline>";
    public KickMessage kickMessages = new KickMessage();

    public static class KickMessage {
        public String didntRead = "<red>You didn't read all the rules!</red>";
        public String updatedRules = "<yellow>Rules updated, please reconnect!</yellow>";
    }

    public static class Rule {
        public String title;
        public String description;

        Rule(String title, String description) {
            this.title = title;
            this.description = description;
        }
    }

    public List<Rule> rules = Arrays.asList(
            new Rule("title", "description"),
            new Rule("more title", "more description")
    );

    public List<UUID> acceptedPlayers = new ArrayList<>();

    public static void loadConfig() {
        Config oldConfig = CONFIG;

        CONFIG = null;
        try {
            File configFile = getConfigFile();

            CONFIG = configFile.exists() ? GSON.fromJson(new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8), Config.class) : new Config();

            {

            }
            saveConfig();
        } catch (IOException exception) {
            CONFIG = oldConfig;
            Rulebook.LOGGER.error("Something went wrong while reading config!");
            exception.printStackTrace();
        }
    }

    public static void saveConfig() {
        try {
            File configFile = getConfigFile();

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8));
            writer.write(GSON.toJson(CONFIG));
            writer.close();
        } catch (Exception exception) {
            Rulebook.LOGGER.error("Something went wrong while saving config!", exception);
        }
    }

    private static File getConfigFile() {
        return new File(FabricLoader.getInstance().getConfigDir().toFile(), Rulebook.MOD_ID + ".json");
    }

    public static boolean hasAccepted(ServerPlayerEntity player) {
        return getConfig().acceptedPlayers.contains(player.getUuid());
    }

    public static void accept(ServerPlayerEntity player) {
        getConfig().acceptedPlayers.add(player.getUuid());
        Config.saveConfig();
    }

    public static void unaccept(ServerPlayerEntity player) {
        getConfig().acceptedPlayers.remove(player.getUuid());
        Config.saveConfig();
        player.networkHandler.disconnect(Placeholders.parseText(TextParserUtils.formatText(getConfig().kickMessages.updatedRules), PlaceholderContext.of(player)));
    }
}
