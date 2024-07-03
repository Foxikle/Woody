package me.flame.menus.menu.iterator;

import me.flame.menus.items.MenuItem;
import me.flame.menus.menu.Slot;
import me.flame.menus.menu.contents.BukkitContents;
import me.flame.menus.menu.Menu;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public class ObjectIterator<T> {
    private final List<T> mappedObjects;

    private Function<T, MenuItem> menuItemCreateFunction;

    private Predicate<T> filter = null;
    private Comparator<T> sorter = null;

    public void onEmpty(Consumer<BukkitContents> onEmptyItems) {
        this.onEmptyItems = onEmptyItems;
    }

    private Consumer<BukkitContents> onEmptyItems = null;

    private boolean applying = false;

    private int position, next;
    private final int size;

    private final Menu menu;

    public ObjectIterator(int startingRow, int startingCol, Function<T, MenuItem> menuItemCreateFunction, Menu menu) {
        this.menu = menu;
        this.size = menu.size();
        this.mappedObjects = new ArrayList<>(size);
        this.position = new Slot(startingRow, startingCol).slot();
    }

    public boolean hasNext() {
        return position < size;
    }

    public int next() {
        return position++;
    }

    public ObjectIterator<T> sort(Priority priority, Comparator<T> sorter) {
        this.sorter = sorter;
        return this;
    }

    public ObjectIterator<T> filter(Predicate<T> filter) {
        this.filter = filter;
        return this;
    }

    public ObjectIterator<T> add(Collection<T> elements) {
        this.mappedObjects.addAll(elements);
        return this;
    }

    public ObjectIterator<T> add(T element) {
        this.mappedObjects.add(element);
        return this;
    }

    public ObjectIterator<T> apply() {
        this.applying = true;
        List<T> filteredObjects = new ArrayList<>(this.mappedObjects);

        filteredObjects.removeIf((object) -> !filter.test(object));
        filteredObjects.sort(sorter);

        if (filteredObjects.isEmpty() && this.onEmptyItems != null) {
            this.onEmptyItems.accept(this.menu.contents());
            this.applying = false;
            return this;
        }

        for (T value : filteredObjects) {
            if (!hasNext()) break;
            MenuItem item = this.menuItemCreateFunction.apply(value);
            if (item == null) continue;
            this.menu.setItem(this.next(), item);
        }
        this.applying = false;
        return this;
    }

    public Function<T, MenuItem> getMenuItemCreateFunction() {
        return menuItemCreateFunction;
    }

    public boolean isApplying() {
        return applying;
    }
}
