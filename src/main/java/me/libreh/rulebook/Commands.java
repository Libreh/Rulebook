package me.libreh.rulebook;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import eu.pb4.placeholders.api.TextParserUtils;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.File;
import java.util.Arrays;

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
                            Config.load();

                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(literal("update")
                        .requires(source -> (source.isExecutedByPlayer() &&
                                hasPermission(source.getPlayer(), "rulebook.update")) || (!source.isExecutedByPlayer())
                        )
                        .executes(context -> {
                            for (ServerPlayerEntity player : context.getSource().getServer().getPlayerManager().getPlayerList()) {
                                updatePlayer(player);
                            }

                            try {
                                for (File file : FabricLoader.getInstance().getGameDir().resolve("world/player-mod-data").toFile().listFiles()) {
                                    if (file.isDirectory()) {
                                        for (File jsonFile : file.listFiles()) {
                                            if (jsonFile.getName().equals("rulebook.json")) {
                                                jsonFile.delete();
                                            }
                                        }
                                    }
                                }
                            } catch (Exception ignored) {}

                            return Command.SINGLE_SUCCESS;
                        })
                        .then(argument("players", EntityArgumentType.players())
                                .executes(context -> {
                                    for (ServerPlayerEntity player : EntityArgumentType.getPlayers(context, "players")) {
                                        updatePlayer(player);
                                    }

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                        .then(literal("offline")
                                .requires(source -> (source.isExecutedByPlayer() &&
                                        hasPermission(source.getPlayer(), "rulebook.reload")) || (!source.isExecutedByPlayer())
                                )
                                .executes(context -> {
                                    try {
                                        for (File file : FabricLoader.getInstance().getGameDir().resolve("world/player-mod-data").toFile().listFiles()) {
                                            if (file.isDirectory()) {
                                                for (File jsonFile : file.listFiles()) {
                                                    if (jsonFile.getName().equals("rulebook.json")) {
                                                        jsonFile.delete();
                                                    }
                                                }
                                            }
                                        }
                                    } catch (Exception ignored) {}

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(literal("accept")
                        .requires(source -> source.isExecutedByPlayer() && !PlayerData.get(source.getPlayer()).hasAccepted)
                        .requires(Permissions.require("rulebook.main", true))
                        .executes(context -> {
                            acceptRules(context.getSource().getPlayer());

                            return Command.SINGLE_SUCCESS;
                        })
                )
        );
    }

    public static void acceptRules(ServerPlayerEntity player) {
        var data = PlayerData.get(player);
        data.hasAccepted = true;
        PlayerData.STORAGE.save(player, data);
    }

    private static void updatePlayer(ServerPlayerEntity player) {
        var data = PlayerData.get(player);
        data.hasAccepted = false;
        PlayerData.STORAGE.save(player, data);
        player.networkHandler.disconnect(TextParserUtils.formatText(Config.getConfig().kickMessages.updatedRules));

        Rulebook.LOGGER.info(Arrays.toString(FabricLoader.getInstance().getGameDir().toFile().list()));

    }

    private static boolean hasPermission(ServerPlayerEntity player, String key) {
        return Permissions.check(player, key) || player.hasPermissionLevel(1) || player.hasPermissionLevel(2) ||
                player.hasPermissionLevel(3) || player.hasPermissionLevel(4);
    }
}
