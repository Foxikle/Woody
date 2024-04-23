package me.flame.menus.menu.animation;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import lombok.Getter;
import me.flame.menus.items.MenuItem;
import me.flame.menus.menu.IMenu;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * To be used in building Frames.
 * @since 2.0.0
 */
@SuppressWarnings("unused")
public class Frame {
    private final MenuItem[] items;
    private MenuItem[] defaultItems;

    @Getter
    private boolean started = false;

    @Contract(pure = true)
    public Frame(MenuItem[] items) {
        this.items = items;
        this.defaultItems = new MenuItem[items.length];
    }

    @NotNull
    @CanIgnoreReturnValue
    public Frame start(IMenu menu) {
        if (!started) {
            this.defaultItems = items;
            started = true;
        }
        processItems(menu, items);
        return this;
    }

    public void reset(IMenu menu) {
        processItems(menu, defaultItems);
    }

    private static void processItems(IMenu menu, MenuItem[] items) {
        for (int itemIndex = 0; itemIndex < menu.size(); itemIndex++) {
            MenuItem item = items[itemIndex];
            if (item != null) menu.updateItem(itemIndex, item.getItemStack());
        }
    }

    public List<MenuItem> getItems() {
        return List.of(items);
    }

    public List<MenuItem> getDefaultItems() {
        return List.of(defaultItems);
    }

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static Frame.Builder builder(int menuLength) {
        if (menuLength % 9 != 0)
            throw new IllegalArgumentException(
                    "Length specified but with improper value; must be a multiple of 9, for example:"
                    + "\n9, 18, 27, 36, 45 and 54 only"
                    + "Length specified: " + menuLength);
        return new Builder(menuLength);
    }

    public static class Builder {
        private final MenuItem[] items;

        @Contract(pure = true)
        Builder(int menuLength) {
            this.items = new MenuItem[54];
        }

        @NotNull
        public Builder setItem(int slot, MenuItem item) {
            items[slot] = item;
            return this;
        }

        @NotNull
        public Frame build() {
            return new Frame(this.items);
        }
    }
}
