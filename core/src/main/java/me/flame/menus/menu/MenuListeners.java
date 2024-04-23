package me.flame.menus.menu;

import me.flame.menus.components.nbt.ItemNbt;
import me.flame.menus.events.BeforeAnimatingEvent;
import me.flame.menus.events.MenuCloseEvent;
import me.flame.menus.events.PageChangeEvent;
import me.flame.menus.items.MenuItem;
import me.flame.menus.menu.actions.Actions;
import me.flame.menus.menu.animation.Animation;
import me.flame.menus.util.ItemResponse;
import me.flame.menus.util.Option;
import me.flame.menus.util.VersionHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public final class MenuListeners implements Listener {
    private static final EnumSet<InventoryAction> TAKE = EnumSet.of(
            InventoryAction.PICKUP_ONE,
            InventoryAction.PICKUP_SOME,
            InventoryAction.PICKUP_HALF,
            InventoryAction.PICKUP_ALL,
            InventoryAction.COLLECT_TO_CURSOR,
            InventoryAction.HOTBAR_SWAP,
            InventoryAction.MOVE_TO_OTHER_INVENTORY
    );

    private static final EnumSet<InventoryAction> PLACE = EnumSet.of(
            InventoryAction.PLACE_ONE,
            InventoryAction.PLACE_SOME,
            InventoryAction.PLACE_ALL
    );

    private static final EnumSet<InventoryAction> SWAP = EnumSet.of(
            InventoryAction.HOTBAR_SWAP,
            InventoryAction.SWAP_WITH_CURSOR,
            InventoryAction.HOTBAR_MOVE_AND_READD
    );

    private static final EnumSet<InventoryAction> DROP = EnumSet.of(
            InventoryAction.DROP_ONE_SLOT,
            InventoryAction.DROP_ALL_SLOT,
            InventoryAction.DROP_ONE_CURSOR,
            InventoryAction.DROP_ALL_CURSOR
    );

    private static final InventoryType PLAYER = InventoryType.PLAYER;
    private static final InventoryAction OTHER_INV = InventoryAction.MOVE_TO_OTHER_INVENTORY;

    @EventHandler
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        Inventory inventory = event.getInventory(), clickedInventory = event.getClickedInventory();
        InventoryHolder holder = inventory.getHolder();
        if (!(holder instanceof IMenu)) return;

        IMenu menu = ((IMenu) holder);
        int slot = event.getSlot();

        menu.actions().executeInventoryEventBy(InventoryClickEvent.class, event);
        if (clickedInventory == null) return;

        if (menu.hasSlotActions()) {
            ItemResponse response = menu.getSlotActions().get(slot);
            if (response != null) response.execute((Player) event.getWhoClicked(), event);
        }

        if (modifierDetected(menu, event.getAction(), clickedInventory.getType(), inventory.getType()))
            event.setResult(Event.Result.DENY);
        executeItem(event, menu, event.getCurrentItem(), (Player) event.getWhoClicked(), slot);
    }

    @EventHandler
    public void onGuiClose(@NotNull InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder();
        if (!(holder instanceof IMenu)) return;
        IMenu menu = ((IMenu) holder);

        if (this.checkForCancellation(event, menu)) return;
        if (!menu.hasAnimations() || !menu.hasAnimationsStarted() || !inventory.getViewers().isEmpty()) return;
        menu.getAnimations().forEach(Animation::stop);
        menu.hasAnimationsStarted(false);
    }

    private boolean checkForCancellation(@NotNull InventoryCloseEvent event, IMenu menu) {
        if (menu.isUpdating()) return false;

        Actions actions = menu.actions();
        if (actions.executeInventoryEventBy(InventoryCloseEvent.class, event, menu))
            VersionHelper.runDelayed(() -> menu.open(event.getPlayer()), 1);
        return true;
    }

    @EventHandler
    public void onGuiDrag(@NotNull InventoryDragEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder();
        if (!(holder instanceof IMenu)) return;
        IMenu menu = ((IMenu) holder);

        if (menu.allModifiersAdded() || (!menu.areItemsPlaceable() && isDraggingOnGui(menu.size(), event.getRawSlots())))
            event.setResult(Event.Result.DENY);
        menu.actions().executeInventoryEventBy(InventoryDragEvent.class, InventoryDragEvent.class, event, menu);
    }

    @EventHandler
    public void onGuiOpen(@NotNull InventoryOpenEvent event) {
        InventoryView view = event.getView();
        InventoryHolder holder = view.getTopInventory().getHolder();
        if (!(holder instanceof IMenu)) return;
        IMenu menu = ((IMenu) holder);
        menu.update();

        checkAnimations(event, menu);
        if (!menu.isUpdating()) menu.actions().executeInventoryEventBy(InventoryOpenEvent.class, event, menu);
    }

    private static void checkAnimations(@NotNull InventoryOpenEvent event, @NotNull IMenu menu) {
        if (!menu.hasAnimations() || menu.hasAnimationsStarted()) return;
        menu.actions().executeInventoryEventBy(InventoryOpenEvent.class, event, menu);

        menu.getAnimations().forEach(animation -> animation.start(menu));
        menu.hasAnimationsStarted(true);
    }

    private static boolean isTakeItemEvent(InventoryAction action, InventoryType ciType, InventoryType type) {
        if (ciType == PLAYER || type == PLAYER) return false;
        return action == OTHER_INV || TAKE.contains(action);
    }

    private static boolean isPlaceItemEvent(InventoryAction action, InventoryType ciType, InventoryType type) {
        if (action == OTHER_INV && ciType == PLAYER && type != ciType) return true;
        return (ciType != PLAYER && type != PLAYER) || PLACE.contains(action);
    }


    private static boolean isSwapItemEvent(InventoryAction action, InventoryType ciType, InventoryType type) {
        return (ciType != PLAYER && type != PLAYER) && SWAP.contains(action);
    }

    private static boolean isDropItemEvent(InventoryAction action, InventoryType type) {
        return (type != PLAYER) && DROP.contains(action);
    }

    private static boolean isOtherEvent(InventoryAction action, InventoryType type) {
        return (action == InventoryAction.CLONE_STACK || action == InventoryAction.UNKNOWN) &&
               (type != PLAYER);
    }

    private static boolean isDraggingOnGui(int size, @NotNull Iterable<Integer> rawSlots) {
        for (int slot : rawSlots) if (slot < size) return true;
        return false;
    }

    private static void executeActions(InventoryClickEvent event, IMenu menu) {
        menu.actions().executeInventoryEventBy(InventoryClickEvent.class, InventoryClickEvent.class, event);
    }

    private static void executeItem(InventoryClickEvent actionEvent, IMenu menu, ItemStack it, Player player, int slot) {
        if (menu instanceof PaginatedMenu) {
            if (handlePaginatedMenu((PaginatedMenu) menu, actionEvent)) return;
        }

        Option<MenuItem> menuItem;
        if (it == null || (menuItem = menu.get(slot)).isNone()) return;
        MenuItem item = menuItem.orElseGetThrow(() -> new IllegalArgumentException(
                "You caught an ultra rare error, this usually should not be thrown by design," +
                "please report this to Woody" +
                "Option<MenuItem> was checked successfully and found that it had a value but this throws??"
        ));

        final String nbt = ItemNbt.getString(it, "woody-menu");
        if (nbt == null || !nbt.equals(item.getUniqueId().toString())) return;

        if (item.isOnCooldown(player)) return;
        item.click(player, actionEvent);
    }

    @SuppressWarnings("UnusedReturnValue")
    private static boolean handlePaginatedMenu(@NotNull PaginatedMenu menu, InventoryClickEvent event) {
        return menu.actions().executeInventoryEventBy(PageChangeEvent.class, InventoryClickEvent.class, event);
    }

    private static boolean modifierDetected(@NotNull IMenu menu, InventoryAction action, InventoryType ciType, InventoryType invType) {
        return (menu.allModifiersAdded()) ||
                    ((!menu.areItemsPlaceable() && isPlaceItemEvent(action, ciType, invType)) ||
                    (menu.areItemsRemovable() && (isTakeItemEvent(action, ciType, invType) || isDropItemEvent(action, invType))) ||
                    (!menu.areItemsSwappable() && isSwapItemEvent(action, ciType, invType)) ||
                    (!menu.areItemsCloneable() && isOtherEvent(action, invType)));
    }
}