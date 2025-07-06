package me.libreh.rulebook.util;

import com.mojang.brigadier.Command;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.sgui.api.elements.BookElementBuilder;
import me.libreh.rulebook.RulebookMod;
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

public final class Utils {
    private Utils() {}

    public static boolean hasAccepted(ServerPlayerEntity player) {
        return ConfigManager.getInstance().getConfig().isPlayerAccepted(player.getUuid());
    }

    public static void accept(ServerPlayerEntity player) {
        var config = ConfigManager.getInstance().getConfig();
        var playerUuid = player.getUuid();
        
        if (!config.isPlayerAccepted(playerUuid)) {
            config.addAcceptedPlayer(playerUuid);
            ConfigManager.getInstance().saveConfig();
            RulebookMod.LOGGER.debug("Player {} accepted the rules", player.getName().getString());
        }
    }

    public static void unaccept(ServerPlayerEntity player) {
        var config = ConfigManager.getInstance().getConfig();
        var playerUuid = player.getUuid();
        
        config.removeAcceptedPlayer(playerUuid);
        ConfigManager.getInstance().saveConfig();
        
        var kickMessage = Placeholders.parseText(
                RulebookMod.PARSER.parseNode(config.getKickMessages().getUpdatedRules()),
                PlaceholderContext.of(player));
        
        player.networkHandler.disconnect(kickMessage);
        RulebookMod.LOGGER.debug("Player {} unaccepted and kicked", player.getName().getString());
    }

    public static int showRules(ServerCommandSource source) {
        var rulesText = Placeholders.parseText(
                RulebookMod.PARSER.parseNode(RuleGenerator.generateRulesString()),
                PlaceholderContext.of(source));
        
        source.sendFeedback(() -> rulesText, false);
        return Command.SINGLE_SUCCESS;
    }

    public static void openBookGui(ServerPlayerEntity player, boolean kick) {
        var rulesArray = RuleGenerator.generateBookPages(player);
        var bookBuilder = new BookElementBuilder();

        for (var rule : rulesArray) {
            bookBuilder.addPage(rule);
        }

        var config = ConfigManager.getInstance().getConfig();
        var finalPage = Placeholders.parseText(
                RulebookMod.PARSER.parseNode(config.getFinalPage()),
                PlaceholderContext.of(player));
        bookBuilder.addPage(finalPage);
        
        new RulebookGui(player, bookBuilder, kick).open();
        RulebookMod.LOGGER.debug("Opened rulebook GUI for player {}", player.getName().getString());
    }

    public static ItemStack getRulebookStack(ServerPlayerEntity player) {
        var book = new ItemStack(Items.WRITTEN_BOOK);
        var bookPages = RuleGenerator.generateBookPages(player);
        
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

    public static final class RuleGenerator {
        private RuleGenerator() {}

        public static List<Text> generateBookPages(ServerPlayerEntity player) {
            var config = ConfigManager.getInstance().getConfig();
            
            List<Text> rulesList = new ArrayList<>();
            String header = config.getRulesHeader();
            String schema = config.getRuleSchema();
            var rules = config.getRules();
            
            for (int index = 0; index < rules.size(); index++) {
                var rule = rules.get(index);
                String ruleBuilder = header + "\n" + 
                        parseRule(schema, index + 1, rule.getTitle(), rule.getDescription());
                
                rulesList.add(Placeholders.parseText(
                        RulebookMod.PARSER.parseNode(ruleBuilder),
                        PlaceholderContext.of(player)));
            }
            
            return rulesList;
        }

        public static String generateRulesString() {
            var config = ConfigManager.getInstance().getConfig();
            StringBuilder rulesString = new StringBuilder();
            
            String header = config.getRulesHeader();
            String schema = config.getRuleSchema();
            var rules = config.getRules();
            
            if (header != null && !header.isEmpty()) {
                rulesString.append(header).append("\n");
            }
            
            for (int index = 0; index < rules.size(); index++) {
                var rule = rules.get(index);
                rulesString.append(parseRule(schema, index + 1, rule.getTitle(), rule.getDescription()));
                
                if (index != rules.size() - 1) {
                    rulesString.append("\n");
                }
            }
            
            return rulesString.toString();
        }

        public static String parseRule(String template, int ruleNumber, String ruleTitle, String ruleDescription) {
            var result = parseString(template, "rule_number", String.valueOf(ruleNumber));
            result = parseString(result, "rule_title", ruleTitle);
            result = parseString(result, "rule_description", ruleDescription);
            
            return result;
        }

        private static String parseString(String template, String variableName, String replacement) {
            return template.replaceAll("%" + variableName + "%", replacement);
        }
    }
}
