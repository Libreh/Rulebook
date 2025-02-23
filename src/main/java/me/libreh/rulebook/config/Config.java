package me.libreh.rulebook.config;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Config {
    public static final Config DEFAULT = new Config();
    public String _comment = "Before changing anything, see https://github.com/Libreh/Rulebook#configuration";

    @SerializedName("config_version")
    public int version = ConfigManager.VERSION;

    @SerializedName("rules_header")
    public String rulesHeader = "Rules Header\n";
    @SerializedName("rule_schema")
    public String ruleSchema = "%rule_number%. %rule_title%\n%rule_description%\n";
    @SerializedName("final_page")
    public String finalPage = "By closing the rulebook <bold>%player:name%</bold> you hereby agree to <underline>all the rules</underline>";
    @SerializedName("kick_messages")
    public KickMessage kickMessages = new KickMessage();

    public static class KickMessage {
        @SerializedName("didnt_read")
        public String didntRead = "<red>You didn't read all the rules!</red>";
        @SerializedName("updated_rules")
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

    @SerializedName("accepted_players")
    public List<UUID> acceptedPlayers = new ArrayList<>();
}
