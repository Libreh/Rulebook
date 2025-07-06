package me.libreh.rulebook.commands.rulebook;

import com.mojang.brigadier.context.CommandContext;
import me.libreh.rulebook.commands.BaseCommand;
import me.libreh.rulebook.config.ConfigManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ReloadCommand extends BaseCommand {
    private static final Text SUCCESS = Text.literal("Reloaded config!");
    private static final Text FAILURE = Text.literal("Error reloading config!")
            .formatted(Formatting.RED);

    public ReloadCommand() {
        super("reload", "reload");
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) {
        boolean ok = ConfigManager.getInstance().loadConfig();
        context.getSource().sendFeedback(() -> ok ? SUCCESS : FAILURE, false);
        return ok ? 1 : 0;
    }
}