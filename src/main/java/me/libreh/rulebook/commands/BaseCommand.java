package me.libreh.rulebook.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.libreh.rulebook.RulebookMod;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public abstract class BaseCommand implements ModCommand {
    private final String name;
    private final String permission;

    protected BaseCommand(String name, String permission) {
        this.name = name;
        this.permission = RulebookMod.MOD_ID + "." + permission;
    }

    public LiteralArgumentBuilder<ServerCommandSource> register() {
        return ModCommand.literal(name)
            .requires(this::isAllowed)
            .executes(this);
    }

    private boolean isAllowed(ServerCommandSource source) {
        if (source.isExecutedByPlayer()) {
            return hasPermission(source.getPlayer());
        }
        return true;
    }

    private boolean hasPermission(ServerPlayerEntity player) {
        return Permissions.check(player, permission, 3);
    }
}