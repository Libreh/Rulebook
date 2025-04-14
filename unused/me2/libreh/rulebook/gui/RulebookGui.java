package me.libreh.rulebook.gui;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.sgui.api.elements.BookElementBuilder;
import eu.pb4.sgui.api.gui.BookGui;
import me.libreh.rulebook.Rulebook;
import me.libreh.rulebook.config.ConfigManager;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;

import static me.libreh.rulebook.Rulebook.joinedPlayers;
import static me.libreh.rulebook.Rulebook.rulebookPlayers;

public class RulebookGui extends BookGui {
    private final boolean kick;

    public RulebookGui(ServerPlayerEntity player, BookElementBuilder book, boolean kick) {
        super(player, book);
        this.kick = kick;
    }

    private final HashMap<Integer, Boolean> viewedPages = new HashMap<>();

    @Override
    public void onTakeBookButton() {
        super.onTakeBookButton();

        acceptIfViewedAll();
        player.closeHandledScreen();
    }

    @Override
    public void onTick() {
        super.onTick();

        if (!viewedPages.containsKey(page)) {
            viewedPages.put(page, true);
        }
    }

    @Override
    public void close(boolean screenHandlerIsClosed) {
        acceptIfViewedAll();

        if (this.isOpen() && !this.reOpen) {
            //noinspection removal
            this.open = this.isOpen();
            this.reOpen = false;

            if (!screenHandlerIsClosed && this.player.currentScreenHandler == this.screenHandler) {
                this.player.closeHandledScreen();
            }

            this.onClose();
        } else {
            this.reOpen = false;
        }
    }

    private void acceptIfViewedAll() {
        if (viewedPages.size() == book.get(DataComponentTypes.WRITTEN_BOOK_CONTENT).getPages(false).size()) {
            ConfigManager.accept(player);
        } else {
            if (kick) {
                var playerUuid = player.getUuid();
                joinedPlayers.remove(playerUuid);
                rulebookPlayers.remove(playerUuid);
                player.networkHandler.disconnect(Placeholders.parseText(Rulebook.PARSER.parseNode(ConfigManager.getConfig().kickMessages.didntRead), PlaceholderContext.of(player)));
            }
        }
    }
}
