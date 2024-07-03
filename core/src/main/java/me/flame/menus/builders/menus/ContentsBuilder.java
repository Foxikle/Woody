package me.flame.menus.builders.menus;

import me.flame.menus.menu.MenuLayout;
import me.flame.menus.menu.Slot;
import me.flame.menus.patterns.IterationPattern;
import me.flame.menus.items.MenuItem;
import me.flame.menus.menu.contents.BukkitContents;
import me.flame.menus.menu.contents.Contents;
import me.flame.menus.menu.Menu;
import me.flame.menus.menu.iterator.IterationDirection;
import me.flame.menus.menu.iterator.MenuIterator;
import org.jetbrains.annotations.NotNull;

/**
 * A modern woody builder to build contents.
 * @since 3.0.0
 */
@SuppressWarnings("unused")
public class ContentsBuilder {
    private final BukkitContents contents;
    private final Menu menu;

    public ContentsBuilder(final Menu menu) {
        this(menu, false);
    }

    public ContentsBuilder(final Menu menu, final boolean concurrent) {
        this.contents = new Contents(menu, concurrent);
        this.menu = menu;
    }

    public ContentsBuilder iterate(@NotNull MenuIterator iterator, IterationResponse response) {
        if (iterator.getDirection() == IterationDirection.HORIZONTAL) {
            // optimize iteration because it's horizontal
            return this.iterate(response);
        }
        while (iterator.hasNext()) {
            Slot slot = iterator.nextSlot();
            response.execute(contents, slot);
        }
        return this;
    }

    public ContentsBuilder iterate(IterationDirection direction, IterationResponse response) {
        if (direction == IterationDirection.HORIZONTAL) return this.iterate(response);
        return this.iterate(new MenuIterator(direction, menu), response);
    }

    public ContentsBuilder iterate(IterationResponse response) {
        int size = contents.size();
        for (int index = 0; index < size; index++) response.execute(contents, new Slot(index));
        return this;
    }

    public ContentsBuilder set(Class<IterationPattern> patternClass, MenuItem item) {
        MenuIterator iterator = new MenuIterator(0, 0, patternClass, menu);
        return this.iterate(iterator, (contents, slot) -> contents.setItem(slot.slot(), item));
    }

    public ContentsBuilder layout(MenuLayout layout) {
        return this.iterate((contents, slot) -> contents.setItem(slot, layout.getMappedItem(slot.row(), slot.column())));
    }

    public BukkitContents create() {
        return contents;
    }

    @FunctionalInterface
    public interface IterationResponse {
        void execute(BukkitContents contents, Slot slot);
    }
}
