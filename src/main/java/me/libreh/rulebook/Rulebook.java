package me.libreh.rulebook;

import com.mojang.brigadier.Command;
import eu.pb4.placeholders.api.TextParserUtils;
import eu.pb4.playerdata.api.PlayerDataApi;
import eu.pb4.sgui.api.elements.BookElementBuilder;
import eu.pb4.sgui.api.gui.BookGui;
import eu.pb4.sgui.virtual.book.BookSlot;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
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
		Config.load();

		PlayerDataApi.register(PlayerData.STORAGE);

		CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> {
			Commands.register(dispatcher);
			dispatcher.register(literal("rules").requires(Permissions.require("rulebook.main", true)).executes(context -> showRules(context.getSource())));
		});

		ServerTickEvents.START_SERVER_TICK.register(server -> {
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				var playerUuid = player.getUuid();
				if (JOIN_LIST.contains(playerUuid)) {
					if (!PlayerData.get(player).hasAccepted) {
						if (!RULEBOOK_LIST.contains(playerUuid)) {
							if (!(player.currentScreenHandler.slots.getFirst() instanceof BookSlot)) {
								RULEBOOK_LIST.add(playerUuid);
								openBookGui(player);
							}
						} else {
							if (!(player.currentScreenHandler.slots.getFirst() instanceof BookSlot)) {
                                JOIN_LIST.remove(playerUuid);
                                RULEBOOK_LIST.remove(playerUuid);
								player.networkHandler.disconnect(TextParserUtils.formatText(Config.getConfig().messages.didntAccept));
							}
						}
					} else {
						JOIN_LIST.remove(playerUuid);
						RULEBOOK_LIST.remove(playerUuid);
					}
				}
			}
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			JOIN_LIST.add(handler.getPlayer().getUuid());
		});
	}

	private static void openBookGui(ServerPlayerEntity player) {
		var rulesArray = generateBookPages();
		var bookBuilder = new BookElementBuilder();
		for (var rule : rulesArray) {
			bookBuilder.addPage(rule);
		}
		bookBuilder.addPage(TextParserUtils.formatText(Config.getConfig().acceptConfirmation).copy().append(TextParserUtils.formatText(Config.getConfig().acceptButton)).styled(style -> style
				.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rulebook accept"))));
		new BookGui(player, bookBuilder).open();
	}


	public static int showRules(ServerCommandSource source) {
		source.sendFeedback(() -> TextParserUtils.formatText(generateRulesString()), false);

		return Command.SINGLE_SUCCESS;
	}

	private static List<Text> generateBookPages() {
		List<Text> rulesList = new ArrayList<>();

		String header = Config.getConfig().rulesHeader;
		String schema = Config.getConfig().ruleSchema;

		var rules = Config.getConfig().rules;

		for (int index = 0; index < rules.size(); index++) {
			var rule = rules.get(index);
			String ruleTitle = rule.title;
			String ruleDescription = rule.description;

			StringBuilder ruleBuilder = new StringBuilder();
			ruleBuilder.append(header).append("\n");
			ruleBuilder.append(parseRule(schema, index + 1, ruleTitle, ruleDescription));

			rulesList.add(TextParserUtils.formatText(ruleBuilder.toString()));
		}

		return rulesList;
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

	private static String parseRule(String line, int ruleNumber, String ruleTitle, String ruleDescription) {
		var rule = parseString(line, "rule_number", String.valueOf(ruleNumber));
		rule = parseString(rule, "rule_title", ruleTitle);
		rule = parseString(rule, "rule_description", ruleDescription);

		return rule;
	}

	private static String parseString(String string, String variableName, String variableReplacement) {
		return string.replaceAll("%" + variableName + "%", variableReplacement);
	}
}