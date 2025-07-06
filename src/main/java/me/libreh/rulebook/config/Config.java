package me.libreh.rulebook.config;

import com.google.gson.annotations.SerializedName;

import java.util.*;

public final class Config {
    @SerializedName("config_version")
    private int version = ConfigManager.VERSION;
    
    @SerializedName("rules_header")
    private String rulesHeader = "Rules Header\n";
    
    @SerializedName("rule_schema")
    private String ruleSchema = "%rule_number%. %rule_title%\n%rule_description%\n";
    
    @SerializedName("final_page")
    private String finalPage = "By closing the rulebook <bold>%player:name%</bold> you hereby agree to <underline>all the rules</underline>";
    
    @SerializedName("kick_messages")
    private KickMessage kickMessages = new KickMessage();
    
    @SerializedName("rules")
    private List<Rule> rules = Arrays.asList(
            new Rule("title", "description"),
            new Rule("more title", "more description")
    );
    
    @SerializedName("accepted_players")
    private List<UUID> acceptedPlayers = new ArrayList<>();

    public int getVersion() { return version; }
    public String getRulesHeader() { return rulesHeader; }
    public String getRuleSchema() { return ruleSchema; }
    public String getFinalPage() { return finalPage; }
    public KickMessage getKickMessages() { return kickMessages; }
    public List<Rule> getRules() { return Collections.unmodifiableList(rules); }
    public List<UUID> getAcceptedPlayers() { return Collections.unmodifiableList(acceptedPlayers); }

    public void setVersion(int version) { this.version = version; }

    public void addAcceptedPlayer(UUID playerUuid) {
        if (!acceptedPlayers.contains(playerUuid)) {
            acceptedPlayers.add(playerUuid);
        }
    }
    
    public void removeAcceptedPlayer(UUID playerUuid) {
        acceptedPlayers.remove(playerUuid);
    }
    
    public boolean isPlayerAccepted(UUID playerUuid) {
        return acceptedPlayers.contains(playerUuid);
    }

    public static final class KickMessage {
        @SerializedName("didnt_read")
        private String didntRead = "<red>You didn't read all the rules!</red>";
        
        @SerializedName("updated_rules")
        private String updatedRules = "<yellow>Rules updated, please reconnect!</yellow>";

        public String getDidntRead() { return didntRead; }
        public String getUpdatedRules() { return updatedRules; }
    }

    public static final class Rule {
        private final String title;
        private final String description;
        
        public Rule(String title, String description) {
            this.title = title;
            this.description = description;
        }
        
        public String getTitle() { return title; }
        public String getDescription() { return description; }
    }
}
