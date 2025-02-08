package me.libreh.rulebook.gui;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.TextParserUtils;
import eu.pb4.sgui.api.elements.BookElementBuilder;
import eu.pb4.sgui.api.gui.BookGui;
import me.libreh.rulebook.config.Config;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;

import static me.libreh.rulebook.Rulebook.JOIN_LIST;
import static me.libreh.rulebook.Rulebook.RULEBOOK_LIST;

public class RulebookGui extends BookGui {
    public RulebookGui(ServerPlayerEntity player, BookElementBuilder book) {
        super(player, book);
    }

    private final HashMap<Integer, Boolean> VIEWED_PAGES = new HashMap<>();

    @Override
    public void onTakeBookButton() {
        super.onTakeBookButton();

        acceptIfViewedAll();
        player.closeHandledScreen();
    }

    @Override
    public void onTick() {
        super.onTick();

        if (!VIEWED_PAGES.containsKey(page)) {
            VIEWED_PAGES.put(page, true);
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
        if (VIEWED_PAGES.size() == book.get(DataComponentTypes.WRITTEN_BOOK_CONTENT).getPages(false).size()) {
            Config.accept(player);
        } else {
            var playerUuid = player.getUuid();
            JOIN_LIST.remove(playerUuid);
            RULEBOOK_LIST.remove(playerUuid);
            player.networkHandler.disconnect(Placeholders.parseText(TextParserUtils.formatText(Config.getConfig().kickMessages.didntRead), PlaceholderContext.of(player)));
        }
    }
}
