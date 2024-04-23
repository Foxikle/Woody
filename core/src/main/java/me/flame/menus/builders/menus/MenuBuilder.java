package me.flame.menus.builders.menus;

import lombok.NonNull;
import me.flame.menus.adventure.TextHolder;
import me.flame.menus.items.MenuItem;
import me.flame.menus.menu.ItemData;
import me.flame.menus.menu.Menu;
import me.flame.menus.menu.MenuType;
import me.flame.menus.menu.PaginatedMenu;
import me.flame.menus.modifiers.Modifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.List;

/**
 * Universal menu builder for menus (Menu, PaginatedMenu).
 * @since 2.0.0
 * @author FlameyosFlow
 */
@SuppressWarnings("unused")
public class MenuBuilder {
    protected TextHolder title;

    @NotNull
    protected MenuType type = MenuType.CHEST;

    @NotNull
    protected final EnumSet<Modifier> modifiers = EnumSet.noneOf(Modifier.class);

    protected int rows = 1;

    public MenuBuilder(int rows) {
        checkRows(rows);
        this.rows = rows;
    }

    public MenuBuilder() {}

    /**
     * Sets the title of the menu.
     *
     * @param  title  the title to be set
     * @return        the builder for chaining
     */
    public MenuBuilder title(@NonNull String title) {
        this.title = TextHolder.of(title);
        return this;
    }

    /**
     * Sets the title of the menu.
     *
     * @param  title  the title to be set
     * @return        the builder for chaining
     */
    public MenuBuilder title(@NonNull TextHolder title) {
        this.title = title;
        return this;
    }

    /**
     * Sets the amount of rows of the menu.
     *
     * @param  rows  the amount of rows to be set
     * @return        the builder for chaining
     */
    public MenuBuilder rows(int rows) {
        checkRows(rows);
        this.rows = rows;
        return this;
    }

    /**
     * Sets the type of the menu.
     * @param type the type, ex. {@link MenuType#HOPPER}, {@link MenuType#FURNACE}, etc.
     * @apiNote By default, it is {@link MenuType#CHEST}.
     * @return the builder for chaining
     */
    public MenuBuilder type(MenuType type) {
        this.type = type;
        return this;
    }

    /**
     * Adds a modifier to the list of modifiers.
     *
     * @param  modifier  the modifier to be added
     * @return           the builder for chaining
     */
    public MenuBuilder addModifier(@NonNull Modifier modifier) {
        modifiers.add(modifier);
        return this;
    }

    /**
     * Remove a modifier from the list of modifiers.
     *
     * @param  modifier  the modifier to be removed
     * @return           the builder for chaining
     */
    public MenuBuilder removeModifier(@NonNull Modifier modifier) {
        modifiers.remove(modifier);
        return this;
    }

    /**
     * Add all the modifiers of {@link Modifier} to the list of modifiers.
     * @return the builder for chaining
     */
    public MenuBuilder addAllModifiers() {
        modifiers.addAll(Modifier.ALL);
        return this;
    }

    @NotNull
    @Contract(" -> new")
    public Menu normal() {
        checkRequirements(rows, title);
        return type == MenuType.CHEST ? new Menu(rows, title, modifiers) : new Menu(type, title, modifiers);
    }

    static void checkRows(int rows) {
        if (rows > 0 && rows <= 6) return;
        throw new IllegalArgumentException("Rows must be more than 1 or 6 and less" + "\nRows: " + rows + "\nFix: Rows must be 1-6");
    }

    static void checkRequirements(int rows, TextHolder title) {
        checkRows(rows);
        if (title != null) return;
        throw new IllegalArgumentException("Title must not be null or empty" + "\nTitle equals null" + "\nFix: Title must not be null or empty");
    }
}
