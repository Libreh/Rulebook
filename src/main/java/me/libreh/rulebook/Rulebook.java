package me.libreh.rulebook;

import com.mojang.brigadier.Command;
import eu.pb4.placeholders.api.TextParserUtils;
import eu.pb4.playerdata.api.PlayerDataApi;
import me.libreh.rulebook.commands.Commands;
import me.libreh.rulebook.config.Config;
import me.libreh.rulebook.config.PlayerData;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.command.ServerCommandSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.literal;

public class Rulebook implements ModInitializer {
    public static final String MOD_ID = "rulebook";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Set<UUID> JOIN_LIST = new HashSet<>();
    public static final Set<UUID> RULEBOOK_LIST = new HashSet<>();

    @Override
    public void onInitialize() {
        Config.load();

        PlayerDataApi.register(PlayerData.STORAGE);

        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> {
            Commands.register(dispatcher);
            dispatcher.register(literal("rules").requires(Permissions.require("rulebook.main", true)).executes(context -> showRules(context.getSource())));
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> JOIN_LIST.add(handler.getPlayer().getUuid()));
    }

    public static int showRules(ServerCommandSource source) {
        source.sendFeedback(() -> TextParserUtils.formatText(generateRulesString()), false);

        return Command.SINGLE_SUCCESS;
    }

    private static String generateRulesString() {
        StringBuilder rulesString = new StringBuilder();

        String header = Config.getConfig().rulesHeader;
        String schema = Config.getConfig().ruleSchema;

        var rules = Config.getConfig().rules;

        if (header != null && !header.isEmpty()) {
            rulesString.append(header).append("\n");
        }

        for (int index = 0; index < rules.size(); index++) {
            var rule = rules.get(index);
            String ruleTitle = rule.title;
            String ruleDescription = rule.description;

            rulesString.append(parseRule(schema, index + 1, ruleTitle, ruleDescription));

            if (index != rules.size() - 1) {
                rulesString.append("\n");
            }
        }

        return rulesString.toString();
    }

    public static String parseRule(String line, int ruleNumber, String ruleTitle, String ruleDescription) {
        var rule = parseString(line, "rule_number", String.valueOf(ruleNumber));
        rule = parseString(rule, "rule_title", ruleTitle);
        rule = parseString(rule, "rule_description", ruleDescription);

        return rule;
    }

    private static String parseString(String string, String variableName, String variableReplacement) {
        return string.replaceAll("%" + variableName + "%", variableReplacement);
    }
}