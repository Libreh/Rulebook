package me.libreh.rulebook.commands.rulebook;

import com.mojang.brigadier.context.CommandContext;
import me.libreh.rulebook.commands.BaseCommand;
import me.libreh.rulebook.util.Utils;
import net.minecraft.server.command.ServerCommandSource;

public class OpenCommand extends BaseCommand {
    public OpenCommand() {
        super("open", "main");
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) {
        Utils.openBookGui(context.getSource().getPlayer(), false);
        return 1;
    }
}
