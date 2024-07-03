package me.flame.menus.menu.pagination;

import me.flame.menus.events.PageChangeEvent;
import me.flame.menus.items.MenuItem;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.ApiStatus;

public interface IndexedPagination extends Pagination<Integer> {
    int getCurrentPageNumber();

    boolean isLastPage();

    boolean isFirstPage();

    /**
     * Goes to the next page
     * @return False if there is no next page.
     */
    boolean next();

    /**
     * Goes to the previous page if possible
     * @return False if there is no previous page.
     */
    boolean previous();

    void setNextPageItem(int nextItemSlot, MenuItem nextItem);

    void setPreviousPageItem(int previousItemSlot, MenuItem previousItem);

    @ApiStatus.Internal
    boolean pageChangingAction(int clickedSlot);

    @ApiStatus.Internal
    PageChangeEvent createPageEvent(final InventoryClickEvent event);
}
