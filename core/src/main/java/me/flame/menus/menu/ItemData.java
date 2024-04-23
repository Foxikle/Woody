package me.flame.menus.menu;

import com.google.common.collect.ImmutableSet;
import me.flame.menus.items.MenuItem;
import me.flame.menus.util.Option;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;

/**
 * Represents the data items of a menu
 * <p>
 * Improves the DRY principle by allowing you to add items the same way in {@link Menu} and {@link PaginatedMenu} and others.
 * <p>
 * Contains MenuItem[] and the base menu
 *
 * @since 2.0.0
 */
@SuppressWarnings("UnusedReturnValue")
public class ItemData {
    private int size;
    private final Menu menu;
    private MenuItem[] items;

    public ItemData(@NotNull final Menu menu) {
        this.menu = menu;
        this.size = menu.size;
        this.items = new MenuItem[size];

        // check for items that should be on every page.
        if (menu instanceof PaginatedMenu) ((PaginatedMenu) menu).setPageItems(this);
    }

    public ItemData(@NotNull final ItemData menu) {
        this.menu = menu.menu;
        this.size = menu.size;
        this.items = menu.items;
    }

    public MenuItem[] getItems() {
        return Arrays.copyOf(items, items.length);
    }

    public boolean addItem(@NotNull final ItemStack... items) {
        final List<MenuItem> notAddedItems = new ArrayList<>(items.length);

        int slot = 0;
        boolean changed = false;
        for (final ItemStack item : items) {
            MenuItem menuItem = MenuItem.of(item);
            if (this.add(slot, menuItem, notAddedItems) == -1) return changed;
            changed = true;
            slot++;
        }

        checkSizing(notAddedItems);
        return changed;
    }

    /**
     *
     * @param slot the slot
     * @param guiItem the item
     * @param notAddedItems the list of items to add to, if the item wasn't added
     * @return 0 for successful addition, -1 to indicate that the menu can't be resized and 1 to indicate it is in resizing process
     */
    private int add(int slot, @NotNull final MenuItem guiItem, @NotNull final List<MenuItem> notAddedItems) {
        int length = items.length;
        while (slot < length && items[slot] != null) slot++;
        if (slot >= length) { // if the slot is out of bounds
            if (menu.rows == 6) return -1;
            notAddedItems.add(guiItem);
            return 1;
        }
        items[slot] = guiItem;
        return 0;
    }

    public boolean addItem(@NotNull final MenuItem... items) {
        return addItem(new ArrayList<>(items.length), items);
    }

    public boolean addItem(final List<MenuItem> toAdd, @NotNull final MenuItem... items) {
        int slot = 0;
        boolean changed = false;
        boolean skip = false;
        for (final MenuItem item : items) {
            if (skip) {
                toAdd.add(item);
                continue;
            }
            switch (add(slot, item, toAdd)) {
                case 0:
                    changed = true;
                    break;
                case 1:
                    skip = true;
                    break;
                default: return changed;
            }
            slot++;
        }

        checkSizing(toAdd);
        return changed;
    }


    public boolean addItem(@NotNull final List<MenuItem> items) {
        return addItem(items.toArray(new MenuItem[0]));
    }

    private void checkSizing(List<MenuItem> toAdd) {
        if (menu.dynamicSizing && !toAdd.isEmpty() && menu.type == MenuType.CHEST) {
            this.recreateInventory();
            this.addItem(toAdd.toArray(new MenuItem[0]));
            menu.update();
        }
    }

    void recreateInventory() {
        size = menu.size;
        items = Arrays.copyOf(items, size);
    }

    public void contents(MenuItem... items) {
        if (items.length % 9 != 0) throw new IllegalArgumentException("Length of items is not a multiple of 9");
        this.items = Arrays.copyOf(items, items.length);
        menu.update(true);
    }

    public void setItem(int slot, MenuItem item) {
        items[slot] = item;
    }

    public MenuItem getItem(int i) {
        return items[i];
    }

    public void forEach(Consumer<? super MenuItem> action) {
        for (MenuItem item : items) action.accept(item);
    }

    public void indexed(ObjIntConsumer<? super MenuItem> action) {
        for (int index = 0; index < items.length; index++) action.accept(items[index], index);
    }

    public Option<MenuItem> findFirst(Predicate<MenuItem> action) {
        for (MenuItem item : items) if (action.test(item)) return Option.some(item);
        return Option.none();
    }

    public MenuItem removeItem(int index) {
        MenuItem oldItem = items[index];
        items[index] = null;
        return oldItem;
    }

    public boolean hasItem(int slot) {
        return items[slot] != null;
    }

    public Option<MenuItem> get(int index) {
        return Option.some(items[index]);
    }

    public boolean removeItem(MenuItem... its) {
        Set<MenuItem> items = ImmutableSet.copyOf(its);

        boolean changed = false;
        for (int index = 0; index < size; index++) {
            MenuItem it = this.items[index];
            if (it == null) continue;
            if (!items.contains(it)) continue;
            this.items[index] = null;
            changed = true;
        }
        return changed;
    }

    public void recreateItems(Inventory inventory) {
        for (int itemIndex = 0; itemIndex < size; itemIndex++) {
            MenuItem item = items[itemIndex];
            if (item == null || !item.getVisiblity().test(menu)) {
                inventory.setItem(itemIndex, null);
                continue;
            }
            if (item.hasStates()) item.updateStates();
            inventory.setItem(itemIndex, item.getItemStack());
        }
    }

    public void updateItem(int slot, @NotNull ItemStack itemStack, MenuItem guiItem) {
        if (guiItem == null) {
            items[slot] = MenuItem.of(itemStack);
            return;
        }
        guiItem.setItemStack(itemStack);
        items[slot] = guiItem;
    }

    public boolean removeItem(ItemStack... items) {
        Set<ItemStack> itemStacks = ImmutableSet.copyOf(items);
        boolean changed = false;
        for (int i = 0; i < size; i++) {
            MenuItem it = this.items[i];
            if (it == null) continue;
            if (!itemStacks.contains(it.getItemStack())) continue; 
            this.items[i] = null;
            changed = true;
        }
        return changed;
    }

    public int size() {
        return size;
    }
}
