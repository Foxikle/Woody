package me.flame.menus.builders.menus;

import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;

import me.flame.menus.modifiers.Modifier;
import me.flame.menus.items.MenuItem;
import me.flame.menus.menu.*;
import me.flame.menus.menu.contents.*;

import net.kyori.adventure.text.Component;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public final class MenuLayoutBuilder {
    @NotNull
    private final Map<Character, MenuItem> mappedButtons;

    @NotNull
    private String[] patterns;

    private int rows;

    public MenuLayoutBuilder(@NotNull Map<Character, MenuItem> mappedButtons) {
        this.mappedButtons = new Char2ObjectOpenHashMap<>(mappedButtons);
        this.patterns = null;
        this.rows = 0;
    }

    public MenuLayoutBuilder() {
        this(new HashMap<>(3));
    }

    public MenuLayoutBuilder set(char id, MenuItem mapped) {
        this.mappedButtons.put(id, mapped);
        return this;
    }

    @SafeVarargs
    public final MenuLayoutBuilder setAll(Map.Entry<Character, MenuItem>... entries) {
        for (Map.Entry<Character, MenuItem> entry : entries) this.set(entry.getKey(), entry.getValue());
        return this;
    }

    public MenuLayoutBuilder pattern(String @NotNull... patterns) {
        if (this.patterns != null) return this;
        this.patterns = patterns;
        this.rows = patterns.length;
        return this;
    }

    /**
     * Creates a MenuLayout managed by BukkitContents and populates it with items.
     * @apiNote This is the recommended way.
     * @return The new MenuLayout
     */
    public MenuLayout create() {
        return new MenuLayout(patterns, rows, mappedButtons);
    }

    /**
     * Creates a menu with the given title and populates it with items.
     *
     * @param  title  the title of the menu
     * @return        the created menu
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "3.1.0")
    public @NotNull Menu createMenu(String title, Menus menus) {
        return createMenu(Component.text(title), menus);
    }

    /**
     * Creates a menu with the given title and populates it with items.
     *
     * @param  title  the title of the menu
     * @return        the created menu
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "3.1.0")
    public Menu createMenu(String title, EnumSet<Modifier> modifiers, Menus menus) {
        return createMenu(Component.text(title), modifiers, menus);
    }

    /**
     * Creates a menu with the given title and populates it with items.
     *
     * @param  title  the title of the menu
     * @return        the created menu
     */
    public @NotNull Menu createMenu(Component title, Menus menus) {
        this.validateData();
        MenuImpl menu = new MenuImpl(rows, title, EnumSet.noneOf(Modifier.class), menus);
        addItems(menu);
        return menu;
    }

    private void addItems(@NotNull Menu menu) {
        int size = menu.size();
        for (int buttonIndex = 0; buttonIndex < size; buttonIndex++) {
            int row = buttonIndex / 9;
            int column = buttonIndex % 9;

            MenuItem button = mappedButtons.get(patterns[row].charAt(column));
            menu.setItem(buttonIndex, button);
        }
    }

    /**
     * Creates a menu with the given title and populates it with items.
     *
     * @param  title  the title of the menu
     * @return        the created menu
     */
    public Menu createMenu(Component title, EnumSet<Modifier> modifiers, Menus menus) {
        this.validateData();
        MenuImpl menu = new MenuImpl(rows, title, modifiers, menus);
        addItems(menu);
        return menu;
    }

    public BukkitContents createContents(Menu menu) {
        this.validateData();
        return Contents.builder(menu)
                .iterate((content, buttonIndex) -> content.setItem(buttonIndex, this.mappedButtons.get(patterns[buttonIndex.row()].charAt(buttonIndex.column()))))
                .create();
    }

    public BukkitContents createConcurrentContents(Menu menu) {
        this.validateData();
        return Contents.builder(menu, true)
                .iterate((content, buttonIndex) -> content.setItem(buttonIndex, this.mappedButtons.get(patterns[buttonIndex.row()].charAt(buttonIndex.column()))))
                .create();
    }

    private void validateData() {
        if (patterns == null) throw new IllegalStateException("No patterns specified. \nFix: use the pattern() method before creating the menu.");
        else if (rows > 6 || rows < 1) throw new IllegalStateException("Patterns array has too many or too low rows (" + rows + "). \nFix: Reduce/increase the amount of strings in the array of pattern()");
    }
}
