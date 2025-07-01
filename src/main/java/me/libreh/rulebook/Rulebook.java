package me.libreh.rulebook;

import eu.pb4.placeholders.api.parsers.NodeParser;
import me.libreh.rulebook.commands.Commands;
import me.libreh.rulebook.config.ConfigManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Rulebook implements ModInitializer {
    public static final String MOD_ID = "rulebook";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Set<UUID> joinedPlayers = new HashSet<>();
    public static final Set<UUID> rulebookPlayers = new HashSet<>();
    public static final NodeParser PARSER = NodeParser.builder()
            .simplifiedTextFormat()
            .quickText()
            .build();

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> ConfigManager.loadConfig());

        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> {
            Commands.registerRulebookCommand(dispatcher);
            Commands.registerRulesCommand(dispatcher);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> joinedPlayers.add(handler.getPlayer().getUuid()));
    }
}