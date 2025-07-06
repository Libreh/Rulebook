package me.libreh.rulebook.mixin;

import com.mojang.authlib.GameProfile;
import eu.pb4.sgui.virtual.book.BookScreenHandler;
import me.libreh.rulebook.RulebookMod;
import me.libreh.rulebook.services.PlayerService;
import me.libreh.rulebook.util.Utils;
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


@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    @Unique
    private static final int TICK_INTERVAL = 4;
    
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
    private void onTick(CallbackInfo ci) {
        rulebookTick++;
        
        if (rulebookTick >= TICK_INTERVAL) {
            handleRulebookLogic();
            rulebookTick = 0;
        }
    }

    @Unique
    private void handleRulebookLogic() {
        var playerService = RulebookMod.getPlayerService();

        if (!playerService.hasPlayerJoined(playerUuid)) {
            return;
        }

        if (Utils.hasAccepted(player)) {
            handleAcceptedPlayer(playerService);
            return;
        }

        if (playerService.isPlayerViewingRulebook(playerUuid)) {
            return;
        }

        if (player.currentScreenHandler instanceof BookScreenHandler) {
            return;
        }

        showRulebookToPlayer(playerService);
    }

    @Unique
    private void handleAcceptedPlayer(PlayerService playerService) {
        playerService.removePlayer(playerUuid);
        player.closeHandledScreen();
        RulebookMod.LOGGER.debug("Player {} has already accepted rules, removing from tracking",
                player.getName().getString());
    }

    @Unique
    private void showRulebookToPlayer(PlayerService playerService) {
        playerService.addRulebookPlayer(playerUuid);
        Utils.openBookGui(player, true);
        RulebookMod.LOGGER.debug("Showing rulebook to player {}", player.getName().getString());
    }
}
