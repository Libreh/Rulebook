package me.libreh.rulebook.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private static Config CONFIG;

    public static Config getConfig() {
        return CONFIG;
    }

    @SerializedName("rules_header")
    public String rulesHeader = "Rules Header\n";
    @SerializedName("rule_schema")
    public String ruleSchema = "%rule_number%. %rule_title%\n%rule_description%\n";
    @SerializedName("accept_confirmation")
    public String acceptConfirmation = "Do you accept the rules?\nClick the checkmark if yes: ";
    @SerializedName("accept_button")
    public String acceptButton = "<green>☑</green>";
    @SerializedName("kick messages")
    public KickMessage kickMessages = new KickMessage();

    public static class KickMessage {
        @SerializedName("didnt_accept")
        public String didntAccept = "<red>You didn't accept the rules</red>";
        @SerializedName("updated_rules")
        public String updatedRules = "<yellow>Rules updated, please reconnect</yellow>";
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
}
