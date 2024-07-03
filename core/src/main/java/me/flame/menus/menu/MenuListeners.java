package me.flame.menus.menu;

import me.flame.menus.events.MenuCloseEvent;
import me.flame.menus.items.ClickSound;
import me.flame.menus.items.ItemResponse;
import me.flame.menus.components.nbt.ItemNbt;
import me.flame.menus.items.MenuItem;
import me.flame.menus.menu.animation.Animation;
import me.flame.menus.util.Option;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("MethodMayBeStatic")
public final class MenuListeners implements Listener {
    private static final EnumSet<InventoryAction> TAKE = EnumSet.of(
            InventoryAction.PICKUP_ONE, InventoryAction.PICKUP_SOME, InventoryAction.PICKUP_HALF, InventoryAction.PICKUP_ALL,
            InventoryAction.COLLECT_TO_CURSOR, InventoryAction.HOTBAR_SWAP, InventoryAction.MOVE_TO_OTHER_INVENTORY
    );

    private static final EnumSet<InventoryAction> PLACE = EnumSet.of(InventoryAction.PLACE_ONE, InventoryAction.PLACE_SOME, InventoryAction.PLACE_ALL);
    private static final EnumSet<InventoryAction> SWAP = EnumSet.of(InventoryAction.HOTBAR_SWAP, InventoryAction.SWAP_WITH_CURSOR, InventoryAction.HOTBAR_MOVE_AND_READD);
    private static final EnumSet<InventoryAction> DROP = EnumSet.of(InventoryAction.DROP_ONE_SLOT, InventoryAction.DROP_ALL_SLOT, InventoryAction.DROP_ONE_CURSOR, InventoryAction.DROP_ALL_CURSOR);

    private final Menus menus;

    public MenuListeners(final Menus menus) {
        this.menus = menus;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        Inventory inventory = event.getInventory(), clickedInventory = event.getClickedInventory();
        InventoryHolder holder = inventory.getHolder();
        if (!(holder instanceof Menu menu)) return;
        int slot = event.getSlot();

        menu.actions().executeInventoryEventBy(event, menu);
        if (clickedInventory == null) return;

        checkSlotAction(event, menu, slot);
        cancelIfModifierDetected(event, menu, clickedInventory, inventory);
        executeItem(event, menu, event.getCurrentItem(), (Player) event.getWhoClicked(), slot, menus);
    }

    private static void cancelIfModifierDetected(final @NotNull InventoryClickEvent event, final Menu menu, final @NotNull Inventory clickedInventory, final Inventory inventory) {
        if (clickedInventory.getType() != InventoryType.PLAYER && 
            modifierDetected(menu, event.getAction(), clickedInventory.getType(), inventory.getType())) event.setResult(Event.Result.DENY);
    }

    private static void checkSlotAction(final @NotNull InventoryClickEvent event, final @NotNull Menu menu, final int slot) {
        if (menu.hasSlotActions()) {
            ItemResponse response = menu.getSlotActions().get(slot);
            if (response != null) response.execute((Player) event.getWhoClicked(), event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true) // monitor since it doesn't actually get cancelled
    public void onGuiClose(@NotNull InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder();
        if (!(holder instanceof Menu menu)) return;

        if (menu.isUpdating()) return;
        if (menu.actions().executeInventoryEventBy(event, menu).contains(MenuCloseEvent.class)) {
            Bukkit.getScheduler().runTaskLater(menus.getPlugin(), () -> menu.open(event.getPlayer()), 1L);
            return;
        }

        if (!menu.isAnimating() || !menu.getViewers().isEmpty()) return;

        menu.getActiveAnimations().forEach(Animation::stop);
        menu.getActiveAnimations().clear();
        menu.setAnimating(false);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGuiDrag(@NotNull InventoryDragEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder();
        if (!(holder instanceof Menu menu)) return;

        if (menu.allModifiersAdded() || (!menu.areItemsPlaceable() && isDraggingOnGui(menu.size(), event.getRawSlots())))
            event.setResult(Event.Result.DENY);
        menu.actions().executeInventoryEventBy(event, menu);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGuiOpen(@NotNull InventoryOpenEvent event) {
        InventoryView view = event.getView();
        InventoryHolder holder = view.getTopInventory().getHolder();
        if (!(holder instanceof Menu menu)) return;
        if (!menu.isUpdating()) menu.actions().executeInventoryEventBy(event, menu);
    }

    private static boolean isTakeItemEvent(InventoryAction action, InventoryType type) {
        if (type == InventoryType.PLAYER) return false;
        return action == InventoryAction.MOVE_TO_OTHER_INVENTORY || TAKE.contains(action);
    }

    private static boolean isPlaceItemEvent(InventoryAction action, InventoryType ciType, InventoryType type) {
        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY && type != ciType) return true;
        return (type != InventoryType.PLAYER) || PLACE.contains(action);
    }

    private static boolean isSwapItemEvent(InventoryAction action, InventoryType type) {
        return (type != InventoryType.PLAYER) && SWAP.contains(action);
    }

    private static boolean isDropItemEvent(InventoryAction action, InventoryType type) {
        return (type != InventoryType.PLAYER) && DROP.contains(action);
    }

    private static boolean isOtherEvent(InventoryAction action, InventoryType type) {
        return (action == InventoryAction.CLONE_STACK || action == InventoryAction.UNKNOWN) && (type != InventoryType.PLAYER);
    }

    private static boolean isDraggingOnGui(int size, @NotNull Iterable<Integer> rawSlots) {
        for (int slot : rawSlots) if (slot < size) return true;
        return false;
    }

    private static void executeItem(InventoryClickEvent event, Menu menu, ItemStack it, Player player, int slot, Menus manager) {
        Optional<MenuItem> menuItem;
        if (it == null || (menuItem = menu.getItem(slot)).isEmpty()) return;
        final MenuItem item = menuItem.orElseThrow(() -> new IllegalArgumentException("""
            You caught an ultra rare error, this usually should not be thrown by design,
            please report this to Woody,
            Option<MenuItem> was checked successfully and found that it had a value.. but this throws?
        """));

        final String nbt = ItemNbt.getString(it, "woody-menu");
        if (nbt != null && !nbt.equals(item.getUniqueId().toString())) return;

        ItemResponse clickAction = item.getClickAction();
        if (clickAction == null) return;

        ClickSound sound = item.getSound() == null ? manager.getGlobalItemClickSound() : item.getSound();
        if (sound != null) player.playSound(player.getLocation(), sound.sound(), sound.volume(), sound.pitch());

        if (item.isAsync()) CompletableFuture.runAsync(() -> clickAction.execute(player, event));
        else clickAction.execute(player, event);
    }

    private static boolean modifierDetected(@NotNull Menu menu, InventoryAction action, InventoryType ciType, InventoryType invType) {
        return menu.allModifiersAdded() || ((!menu.areItemsPlaceable() && isPlaceItemEvent(action, ciType, invType)) ||
                    (menu.areItemsRemovable() && (isTakeItemEvent(action, invType) || isDropItemEvent(action, invType))) ||
                    (!menu.areItemsSwappable() && isSwapItemEvent(action, invType)) ||
                    (!menu.areItemsCloneable() && isOtherEvent(action, invType)));
    }
}