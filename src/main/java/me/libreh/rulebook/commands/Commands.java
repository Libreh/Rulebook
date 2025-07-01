package me.libreh.rulebook.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import me.libreh.rulebook.util.RBUtil;
import me.libreh.rulebook.config.ConfigManager;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Commands {
    private static final int OP_LEVEL = 3;

    public static void registerRulebookCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("rulebook")
                .requires(Permissions.require("rulebook.main", true))
                .executes(context -> RBUtil.showRules(context.getSource()))
                .then(literal("open")
                        .requires(Permissions.require("rulebook.main", true))
                        .executes(context -> {
                            RBUtil.openBookGui(context.getSource().getPlayer(), false);
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(literal("reload")
                        .requires(source -> (source.isExecutedByPlayer() &&
                                hasPermission(source.getPlayer(), "rulebook.reload")) || (!source.isExecutedByPlayer()))
                        .executes(context -> {
                            if (ConfigManager.loadConfig()) {
                                context.getSource().sendFeedback(() -> Text.literal("Reloaded config successfully!"), false);
                            } else {
                                context.getSource().sendError(Text.literal("Error occurred while reloading config!").formatted(Formatting.RED));
                            }
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(literal("update")
                        .requires(source -> (source.isExecutedByPlayer() &&
                                hasPermission(source.getPlayer(), "rulebook.update")) || (!source.isExecutedByPlayer()))
                        .executes(context -> {
                            for (ServerPlayerEntity player : context.getSource().getServer().getPlayerManager().getPlayerList()) {
                                RBUtil.unaccept(player);
                            }

                            for (UUID uuid : ConfigManager.getConfig().acceptedPlayers) {
                                if (context.getSource().getServer().getPlayerManager().getPlayer(uuid) == null) {
                                    ConfigManager.getConfig().acceptedPlayers.remove(uuid);
                                    ConfigManager.saveConfig();
                                }
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(argument("players", EntityArgumentType.players())
                                .executes(context -> {
                                    for (ServerPlayerEntity player : EntityArgumentType.getPlayers(context, "players")) {
                                        RBUtil.unaccept(player);
                                    }

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                        .then(literal("offline")
                                .executes(context -> {
                                    for (UUID uuid : ConfigManager.getConfig().acceptedPlayers) {
                                        if (context.getSource().getServer().getPlayerManager().getPlayer(uuid) == null) {
                                            ConfigManager.getConfig().acceptedPlayers.remove(uuid);
                                            ConfigManager.saveConfig();
                                        }
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(literal("accept")
                        .requires(source -> source.isExecutedByPlayer() && RBUtil.hasAccepted(source.getPlayer()))
                        .requires(Permissions.require("rulebook.main", true))
                        .executes(context -> {
                            RBUtil.accept(context.getSource().getPlayer());
                            ConfigManager.saveConfig();
                            return Command.SINGLE_SUCCESS;
                        })));
    }

    public static void registerRulesCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("rules")
                .requires(Permissions.require("rulebook.main", true))
                .executes(context -> RBUtil.showRules(context.getSource())).
                        then(literal("open")
                        .requires(Permissions.require("rulebook.main", true))
                        .executes(context -> {
                            RBUtil.openBookGui(context.getSource().getPlayer(), true);
                            return Command.SINGLE_SUCCESS;
                        })));
    }

    private static boolean hasPermission(ServerPlayerEntity player, String key) {
        return Permissions.check(player, key) || player.hasPermissionLevel(OP_LEVEL);
    }
}
