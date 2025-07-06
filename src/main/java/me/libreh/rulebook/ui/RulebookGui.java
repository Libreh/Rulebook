package me.libreh.rulebook.ui;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.sgui.api.elements.BookElementBuilder;
import eu.pb4.sgui.api.gui.BookGui;
import me.libreh.rulebook.RulebookMod;
import me.libreh.rulebook.config.ConfigManager;
import me.libreh.rulebook.services.PlayerService;
import me.libreh.rulebook.util.Utils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;

public final class RulebookGui extends BookGui {
    private final boolean shouldKickOnIncomplete;
    private final Map<Integer, Boolean> viewedPages;
    private final PlayerService playerService;

    public RulebookGui(ServerPlayerEntity player, BookElementBuilder book, boolean shouldKickOnIncomplete) {
        super(player, book);
        this.shouldKickOnIncomplete = shouldKickOnIncomplete;
        this.viewedPages = new HashMap<>();
        this.playerService = RulebookMod.getPlayerService();
    }
    
    @Override
    public void onTakeBookButton() {
        super.onTakeBookButton();


        handleBookAcceptance();
        if (hasViewedAllPages()) {
            giveRulebookToPlayer();
        }
        closeGui();
    }
    
    @Override
    public void onTick() {
        super.onTick();
        trackCurrentPage();
    }
    
    @Override
    public void close(boolean screenHandlerIsClosed) {
        handleBookAcceptance();
        
        if (this.isOpen() && !this.reOpen) {
            this.reOpen = false;
            
            if (!screenHandlerIsClosed && this.player.currentScreenHandler == this.screenHandler) {
                this.player.closeHandledScreen();
            }
            
            this.onClose();
        } else {
            this.reOpen = false;
        }
    }

    private void handleBookAcceptance() {
        if (hasViewedAllPages()) {
            acceptPlayer();
        } else if (shouldKickOnIncomplete) {
            kickPlayerForIncompleteReading();
        }
    }

    private boolean hasViewedAllPages() {
        var data = book.get(DataComponentTypes.WRITTEN_BOOK_CONTENT);
        if (data == null) {
            RulebookMod.LOGGER.warn("Book content is null for player {}", player.getName().getString());
            return false;
        }
        
        var pages = data.getPages(false);
        return viewedPages.size() >= pages.size();
    }

    private void acceptPlayer() {
        Utils.accept(player);
        var playerUuid = player.getUuid();

        playerService.removePlayer(playerUuid);
        
        RulebookMod.LOGGER.info("Player {} accepted the rules", player.getName().getString());
    }

    private void kickPlayerForIncompleteReading() {
        var playerUuid = player.getUuid();
        var config = ConfigManager.getInstance().getConfig();

        playerService.removePlayer(playerUuid);

        var kickMessage = Placeholders.parseText(
                RulebookMod.PARSER.parseNode(config.getKickMessages().getDidntRead()),
                PlaceholderContext.of(player));
        
        player.networkHandler.disconnect(kickMessage);
        RulebookMod.LOGGER.info("Player {} kicked for not reading all rules", player.getName().getString());
    }

    private void giveRulebookToPlayer() {
        ItemStack rulebook = Utils.getRulebookStack(player);
        player.giveItemStack(rulebook);
        RulebookMod.LOGGER.debug("Gave rulebook to player {}", player.getName().getString());
    }

    private void closeGui() {
        player.closeHandledScreen();
    }

    private void trackCurrentPage() {
        viewedPages.put(page, true);
    }
}
