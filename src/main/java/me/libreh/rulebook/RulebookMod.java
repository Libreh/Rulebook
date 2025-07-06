package me.libreh.rulebook;

import eu.pb4.placeholders.api.parsers.NodeParser;
import me.libreh.rulebook.commands.Commands;
import me.libreh.rulebook.config.ConfigManager;
import me.libreh.rulebook.services.PlayerService;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RulebookMod implements ModInitializer {
    public static final String MOD_ID = "rulebook";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final PlayerService playerService = new PlayerService();

    public static final NodeParser PARSER = NodeParser.builder()
            .simplifiedTextFormat()
            .quickText()
            .build();

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            if (ConfigManager.getInstance().loadConfig()) {
                LOGGER.info("Config loaded successfully");
            } else {
                LOGGER.error("Failed to load config");
            }
        });


        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) ->
                Commands.register(dispatcher));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                playerService.onPlayerJoin(handler.getPlayer().getUuid()));
    }

    public static PlayerService getPlayerService() {
        return playerService;
    }
}