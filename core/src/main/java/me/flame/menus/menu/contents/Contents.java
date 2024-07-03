package me.flame.menus.menu.contents;

import com.google.common.collect.ImmutableList;

import me.flame.lotte.LinkedConcurrentCache;
import me.flame.menus.builders.menus.ContentsBuilder;
import me.flame.menus.util.Option;
import me.flame.menus.items.MenuItem;
import me.flame.menus.menu.Menu;

import org.bukkit.inventory.Inventory;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public final class Contents implements BukkitContents {
    Map<Integer, MenuItem> items;
    private final Menu menu;

    private final Map<Integer, Supplier<MenuItem>> refreshableItems = new HashMap<>(9);

    public Contents(Menu menu) {
        Objects.requireNonNull(menu);
        this.menu = menu;
        this.items = new LinkedHashMap<>(menu.size());
    }

    public Contents(Menu menu, boolean concurrent) {
        Objects.requireNonNull(menu);
        this.menu = menu;
        this.items = concurrent ? new LinkedConcurrentCache<>(menu.size()) : new LinkedHashMap<>(menu.size());
    }

    @Contract("_ -> new")
    public static @NotNull ContentsBuilder builder(Menu menu) {
        return new ContentsBuilder(menu);
    }

    @Contract("_, _ -> new")
    public static @NotNull ContentsBuilder builder(Menu menu, boolean concurrent) {
        return new ContentsBuilder(menu, concurrent);
    }

    @Contract(pure = true)
    public @Unmodifiable Map<Integer, MenuItem> getItems() {
        return Map.copyOf(items);
    }

    public Map<Integer, MenuItem> getMutableItems() {
        return items;
    }

    @Override
    public void refreshItem(final int index) {
        MenuItem item = refreshableItems.get(index).get();
        this.items.put(index, item);
        this.menu.getInventory().setItem(index, item.getItemStack());
    }

    @Override
    public void refreshItem(final int index, final Supplier<MenuItem> item) {
        items.put(index, item.get());
    }

    @Override
    public Stream<MenuItem> stream() {
        return items.values().stream();
    }

    @Override
    public int itemCount() {
        return items.size();
    }

    @Override
    public int firstEmptySlot(final int startingPoint) {
        final int size = menu.size();
        for (int index = startingPoint; index < size; index++) {
            if (!this.items.containsKey(index)) return index;
        }
        return -1;
    }

    @Override
    public void addRefreshableItem(final int index, final Supplier<MenuItem> item) {
        refreshableItems.put(index, item);
        this.items.put(index, item.get());
    }

    @Override
    public void removeRefreshableItem(final int index) {
        refreshableItems.remove(index);
        this.items.remove(index);
    }

    public int addItem(final @NotNull MenuItem... items) {
        return addItem(new ArrayList<>(items.length), items);
    }

    public int addItem(final List<MenuItem> toAdd, @NotNull final MenuItem... items) {
        return this.addItem(toAdd, 0, items.length, items);
    }

    public int addItem(final List<MenuItem> toAdd, int fromIndex, @NotNull final MenuItem... items) {
        return this.addItem(toAdd, fromIndex, items.length, items);
    }

    public int addItem(final List<MenuItem> toAdd, int fromIndex, int toIndex, final MenuItem... items) {
        boolean changed = false;
        int itemsAdded = 0, size = menu.size();
        for (int itemIndex = 0, slot = this.firstEmptySlot(fromIndex); itemIndex < toIndex; itemIndex++) {
            MenuItem item = items[itemIndex];
            if (item == null) continue;

            slot = this.firstEmptySlot(slot);
            if (slot >= size) {
                if (menu.rows() == 6) return itemsAdded;
                List<MenuItem> unaddedItems = ImmutableList.copyOf(items);
                toAdd.addAll(itemIndex == 0 ? unaddedItems : unaddedItems.subList(itemIndex, size));
                break;
            }
            this.items.put(slot, items[itemIndex]);
            itemsAdded++;
        }
        checkSizing(toAdd, menu);
        return itemsAdded;
    }

    public int addItem(int fromIndex, @NotNull final MenuItem... items) {
        return this.addItem(new ArrayList<>(items.length), fromIndex, items.length, items);
    }

    public int addItem(int fromIndex, int toIndex, @NotNull final MenuItem... items) {
        return this.addItem(new ArrayList<>(items.length), fromIndex, toIndex, items);
    }

    public int addItem(@NotNull final List<MenuItem> items) {
        return addItem(items.toArray(new MenuItem[0]));
    }

    private void checkSizing(@NotNull List<MenuItem> toAdd, Menu menu) {
        if (!toAdd.isEmpty() && menu.canResize()) {
            menu.recreateInventory();
            this.addItem(toAdd.toArray(new MenuItem[0]));
        }
    }

    public void replaceContents(MenuItem @NotNull ... items) {
        int length = items.length;
        if (length % 9 != 0) throw new IllegalArgumentException("Length of items is not a multiple of 9");
        this.items.clear();
        for (int i = 0; i < length; i++) {
            MenuItem item = items[i];
            this.items.put(i, item);
        }
    }

    public void replaceContents(BukkitContents contents) {
        if (contents.size() % 9 != 0) throw new IllegalArgumentException("Length of items is not a multiple of 9");
        this.items = contents.getItems();
    }

    @Override
    public boolean removeItem(@NotNull final List<MenuItem> itemStacks) {
        return this.removeItem(itemStacks.toArray(new MenuItem[0]));
    }

    @Override
    public int rows() {
        return menu.rows();
    }

    @Override
    public int columns() {
        return menu.columns();
    }

    @Override
    public Menu getMenu() {
        return menu;
    }

    @Override
    public Set<Map.Entry<Integer, MenuItem>> getEntries() {
        return this.items.entrySet();
    }

    public void setItem(int slot, MenuItem item) {
        items.put(slot, item);
    }

    public Optional<MenuItem> getItem(int index) {
        return Optional.ofNullable(items.get(index));
    }

    public void forEach(Consumer<? super MenuItem> action) {
        for (var item : items.entrySet()) action.accept(item.getValue());
    }

    public void indexed(ObjIntConsumer<? super MenuItem> action) {
        for (var item : items.entrySet()) action.accept(item.getValue(), item.getKey());
    }

    public Optional<MenuItem> findFirst(Predicate<MenuItem> action) {
        for (var item : items.entrySet()) if (action.test(item.getValue())) return Optional.of(item.getValue());
        return Optional.empty();
    }

    public MenuItem removeItem(int index) {
        return items.remove(index);
    }

    public boolean hasItem(int slot) {
        return items.containsKey(slot);
    }

    public boolean removeItem(MenuItem... abandonedItems) {
        Set<MenuItem> items = Set.of(abandonedItems);

        boolean changed = false;
        for (Map.Entry<Integer, MenuItem> entries : this.items.entrySet()) {
            int index = entries.getKey();
            if (!items.contains(entries.getValue())) continue;
            this.items.remove(index);
            this.menu.getInventory().setItem(index, null);
            changed = true;
        }
        return changed;
    }

    @Override
    public boolean isConcurrent() {
        return false;
    }

    public void recreateItems(Inventory inventory) {
        final int size = this.menu.size();
        for (Map.Entry<Integer, MenuItem> entry : this.items.entrySet()) {
            int itemIndex = entry.getKey();
            MenuItem button = entry.getValue();
            if (button == null || (button.getVisiblity() != null && !button.getVisiblity().test(menu))) {
                inventory.setItem(itemIndex, null);
                continue;
            }

            Supplier<MenuItem> refreshableItem = this.refreshableItems.get(itemIndex);
            if (refreshableItem != null) inventory.setItem(itemIndex, refreshableItem.get().getItemStack());
            else inventory.setItem(itemIndex, button.getItemStack());
        }
    }

    @Override
    public void clear() {
        items.clear();
    }

    public int size() {
        return menu.size();
    }

    /*
    Private methods
     */

    enum AddResult { SUCCESSFUL, RESIZED, FAILED }

    @SuppressWarnings("MethodCallInLoopCondition") // only for those with the optimization inspections.
    AddResult add(int slot, int itemIndex, @NotNull final MenuItem guiItem, @NotNull final List<MenuItem> notAddedItems, MenuItem... loopingOver) {
        int size = menu.size();
        if (slot >= size) {
            if (menu.rows() == 6) return AddResult.FAILED;
            List<MenuItem> unaddedItems = ImmutableList.copyOf(loopingOver);
            notAddedItems.addAll(itemIndex == 0 ? unaddedItems : unaddedItems.subList(itemIndex, size));
            return AddResult.RESIZED;
        }
        items.put(slot, guiItem);
        return AddResult.SUCCESSFUL;
    }
}
