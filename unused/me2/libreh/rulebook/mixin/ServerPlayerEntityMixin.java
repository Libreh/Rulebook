package me.libreh.rulebook.mixin;

import com.mojang.authlib.GameProfile;
import eu.pb4.sgui.virtual.book.BookScreenHandler;
import me.libreh.rulebook.Rulebook;
import me.libreh.rulebook.config.ConfigManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

import static me.libreh.rulebook.Rulebook.joinedPlayers;
import static me.libreh.rulebook.Rulebook.rulebookPlayers;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    @Unique
    private final ServerPlayerEntity player = ((ServerPlayerEntity) (Object) this);

    @Unique
    private final UUID playerUuid = this.getUuid();

    @Unique
    private int rulebookTick;

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        rulebookTick++;
        if (rulebookTick == 4) {
            if (joinedPlayers.contains(playerUuid)) {
                if (!ConfigManager.hasAccepted(player)) {
                    if (!rulebookPlayers.contains(playerUuid)) {
                        if (!(player.currentScreenHandler instanceof BookScreenHandler)) {
                            rulebookPlayers.add(playerUuid);
                            Rulebook.openBookGui(player);
                        }
                    }
                } else {
                    joinedPlayers.remove(playerUuid);
                    rulebookPlayers.remove(playerUuid);
                    player.closeHandledScreen();
                }
            }

            rulebookTick = 0;
        }
    }
}
