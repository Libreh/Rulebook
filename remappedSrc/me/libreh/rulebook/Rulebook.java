package me.libreh.rulebook;

import com.mojang.brigadier.Command;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.TextParserUtils;
import eu.pb4.sgui.api.elements.BookElementBuilder;
import me.libreh.rulebook.commands.Commands;
import me.libreh.rulebook.config.ConfigManager;
import me.libreh.rulebook.gui.RulebookGui;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static net.minecraft.server.command.CommandManager.literal;

public class Rulebook implements ModInitializer {
    public static final String MOD_ID = "rulebook";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Set<UUID> JOIN_LIST = new HashSet<>();
    public static final Set<UUID> RULEBOOK_LIST = new HashSet<>();

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> ConfigManager.loadConfig());

        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> {
            Commands.register(dispatcher);
            dispatcher.register(literal("rules")
                    .requires(Permissions.require("rulebook.main", true))
                    .executes(context -> showRules(context.getSource())).
                    then(literal("open")
                            .requires(Permissions.require("rulebook.main", true))
                            .executes(context -> {
                                Rulebook.openBookGui(context.getSource().getPlayer());

                                return Command.SINGLE_SUCCESS;
                            })
                    )
            );
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> JOIN_LIST.add(handler.getPlayer().getUuid()));
    }

    public static int showRules(ServerCommandSource source) {
        source.sendFeedback(() -> (Placeholders.parseText(TextParserUtils.formatText(generateRulesString()), PlaceholderContext.of(source))), false);

        return Command.SINGLE_SUCCESS;
    }

    public static void openBookGui(ServerPlayerEntity player) {
        var rulesArray = generateBookPages(player);
        var bookBuilder = new BookElementBuilder();
        for (var rule : rulesArray) {
            bookBuilder.addPage(rule);
        }
        bookBuilder.addPage(Placeholders.parseText(TextParserUtils.formatText(ConfigManager.getConfig().finalPage), PlaceholderContext.of(player)));
        new RulebookGui(player, bookBuilder).open();
    }

    public static List<Text> generateBookPages(ServerPlayerEntity player) {
        var config = ConfigManager.getConfig();

        List<Text> rulesList = new ArrayList<>();

        String header = config.rulesHeader;
        String schema = config.ruleSchema;

        var rules = config.rules;

        for (int index = 0; index < rules.size(); index++) {
            var rule = rules.get(index);
            String ruleTitle = rule.title;
            String ruleDescription = rule.description;

            String ruleBuilder = header + "\n" + parseRule(schema, index + 1, ruleTitle, ruleDescription);

            rulesList.add(Placeholders.parseText(TextParserUtils.formatText(ruleBuilder), PlaceholderContext.of(((player)))));
        }

        return rulesList;
    }

    private static String generateRulesString() {
        var config = ConfigManager.getConfig();

        StringBuilder rulesString = new StringBuilder();

        String header = config.rulesHeader;
        String schema = config.ruleSchema;

        var rules = config.rules;

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