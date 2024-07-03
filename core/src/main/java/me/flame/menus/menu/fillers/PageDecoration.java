package me.flame.menus.menu.fillers;


import me.flame.menus.items.MenuItem;
import me.flame.menus.menu.contents.BukkitContents;
import me.flame.menus.menu.pagination.Pagination;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public final class PageDecoration  {
    public static void fillBorders(MenuItem item, @NotNull Pagination<?> menu) {
        for (final BukkitContents page : menu.pages())  Filler.fillBorders(item, menu);
    }

    public static void fill(@NotNull MenuItem item, @NotNull Pagination<?> menu) {
        for (final BukkitContents page : menu.pages()) Filler.fill(item, menu);
    }

    public static void fillRow(final int row, MenuItem itemStack, @NotNull Pagination<?> menu) {
        for (final BukkitContents page : menu.pages())  Filler.fillRow(row, itemStack, page);
    }


    public static void fillArea(final int length, final int width, MenuItem itemStack, @NotNull Pagination<?> menu) {
        for (final BukkitContents page : menu.pages())  Filler.fillArea(length, width, itemStack, page);
    }

    public static void fillSide(Filler.Side side, MenuItem borderMaterial, @NotNull Pagination<?> menu) {
        for (final BukkitContents page : menu.pages()) Filler.fillAskedSide(side, borderMaterial, page);
    }
}
