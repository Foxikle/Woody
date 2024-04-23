package me.flame.menus.adventure;

import net.kyori.adventure.text.Component;

import net.kyori.adventure.text.TextComponent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings("deprecation")
public class Lore implements Iterable<TextHolder> {
    private final ItemMeta meta;
    private TextHolder[] lore;

    private static final Lore EMPTY = new Lore((ItemMeta) null);
    private static final TextHolder[] EMPTY_LORE = new TextHolder[0];

    public Lore(ItemMeta meta) {
        this.meta = meta;
        this.lore = (CompHolder.isNativeAdventureSupport()) ? lore(meta) : getLore(meta);
    }
    @Contract(pure = true)
    public Lore(@NotNull Lore lore) {
        this.meta = lore.meta;
        this.lore = lore.lore;
    }

    private static @NotNull TextHolder[] lore(ItemMeta meta) {
        if (meta == null || !meta.hasLore()) return EMPTY_LORE;
        List<Component> components = Objects.requireNonNull(meta.lore());
        return components.stream().map(CompHolder::of).toArray(TextHolder[]::new);
    }

    private static @NotNull TextHolder[] getLore(ItemMeta meta) {
        if (meta == null || !meta.hasLore()) return EMPTY_LORE;
        List<String> components = Objects.requireNonNull(meta.getLore());
        return components.stream().map(StringHolder::of).toArray(TextHolder[]::new);
    }

    public static Lore empty() {
        return EMPTY;
    }

    public int size() {
        return lore.length;
    }

    public TextHolder get(int stringIndex) {
        if (stringIndex >= lore.length) return null;
        return lore[stringIndex];
    }

    @NotNull
    @Override
    public Iterator<TextHolder> iterator() {
        return new TextHolderIterator(lore);
    }

    @Override
    public void forEach(Consumer<? super TextHolder> consumer) {
        for (TextHolder holder : lore) {
            consumer.accept(holder);
        }
    }

    public void set(int stringIndex, TextHolder of) {
        if (stringIndex >= lore.length) return;
        lore[stringIndex] = of;
    }

    public void toItemLore(ItemStack itemStack, boolean setMeta) {
        for (TextHolder textHolder : lore) textHolder.asItemLoreAtEnd(meta);
        if (setMeta) itemStack.setItemMeta(meta);
    }

    public TextHolder asOneTextHolder() {
        return (CompHolder.isNativeAdventureSupport())
                ? CompHolder.of(components(lore))
                : StringHolder.of(strings(lore));
    }

    private static @NotNull Component components(TextHolder[] lore) {
        TextComponent.Builder builder = Component.text();
        Arrays.stream(lore).map(TextHolder::asComponent).forEach(builder::append);
        return builder.build();
    }

    private static @NotNull String strings(TextHolder[] lore) {
        StringBuilder builder = new StringBuilder();
        Arrays.stream(lore).map(TextHolder::toString).forEach(builder::append);
        return builder.toString();
    }

    public void copyFrom(TextHolder[] newLore) {
        this.lore = newLore;
    }

    public static class TextHolderIterator implements Iterator<TextHolder> {
        private final TextHolder[] lore;
        private int index;
        private final int length;

        @Contract(pure = true)
        public TextHolderIterator(@NotNull TextHolder[] lore) {
            this.lore = lore;
            this.length = lore.length;
        }


        @Override
        public boolean hasNext() {
            return index < length;
        }

        @Override
        public TextHolder next() {
            if (index >= length) throw new NoSuchElementException();
            TextHolder holder = lore[index];
            index++;
            return holder;
        }
    }
}
