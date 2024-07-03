package me.flame.menus.menu;

import org.jetbrains.annotations.NotNull;

public final class Structure {
    private int size, rows, columns;
    private final OpenedType type;

    private Structure(int size) {
        this.size = size;
        this.columns = 9;
        this.rows = size / columns;
        this.type = MenuType.CHEST;
    }

    private Structure(@NotNull OpenedType type) {
        this.size = type.maxSize();
        this.columns = type.maxColumns();
        this.rows = type.maxRows();
        this.type = type;
    }

    public int size() {
        return size;
    }

    public int rows() {
        return rows;
    }

    public int columns() {
        return columns;
    }

    public OpenedType type() {
        return type;
    }

    public void size(final int size) {
        this.size = size;
    }

    public void rows(final int rows) {
        this.rows = rows;
    }

    public void columns(final int columns) {
        this.columns = columns;
    }

    @NotNull
    public static Structure of(int size) {
        return new Structure(size);
    }

    @NotNull
    public static Structure of(OpenedType type) {
        return new Structure(type);
    }

    @NotNull
    public static Structure ofRows(int rows) {
        return new Structure(rows * 9);
    }
}