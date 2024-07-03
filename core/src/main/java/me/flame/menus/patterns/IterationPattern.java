package me.flame.menus.patterns;

import me.flame.menus.menu.Slot;

@FunctionalInterface
public interface IterationPattern {
    Slot shift(Slot slot, int maxRows, int maxColumns);
}
