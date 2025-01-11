package me.libreh.rulebook;

import eu.pb4.sgui.api.elements.BookElementBuilder;
import eu.pb4.sgui.api.gui.BookGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.server.network.ServerPlayerEntity;

public class RulebookGui extends BookGui {
    public RulebookGui(ServerPlayerEntity player, BookElementBuilder book) {
        super(player, book);
    }

    @Override
    public void onTakeBookButton() {
        if (book.get(DataComponentTypes.WRITTEN_BOOK_CONTENT).pages().size() == page + 1) {
            Commands.acceptRules(player);
        }
    }
}
