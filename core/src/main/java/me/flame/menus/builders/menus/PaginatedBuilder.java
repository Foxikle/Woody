package me.flame.menus.builders.menus;

import me.flame.menus.items.MenuItem;
import me.flame.menus.menu.MenuType;
import me.flame.menus.menu.PaginatedMenu;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class PaginatedBuilder extends MenuBuilder {
    protected int pages = 2, nextItemSlot = -1, previousItemSlot = -1;

    private MenuItem nextItem, previousItem;

    public PaginatedBuilder(int rows) {
        super(rows);
    }

    public PaginatedBuilder() {}

    /**
     * Set the number of pages for the paginated builder.
     *
     * @param  pages  the number of pages to set
     * @return        the builder for chaining
     */
    @NotNull
    public MenuBuilder pages(final int pages) {
        this.pages = pages;
        return this;
    }

    public MenuBuilder nextPageItem(int nextItemSlot, MenuItem nextItem) {
        this.nextItemSlot = nextItemSlot;
        this.nextItem = nextItem;
        return this;
    }

    public MenuBuilder previousPageItem(int previousItemSlot, MenuItem previousItem) {
        this.previousItemSlot = previousItemSlot;
        this.previousItem = previousItem;
        return this;
    }

    @NotNull
    @Contract(" -> new")
    public PaginatedMenu build() {
        checkRequirements(rows, title);
        checkPaginatedRequirements(pages, nextItemSlot, previousItemSlot, nextItem, previousItem);
        return type == MenuType.CHEST
                ? new PaginatedMenu(rows, pages, title, modifiers, nextItem, previousItem, previousItemSlot, nextItemSlot)
                : new PaginatedMenu(type, pages, title, modifiers, nextItem, previousItem, previousItemSlot, nextItemSlot);
    }

    private static void checkPaginatedRequirements(int pages, int next, int previous, MenuItem nextItem, MenuItem previousItem) {
        if (pages < 1)
            throw new IllegalArgumentException("Pages must be more than 1" + "Pages: " + pages + "\nFix: Pages must be more than 1");

        if (next == -1 || previous == -1 || nextItem == null || previousItem == null) {
            throw new IllegalArgumentException(
                "Next and previous item slots and items must not be null/-1" +
                "\nNext equals null: " + (nextItem == null) + "\nPrevious equals null: " + (previousItem == null) +
                "\nNext Item Slot: " + next + "\nPrevious Item Slot: " + previous +
                "\nFix: The items and item slots must be set."
            );
        }
    }
}
