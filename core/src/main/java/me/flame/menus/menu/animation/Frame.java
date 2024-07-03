package me.flame.menus.menu.animation;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import me.flame.menus.items.MenuItem;
import me.flame.menus.menu.Menu;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * To be used in building Frames.
 * @since 2.0.0
 */
@SuppressWarnings({ "unused"})
public class Frame {
    private final Map<Integer, MenuItem> items;
    private Map<Integer, MenuItem> defaultItems;

    public boolean hasStarted() {
        return started;
    }

    private boolean started = false;

    @Contract(pure = true)
    public Frame(Map<Integer, MenuItem> items) {
        Objects.requireNonNull(items);
        this.items = new LinkedHashMap<>(items);
        this.defaultItems = new LinkedHashMap<>(items.size());
    }

    @NotNull
    @CanIgnoreReturnValue
    public Frame start(Menu menu) {
        if (!started) {
            this.defaultItems = items;
            started = true;
        }
        processItems(menu, items);
        return this;
    }

    public void reset(Menu menu) {
        processItems(menu, defaultItems);
    }

    private static void processItems(Menu menu, Map<Integer, MenuItem> items) {
        final int size = menu.size();
        for (Map.Entry<Integer, MenuItem> item : items.entrySet()) {
            menu.setItem(item.getKey(), item.getValue());
        }
    }

    public List<MenuItem> getItems() {
        return List.copyOf(items.values());
    }

    public List<MenuItem> getDefaultItems() {
        return List.copyOf(defaultItems.values());
    }

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static Frame.Builder builder(int menuLength) {
        if (menuLength % 9 != 0)
            throw new IllegalArgumentException(
                    "Length specified but with improper value; must be a multiple of 9, for example:"
                    + "\n9, 18, 27, 36, 45 and 54 only"
                    + "\nLength specified: " + menuLength);
        if (menuLength < 0)
            throw new IllegalArgumentException("Negative menu length detected provided. \nLength: " + menuLength);
        return new Builder(menuLength);
    }

    public static class Builder {
        private final Map<Integer, MenuItem> items;

        @Contract(pure = true)
        Builder(int menuLength) {
            this.items = new LinkedHashMap<>(menuLength);
        }

        @NotNull
        public Builder setItem(int slot, MenuItem item) {
            items.put(slot, item);
            return this;
        }

        @NotNull
        public Frame build() {
            return new Frame(this.items);
        }
    }
}
