package me.libreh.rulebook.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.TextParserUtils;
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

    public static void load() {
        Config oldConfig = CONFIG;

        CONFIG = null;
        try {
            File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "rulebook.json");

            Config config = configFile.exists() ? GSON.fromJson(new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8), Config.class) : new Config();

            CONFIG = config;

            {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8));
                writer.write(GSON.toJson(config));
                writer.close();
            }
        } catch (IOException exception) {
            CONFIG = oldConfig;
        }
    }

    public static boolean hasAccepted(ServerPlayerEntity player) {
        return getConfig().acceptedPlayers.contains(player.getUuid());
    }

    public static void accept(ServerPlayerEntity player) {
        getConfig().acceptedPlayers.add(player.getUuid());
    }

    public static void unaccept(ServerPlayerEntity player) {
        getConfig().acceptedPlayers.remove(player.getUuid());
        player.networkHandler.disconnect(Placeholders.parseText(TextParserUtils.formatText(Config.getConfig().kickMessages.updatedRules), PlaceholderContext.of(player)));
    }
}
