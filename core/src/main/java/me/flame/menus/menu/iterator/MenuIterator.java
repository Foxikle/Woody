package me.flame.menus.menu.iterator;

import me.flame.menus.patterns.IterationPattern;
import me.flame.menus.items.MenuItem;
import me.flame.menus.menu.Slot;
import me.flame.menus.menu.Menu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@SuppressWarnings("unused")
public final class MenuIterator implements Iterator<MenuItem> {
    @NotNull
    private Slot position;

    @NotNull
    private final IterationPattern direction;

    @NotNull
    private final Menu menu;

    private Slot next;

    private final int rows;

    private final Iterator<Map.Entry<Integer, MenuItem>> entries;

    private static final String NOTHING_MORE_NEXT =
            "Used MenuIterator#next() but nothing more" +
                    "\nFix: Use hasNext() beforehand to avoid this error.";

    private static final String NOTHING_MORE_NEXT_OPTIONAL =
            "Used MenuIterator#nextOptional() but nothing more" +
                    "\nFix: Use hasNext() beforehand to avoid this error.";

    private static final String NOTHING_MORE_NEXT_NOT_NULL =
            "Used MenuIterator#nextNotNull() but no non-null value was found";

    private static final String GREATER_THAN_ONE_ONLY =
            "Starting row and column must be 1 or greater only." +
                    "\nFix: If you're using an algorithm for rows/cols, you might wanna check it";

    @SuppressWarnings("DataFlowIssue")
    public MenuIterator(int startingRow, int startingCol,
                        @Nullable IterationPattern direction,
                        @NotNull Menu menu) {
        Slot prepos = new Slot(startingRow, startingCol);
        this.menu = menu;
        this.next = prepos;
        this.position = prepos;

        this.direction = direction; // this won't be used anyway if it's null.

        this.entries = direction == null || direction == IterationDirection.HORIZONTAL ? menu.getEntries().iterator() : null;

        this.rows = menu.rows();
    }

    public MenuIterator(@NotNull IterationPattern direction, @NotNull Menu menu) {
        this(0, 0, direction, menu);
    }

    public MenuIterator(int startingRow, int startingCol,
                        @NotNull Class<IterationPattern> direction,
                        @NotNull Menu menu) {
        this(startingRow, startingCol, menu.manager().getPattern(direction), menu);
    }

    public MenuIterator(@NotNull Class<IterationPattern> direction, @NotNull Menu menu) {
        this(0, 0, direction, menu);
    }

    /**
     * Retrieves the next slot in the menu.
     * @return           the next empty slot in the menu, or null if no empty slot is found
     */
    public @Nullable Slot nextSlot() {
        position = next;
        next = null;
        return position;
    }

    @Override
    public boolean hasNext() {
        if (entries != null && entries.hasNext()) return true;
        if (next != null) return true;
        next = direction.shift(position, this.rows, this.menu.columns());
        return next.slot() != -1;
    }

    @Override
    public MenuItem next() {
        if (entries != null) {
            //Map.Entry<Integer, MenuItem> entry = entries.next();
            return entries.next().getValue();
        }
        Slot slot = nextSlot();
        if (slot != null) return menu.getItem(slot).orElse(null);
        throw new NoSuchElementException(NOTHING_MORE_NEXT);
    }

    /**
     * Retrieves the next non-null MenuItem in the menu.
     *
     * @return the next non-null MenuItem in the menu
     */
    public @NotNull Optional<MenuItem> nextNotNull() {
        if (entries != null) return Optional.ofNullable(entries.next().getValue());
        while (hasNext()) {
            position = next;
            next = direction.shift(position, rows, menu.columns());
            Optional<MenuItem> item = menu.getItem(position);
            if (item.isPresent()) return item;
        }

        /*throw new NoSuchElementException(NOTHING_MORE_NEXT_NOT_NULL +
                "\nFix: Everything after slot " + position + " is empty/null."
        );*/
        return Optional.empty();
    }

    public @NotNull IterationPattern getDirection() {
        return direction;
    }
}