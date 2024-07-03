package me.flame.menus.menu.loader;

import me.flame.menus.items.MenuItem;
import me.flame.menus.menu.Structure;
import me.flame.menus.menu.contents.BukkitContents;
import me.flame.menus.menu.opener.MenuOpener;
import me.flame.menus.menu.pagination.IndexedPagination;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface PagedMenuLoader {
    Component title();

    Structure structure();

    default MenuOpener opener() {
        return MenuOpener.DEFAULT;
    }

    void setup(IndexedPagination menu);

    List<BukkitContents> load(IndexedPagination menu);

    int pageCount();

    Pair<Integer, MenuItem> nextItem();

    Pair<Integer, MenuItem> previousItem();
}
