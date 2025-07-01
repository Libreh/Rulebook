package me.libreh.rulebook.util;

import com.mojang.brigadier.Command;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.sgui.api.elements.BookElementBuilder;
import me.libreh.rulebook.Rulebook;
import me.libreh.rulebook.config.ConfigManager;
import me.libreh.rulebook.ui.RulebookGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class RBUtil {
    public static boolean hasAccepted(ServerPlayerEntity player) {
        return ConfigManager.getConfig().acceptedPlayers.contains(player.getUuid());
    }

    public static void accept(ServerPlayerEntity player) {
        if (!ConfigManager.getConfig().acceptedPlayers.contains(player.getUuid())) {
            ConfigManager.getConfig().acceptedPlayers.add(player.getUuid());
            ConfigManager.saveConfig();
        }
    }

    public static void unaccept(ServerPlayerEntity player) {
        ConfigManager.getConfig().acceptedPlayers.remove(player.getUuid());
        ConfigManager.saveConfig();
        player.networkHandler.disconnect(Placeholders.parseText(
                Rulebook.PARSER.parseNode(ConfigManager.getConfig().kickMessages.updatedRules),
                PlaceholderContext.of(player)));
    }

    public static int showRules(ServerCommandSource source) {
        source.sendFeedback(() ->
                Placeholders.parseText(Rulebook.PARSER.parseNode(generateRulesString()),
                        PlaceholderContext.of(source)),
                false);
        return Command.SINGLE_SUCCESS;
    }

    public static void openBookGui(ServerPlayerEntity player, boolean kick) {
        var rulesArray = generateBookPages(player);
        var bookBuilder = new BookElementBuilder();
        for (var rule : rulesArray) {
            bookBuilder.addPage(rule);
        }
        bookBuilder.addPage(Placeholders.parseText(
                Rulebook.PARSER.parseNode(ConfigManager.getConfig().finalPage),
                PlaceholderContext.of(player)));
        new RulebookGui(player, bookBuilder, kick).open();
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

            rulesList.add(Placeholders.parseText(
                    Rulebook.PARSER.parseNode(ruleBuilder),
                    PlaceholderContext.of(player)));
        }

        return rulesList;
    }

    public static ItemStack getRulebookStack(ServerPlayerEntity player) {
        var book = new ItemStack(Items.WRITTEN_BOOK);
        var bookPages = RBUtil.generateBookPages(player);

        List<RawFilteredPair<Text>> texts = new ArrayList<>();
        for (var page : bookPages) {
            texts.add(RawFilteredPair.of(page));
        }

        var data = new WrittenBookContentComponent(
                RawFilteredPair.of("Rulebook"),
                "",
                0,
                texts,
                true
        );
        book.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, data);

        return book;
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
