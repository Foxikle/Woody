package me.flame.menus.adventure;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.bukkit.Bukkit;
import org.bukkit.inventory.meta.ItemMeta;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wrapper of an Adventure {@link Component}.
 * @author stefvanschie at <a href="https://github.com/stefvanschie/IF">IF's github.</a>
 */
@SuppressWarnings("unused")
public abstract class CompHolder extends TextHolder {
    private static final boolean nativeAdventureSupport = getCurrentVersion() == 1165 && isPaper();

    private static int getCurrentVersion() {
        Matcher matcher = Pattern
                .compile("(\\d+\\.\\d+)(\\.\\d+)?")
                .matcher(Bukkit.getBukkitVersion());

        if (matcher.find()) {
            String version = matcher.group(1).replace(".", "");
            String patch = matcher.group(2);
            patch = patch == null ? "0" : patch.replace(".", "");
            return Integer.parseUnsignedInt(version + patch);
        }

        throw new RuntimeException(
                "Could not retrieve server version! \nFix: Install the server properly or add a WORKING version/jar."
        );
    }

    private static boolean isPaper() {
        try { // PaperConfig
            Class.forName("com.destroystokyo.paper.PaperConfig");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.builder()
                .character(LegacyComponentSerializer.SECTION_CHAR)
                .useUnusualXRepeatedCharacterHexFormat()
                .hexColors()
                .build();
    
    /**
     * Wraps the specified Adventure component.
     *
     * @param value the value to wrap
     * @return an instance that wraps the specified value
     */
    @NotNull
    @Contract(pure = true)
    public static CompHolder of(@NotNull Object value) {
        Objects.requireNonNull(value, "value mustn't be null");

        return isNativeAdventureSupport() ? new NativeCompHolder((Component) value) : new ForeignCompHolder(value);
    }
    
    /**
     * Gets whether the server platform natively supports Adventure.
     * Native Adventure support means that eg. {@link ItemMeta#displayName(Component)}
     * is a valid method.
     *
     * @return whether the server platform natively supports Adventure
     * @since 2.0.0
     */
    @SuppressWarnings("LombokGetterMayBeUsed")
    public static boolean isNativeAdventureSupport() {
        return nativeAdventureSupport;
    }
    
    /**
     * The Adventure component this instance wraps.
     */
    @NotNull
    protected final Object value;
    
    /**
     * Creates and initializes a new instance.
     *
     * @param value the Adventure component this instance should wrap
     * @since 0.10.0
     */
    CompHolder(@NotNull Object value) {
        this.value = value;
    }
    
    /**
     * Gets the Adventure component this instance wraps.
     *
     * @return the contained Adventure component
     * @since 2.0.0
     */
    @NotNull
    @Contract(pure = true)
    public Component component() {
        return (Component) value;
    }

    /*

    @NotNull
    @Override
    @Contract(pure = true)
    public String toString() {
        return legacySerializer.serialize((Component) value);
    }*/
    
    @Override
    public int hashCode() {
        return value.hashCode();
    }
    
    @Override
    public boolean equals(Object other) {
        return other != null && getClass() == other.getClass()
                && Objects.equals(value, ((CompHolder) other).value);
    }

    public abstract boolean isComponent();
}
