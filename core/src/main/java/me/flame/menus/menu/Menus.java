package me.flame.menus.menu;

import me.flame.menus.items.ClickSound;
import me.flame.menus.patterns.IterationPattern;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.plugin.Plugin;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// TODO: add per-player views
@SuppressWarnings("unused")
public final class Menus {
    public static final EnumSet<InventoryType> TYPES = Stream.of(InventoryType.values())
            .filter(InventoryType::isCreatable)
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(InventoryType.class)));

    public Plugin getPlugin() {
        return plugin;
    }

    private final Plugin plugin;

    private final Map<InventoryType, Map.Entry<String, OpenedType>> types = new EnumMap<>(InventoryType.class);
    private final Map<Class<?>, IterationPattern> patterns = new HashMap<>(5);

    public ClickSound getGlobalItemClickSound() {
        return globalItemClickSound;
    }

    public void setGlobalItemClickSound(final ClickSound globalItemClickSound) {
        this.globalItemClickSound = globalItemClickSound;
    }

    private ClickSound globalItemClickSound;

    public Menus(Plugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(new MenuListeners(this), plugin);
    }

    public void putType(final OpenedType type) {
        if (!TYPES.contains(type.inventoryType())) {
            throw new IllegalArgumentException("Inventory type that was provided is not creatable: " + type.inventoryType().name());
        }
        this.types.put(type.inventoryType(), Map.entry(type.inventoryType().name(), type));
    }

    public OpenedType getType(InventoryType type) {
        return this.types.get(type).getValue();
    }

    public OpenedType getType(String type) {
        return this.types.get(InventoryType.valueOf(type.toUpperCase())).getValue();
    }

    public void putPattern(IterationPattern pattern) {
        this.patterns.put(pattern.getClass(), pattern);
    }

    public IterationPattern getPattern(Class<IterationPattern> pattern) {
        return this.patterns.get(pattern);
    }
}
