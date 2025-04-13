package me.libreh.rulebook.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import me.libreh.rulebook.Rulebook;
import me.libreh.rulebook.config.ConfigManager;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.UUID;

import static me.libreh.rulebook.config.ConfigManager.getConfig;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Commands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var config = getConfig();

        dispatcher.register(literal("rulebook")
                .requires(Permissions.require("rulebook.main", true))
                .executes(context -> Rulebook.showRules(context.getSource()))
                .then(literal("open")
                        .requires(Permissions.require("rulebook.main", true))
                        .executes(context -> {
                            Rulebook.openBookGui(context.getSource().getPlayer(), false);

                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(literal("reload")
                        .requires(source -> (source.isExecutedByPlayer() &&
                                hasPermission(source.getPlayer(), "rulebook.reload")) || (!source.isExecutedByPlayer())
                        )
                        .executes(context -> {
                            if (ConfigManager.loadConfig()) {
                                context.getSource().sendFeedback(() -> Text.literal("Reloaded config!"), false);
                            } else {
                                context.getSource().sendError(Text.literal("Error occurred while reloading config!").formatted(Formatting.RED));
                            }

                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(literal("update")
                        .requires(source -> (source.isExecutedByPlayer() &&
                                hasPermission(source.getPlayer(), "rulebook.update")) || (!source.isExecutedByPlayer())
                        )
                        .executes(context -> {
                            for (ServerPlayerEntity player : context.getSource().getServer().getPlayerManager().getPlayerList()) {
                                ConfigManager.unaccept(player);
                            }

                            for (UUID uuid : config.acceptedPlayers) {
                                if (context.getSource().getServer().getPlayerManager().getPlayer(uuid) == null) {
                                    config.acceptedPlayers.remove(uuid);
                                    ConfigManager.overrideConfig();
                                }
                            }

                            return Command.SINGLE_SUCCESS;
                        })
                        .then(argument("players", EntityArgumentType.players())
                                .executes(context -> {
                                    for (ServerPlayerEntity player : EntityArgumentType.getPlayers(context, "players")) {
                                        ConfigManager.unaccept(player);
                                    }

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                        .then(literal("offline")
                                .requires(source -> (source.isExecutedByPlayer() &&
                                        hasPermission(source.getPlayer(), "rulebook.reload")) || (!source.isExecutedByPlayer())
                                )
                                .executes(context -> {
                                    for (UUID uuid : config.acceptedPlayers) {
                                        if (context.getSource().getServer().getPlayerManager().getPlayer(uuid) == null) {
                                            getConfig().acceptedPlayers.remove(uuid);
                                            ConfigManager.overrideConfig();
                                        }
                                    }

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(literal("accept")
                        .requires(source -> source.isExecutedByPlayer() && ConfigManager.hasAccepted(source.getPlayer()))
                        .requires(Permissions.require("rulebook.main", true))
                        .executes(context -> {
                            ConfigManager.accept(context.getSource().getPlayer());
                            ConfigManager.overrideConfig();

                            return Command.SINGLE_SUCCESS;
                        })
                )
        );
    }

    private static boolean hasPermission(ServerPlayerEntity player, String key) {
        return Permissions.check(player, key) || player.hasPermissionLevel(1) || player.hasPermissionLevel(2) ||
                player.hasPermissionLevel(3) || player.hasPermissionLevel(4);
    }
}
