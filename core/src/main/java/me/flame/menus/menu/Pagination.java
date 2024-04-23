package me.flame.menus.menu;

import com.google.common.collect.ImmutableSet;

import me.flame.menus.items.MenuItem;
import me.flame.menus.util.Option;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
public interface Pagination extends IMenu {
    public List<ItemData> pages();

    /**
     * Goes to the next page
     *
     * @return False if there is no next page.
     */
    boolean next();

    /**
     * Goes to the previous page if possible
     *
     * @return False if there is no previous page.
     */
    boolean previous();

    /**
     * Goes to the specified page
     *
     * @return False if there is no next page.
     */
    boolean page(int pageNum);

    Option<ItemData> getOptionalPage(int index);

    default void addPageItems(MenuItem... items) {
        for (ItemData page : pages()) page.addItem(items);
    }

    default void addPageItems(ItemStack... items) {
        for (ItemData page : pages()) page.addItem(items);
    }

    default void removePageItem(int slot) {
        for (ItemData page : pages()) page.removeItem(slot);
    }

    default void removePageItem(ItemStack slot) {
        for (ItemData page : pages()) page.removeItem(slot);
    }

    default void removePageItem(MenuItem slot) {
        for (ItemData page : pages()) page.removeItem(slot);
    }

    default void removePageItem(ItemStack... slot) {
        Set<ItemStack> set = ImmutableSet.copyOf(slot);
        for (ItemData page : pages()) page.removeItem(slot);
    }

    default void removePageItem(MenuItem... slot) {
        Set<MenuItem> set = ImmutableSet.copyOf(slot);
        for (ItemData page : pages()) page.removeItem(slot);
    }

    default void setPageItem(int[] slots, MenuItem[] items) {
        if (slots.length != items.length) throw new IllegalArgumentException("Number of slots and number of items must be equal.");
        for (ItemData page : pages()) {
            for (int i = 0; i < slots.length; i++) {
                page.setItem(slots[i], items[i]);
                this.pageItems().put(slots[i], items[i]);
            }
        }
    }

    @ApiStatus.Internal
    Map<Integer, MenuItem> pageItems();

    default void setPageItem(int slot, ItemStack item) {
        setPageItem(slot, MenuItem.of(item));
    }

    default void setPageItem(int slot, MenuItem item) {
        this.pageItems().put(slot, item);
        for (ItemData page : pages()) page.setItem(slot, item);
    }

    default void setPageItem(int[] slots, MenuItem item) {
        int size = slots.length;
        for (ItemData page : pages()) {
            for (int slot : slots) {
                page.setItem(slot, item);
                this.pageItems().put(slot, item);
            }
        }
    }

    /**
     * Gets the current page number
     *
     * @return The current page number
     */
    int getCurrentPageNumber();

    /**
     * Gets the number of pages the GUI has
     *
     * @return The number of pages
     */
    int getPagesSize();
}
