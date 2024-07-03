package me.flame.menus.menu.pagination;

import com.google.common.collect.ImmutableSet;

import me.flame.menus.menu.contents.BukkitContents;
import me.flame.menus.items.MenuItem;
import me.flame.menus.menu.Menu;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({ "unused" })
public interface Pagination<T> extends Menu {
    default void setPageItems(BukkitContents items) {
        for (Map.Entry<Integer, MenuItem> entry : pageItems().entrySet()) items.setItem(entry.getKey(), entry.getValue());
    }

    default void addPageItems(MenuItem... items) {
        for (BukkitContents page : pages()) page.addItem(items);
    }

    List<BukkitContents> pages();

    void setPage(T index, BukkitContents page);

    BukkitContents getPage(T key);

    Class<T> indexClass();

    default void removePageItem(int slot) {
        for (BukkitContents page : pages()) page.removeItem(slot);
    }

    default void removePageItem(MenuItem slot) {
        for (BukkitContents page : pages()) page.removeItem(slot);
    }

    default void removePageItem(MenuItem... slot) {
        Set<MenuItem> set = ImmutableSet.copyOf(slot);
        for (BukkitContents page : pages()) page.removeItem(slot);
    }

    default void setPageItem(int[] slots, MenuItem @NotNull [] items) {
        int length = slots.length;

        if (length != items.length) throw new IllegalArgumentException("Number of slots and number of items must be equal.");
        for (BukkitContents page : pages()) {
            for (int i = 0; i < length; i++) {
                page.setItem(slots[i], items[i]);
                this.pageItems().put(slots[i], items[i]);
            }
        }
    }

    @ApiStatus.Internal
    Map<Integer, MenuItem> pageItems();

    default void setPageItem(int slot, MenuItem item) {
        this.pageItems().put(slot, item);
        for (BukkitContents page : pages()) page.setItem(slot, item);
    }

    default void setPageItem(int[] slots, MenuItem item) {
        for (BukkitContents page : pages()) {
            for (int slot : slots) {
                page.setItem(slot, item);
                this.pageItems().put(slot, item);
            }
        }
    }

    /**
     * Goes to the specified page
     *
     * @return False if there is no next page.
     */
    boolean page(T key);

    T getPageIdentifier();

    /**
     * Gets the number of pages the GUI has
     *
     * @return The number of pages
     */
    int getPagesSize();

    /**
     * Opens the GUI to a specific page for the given player
     *
     * @param player   The player to open the GUI to
     * @param openPage The specific page to open at
     */
    void open(@NotNull final HumanEntity player, final T openPage);
}
