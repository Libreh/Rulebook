package me.libreh.rulebook.commands.rulebook;

import com.mojang.brigadier.context.CommandContext;
import me.libreh.rulebook.commands.BaseCommand;
import me.libreh.rulebook.config.ConfigManager;
import me.libreh.rulebook.util.Utils;
import net.minecraft.server.command.ServerCommandSource;

public class AcceptCommand extends BaseCommand {
    public AcceptCommand() {
        super("accept", "accept");
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) {
        Utils.accept(context.getSource().getPlayer());
        ConfigManager.getInstance().saveConfig();
        return 1;
    }
}
