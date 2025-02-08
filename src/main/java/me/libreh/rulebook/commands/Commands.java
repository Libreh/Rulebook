package me.libreh.rulebook.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import me.libreh.rulebook.Rulebook;
import me.libreh.rulebook.config.Config;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Commands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("rulebook")
                .requires(Permissions.require("rulebook.main", true))
                .executes(context -> Rulebook.showRules(context.getSource()))
                .then(literal("reload")
                        .requires(source -> (source.isExecutedByPlayer() &&
                                hasPermission(source.getPlayer(), "rulebook.reload")) || (!source.isExecutedByPlayer())
                        )
                        .executes(context -> {
                            Config.saveConfig();

                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(literal("update")
                        .requires(source -> (source.isExecutedByPlayer() &&
                                hasPermission(source.getPlayer(), "rulebook.update")) || (!source.isExecutedByPlayer())
                        )
                        .executes(context -> {
                            for (ServerPlayerEntity player : context.getSource().getServer().getPlayerManager().getPlayerList()) {
                                Config.unaccept(player);
                            }

                            for (UUID uuid : Config.getConfig().acceptedPlayers) {
                                if (context.getSource().getServer().getPlayerManager().getPlayer(uuid) == null) {
                                    Config.getConfig().acceptedPlayers.remove(uuid);
                                    Config.saveConfig();
                                }
                            }

                            return Command.SINGLE_SUCCESS;
                        })
                        .then(argument("players", EntityArgumentType.players())
                                .executes(context -> {
                                    for (ServerPlayerEntity player : EntityArgumentType.getPlayers(context, "players")) {
                                        Config.unaccept(player);
                                    }

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                        .then(literal("offline")
                                .requires(source -> (source.isExecutedByPlayer() &&
                                        hasPermission(source.getPlayer(), "rulebook.reload")) || (!source.isExecutedByPlayer())
                                )
                                .executes(context -> {
                                    for (UUID uuid : Config.getConfig().acceptedPlayers) {
                                        if (context.getSource().getServer().getPlayerManager().getPlayer(uuid) == null) {
                                            Config.getConfig().acceptedPlayers.remove(uuid);
                                            Config.saveConfig();
                                        }
                                    }

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(literal("accept")
                        .requires(source -> source.isExecutedByPlayer() && Config.hasAccepted(source.getPlayer()))
                        .requires(Permissions.require("rulebook.main", true))
                        .executes(context -> {
                            Config.accept(context.getSource().getPlayer());
                            Config.saveConfig();

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
