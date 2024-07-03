package me.flame.menus.menu.fillers;

import me.flame.menus.items.MenuItem;

import me.flame.menus.menu.contents.BukkitContents;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@SuppressWarnings("unused")
public final class Filler {
    static void fillMenu(int size, MenuItem item, BukkitContents menu) {
        for (int itemIndex = 0; itemIndex < size; itemIndex++) {
            if (!menu.hasItem(itemIndex)) menu.setItem(itemIndex, item);
        }
    }

    public static void fill(@NotNull MenuItem item, BukkitContents menu) {
        fillMenu(menu.size(), item, menu);
    }

    public static void fillBorders(@NotNull MenuItem borderMaterial, BukkitContents menu) {
        addBorderItem(menu.size(), borderMaterial, menu);
    }

    static void addBorderItem(int size, MenuItem item, @NotNull BukkitContents menu) {
        for (int slot = 0; slot < menu.size(); slot++) {
            int row = slot / menu.columns();
            int col = slot % menu.columns();
            if (row == 0 || row == menu.rows() - 1 || col == 0 || col == 8) {
                Optional<MenuItem> currentItem = menu.getItem(slot);
                if (currentItem.isEmpty() || currentItem.get().getType() == Material.AIR) {
                    menu.setItem(slot, item);
                }
            }
        }
    }

    static void addRowItems(int sizedRow, int size, MenuItem item, BukkitContents menu) {
        for (int i = sizedRow; i < size; i++) menu.setItem(i, item);
    }

    public static void fillRow(int row, MenuItem item, BukkitContents menu) {
        if (row < 1 || row > 6) return;
        row--;
        final int sizedRow = row * 9, size = sizedRow + 9;
        addRowItems(sizedRow, size, item, menu);
    }

    public static void fillArea(int length, int width, MenuItem itemStack, BukkitContents menu) {
        final int size = menu.size();
        addAreaItems(length, width, itemStack, size, menu);
    }

    static void fillAskedSide(Side side, MenuItem item, BukkitContents menu) {
        final int rows = menu.rows();
        switch (side) {
            case TOP:
                addRowItems(0, 8, item, menu);
                break;
            case BOTTOM:
                addRowItems(rows * 9, (rows * 9) + 9, item, menu);
                break;
            // implement LEFT and RIGHT from scratch; like filling vertical rows
            case LEFT:
                for (int i = 0; i < rows; i++) menu.setItem(i, item);
                break;
            case RIGHT:
                int size = menu.size();
                for (int i = 8; i < size; i += 9) menu.setItem(i, item);
                break;
            case LEFT_RIGHT:
                for (int i = 0; i < rows; i++) {
                    menu.setItem(i, item);
                    menu.setItem(i + 8, item);
                }
                break;
        }
    }

    public static void fillSide(Side side, MenuItem borderMaterial, BukkitContents menu) {
        fillAskedSide(side, borderMaterial, menu);
    }

    static void addAreaItems(int length, int width, MenuItem itemStack, int size, BukkitContents menu) {
        int slot = findFirstAreaSlot(width, length);
        while (slot < size) {
            if (isInArea(slot, length, width)) menu.setItem(slot, itemStack);
            slot++;
        }
    }

    public static boolean isInArea(int slot, int length, int width) {
        return (slot / 9) < (length / 9) && (slot % 9) < width;
    }

    public static int findFirstAreaSlot(int width, int length) {
        int area = width * length;
        return Integer.numberOfTrailingZeros(area);
    }

    // isInArea = simple geometry to check if a slot is in an area of L*W

    public enum Side {
        TOP,
        BOTTOM,
        LEFT,
        RIGHT,
        LEFT_RIGHT
    }
}
