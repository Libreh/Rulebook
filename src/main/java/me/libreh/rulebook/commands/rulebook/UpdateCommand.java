package me.libreh.rulebook.commands.rulebook;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.libreh.rulebook.commands.BaseCommand;
import me.libreh.rulebook.commands.ModCommand;
import me.libreh.rulebook.config.Config;
import me.libreh.rulebook.config.ConfigManager;
import me.libreh.rulebook.util.Utils;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;

public class UpdateCommand extends BaseCommand {
    public UpdateCommand() {
        super("update", "update");
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        for (ServerPlayerEntity player : context.getSource().getServer().getPlayerManager().getPlayerList()) {
            Utils.unaccept(player);
        }

        Config config = ConfigManager.getInstance().getConfig();
        for (UUID uuid : config.getAcceptedPlayers()) {
            if (context.getSource().getServer().getPlayerManager().getPlayer(uuid) == null) {
                config.getAcceptedPlayers().remove(uuid);
                ConfigManager.getInstance().saveConfig();
            }
        }
        return 1;
    }

    public LiteralArgumentBuilder<ServerCommandSource> register() {
        var builder = super.register();
        builder.then(argument("players", EntityArgumentType.players())
                        .executes(context -> {
                            for (ServerPlayerEntity player : EntityArgumentType.getPlayers(context, "players")) {
                                Utils.unaccept(player);
                            }
                            return 1;
                        })
                )
                .then(ModCommand.literal("offline")
                        .executes(context -> {
                            Config config = ConfigManager.getInstance().getConfig();
                            for (UUID uuid : config.getAcceptedPlayers()) {
                                if (context.getSource().getServer().getPlayerManager().getPlayer(uuid) == null) {
                                    config.getAcceptedPlayers().remove(uuid);
                                    ConfigManager.getInstance().saveConfig();
                                }
                            }
                            return 1;
                        })
                );
        return builder;
    }
}
