package me.flame.menus.menu;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import me.flame.menus.adventure.TextHolder;
import me.flame.menus.items.MenuItem;
import me.flame.menus.modifiers.Modifier;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable data class containing of Data to simplify the internal
 * <p>
 * code (parameters)
 */
@Getter
@EqualsAndHashCode
@SuppressWarnings({ "unused", "unchecked" })
public class MenuData implements Serializable, ConfigurationSerializable {
    private int rows;
    private List<ItemData> pages;
    private TextHolder title;
    private MenuType type;
    private ItemData items;
    private EnumSet<Modifier> modifiers;

    public MenuData(String title, int rows, List<ItemData> pages, EnumSet<Modifier> modifiers, ItemData items) {
        this.title = TextHolder.of(title);
        this.rows = rows;
        this.pages = pages;
        this.type = MenuType.CHEST;
        this.modifiers = modifiers;
        this.items = items;
    }

    public MenuData(String title, int rows, List<ItemData> pages, EnumSet<Modifier> modifiers, ItemData items) {
        this.title = TextHolder.of(title);
        this.rows = 1;
        this.pages = pages;
        this.type = type;
        this.modifiers = modifiers;
    }

    public MenuData(TextHolder title, int rows, List<ItemData> pages, EnumSet<Modifier> modifiers, ItemData items) {
        this.title = title;
        this.rows = rows;
        this.pages = pages;
        this.type = MenuType.CHEST;
        this.modifiers = modifiers;
    }

    public MenuData(TextHolder title, MenuType type, List<ItemData> pages, EnumSet<Modifier> modifiers, ItemData items) {
        this.title = title;
        this.rows = 1;
        this.pages = pages;
        this.type = type;
        this.modifiers = modifiers;
    }

    public MenuData(String title, int rows, EnumSet<Modifier> modifiers, ItemData items) {
        this(TextHolder.of(title), rows, null, modifiers, items);
    }

    public MenuData(String title, MenuType type, EnumSet<Modifier> modifiers, ItemData items) {
        this(TextHolder.of(title), type, null, modifiers, items);
    }

    public MenuData(TextHolder title, int rows, EnumSet<Modifier> modifiers, ItemData items) {
        this(title, rows, null, modifiers, items);
    }

    public MenuData(TextHolder title, MenuType type, EnumSet<Modifier> modifiers, ItemData items) {
        this(title, type, null, modifiers, items);
    }

    private void readObject(java.io.ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.readFields();
        this.title = (TextHolder) s.readObject();
        this.rows = s.readInt();
        this.pages = (List<ItemData>) s.readObject();
        this.type = (MenuType) s.readObject();
        this.items = (ItemData) s.readObject();
        this.modifiers = (EnumSet<Modifier>) s.readObject();
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeObject(title);
        s.writeInt(rows);
        s.writeObject(pages);
        s.writeObject(type);
        s.writeObject(items);
        s.writeObject(modifiers);
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>(6);
        map.put("title", title.toString());
        map.put("rows", rows);
        map.put("pages", pages);
        map.put("type", type.name());
        map.put("items", items);
        map.put("modifiers", modifiers);
        return map;
    }

    /**
     * Deserialize the serialized data and create a MenuData object.
     *
     * @param  serializedData  the serialized data to be deserialized
     * @return                 the deserialized MenuData object
     */
    public static MenuData deserialize(Map<String, Object> serializedData) {
        MenuData data;
        MenuType type = MenuType.valueOf((String) serializedData.get("type"));
        if (type == MenuType.CHEST) {
            data = new MenuData(
                TextHolder.of((String) serializedData.get("title")),
                (int) serializedData.get("rows"),
                (List<ItemData>) serializedData.get("pages"),
                (EnumSet<Modifier>) serializedData.get("modifiers"),
                (ItemData) serializedData.get("items")
            );
        } else {
            data = new MenuData(
                    TextHolder.of((String) serializedData.get("title")),
                    type,
                    (List<ItemData>) serializedData.get("pages"),
                    (EnumSet<Modifier>) serializedData.get("modifiers"),
                    (ItemData) serializedData.get("items")
            );
        }
        return data;
    }

    @NotNull
    public Menu asMenu() {
        Menu menu = type != MenuType.CHEST ? new Menu(type, title, modifiers) : new Menu(rows, title, modifiers);
        menu.setContents(items);
        return menu;
    }

    @NotNull
    public static MenuData intoData(Menu menu) {
        if (menu instanceof PaginatedMenu) {
            return menu.getType() == MenuType.CHEST
                    ? new MenuData(menu.title(), menu.rows(), ((PaginatedMenu) menu).pages(), menu.getModifiers(), menu.getItems())
                    : new MenuData(menu.title(), menu.getType(), ((PaginatedMenu) menu).pages(), menu.getModifiers(), menu.getItems());
        }
        return menu.getType() == MenuType.CHEST
                ? new MenuData(menu.title(), menu.rows(), menu.getModifiers(), menu.getItems())
                : new MenuData(menu.title(), menu.getType(), menu.getModifiers(), menu.getItems());
    }
}
