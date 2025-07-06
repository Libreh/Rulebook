package me.libreh.rulebook.commands;

import com.mojang.brigadier.CommandDispatcher;
import me.libreh.rulebook.RulebookMod;
import me.libreh.rulebook.commands.rulebook.AcceptCommand;
import me.libreh.rulebook.commands.rulebook.OpenCommand;
import me.libreh.rulebook.commands.rulebook.ReloadCommand;
import me.libreh.rulebook.commands.rulebook.UpdateCommand;
import me.libreh.rulebook.util.Utils;
import net.minecraft.server.command.ServerCommandSource;

public final class Commands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                ModCommand.literal(RulebookMod.MOD_ID)
                        .executes(context -> Utils.showRules(context.getSource()))
                        .then(new OpenCommand().register())
                        .then(new ReloadCommand().register())
                        .then(new UpdateCommand().register())
                        .then(new AcceptCommand().register())
        );
    }
}