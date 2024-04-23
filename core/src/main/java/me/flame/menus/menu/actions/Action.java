package me.flame.menus.menu.actions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.flame.menus.menu.IMenu;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.*;

@Getter
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class Action<E extends Event, T extends Event> {
    final Consumer<E> eventConsumer;
    final Function<T, E> customEventSupplier;
    final Class<T> executeOnSupportedEvent;
    final BiPredicate<T, IMenu> executeWhen;
    final Consumer<E> onCancelAction;
    final boolean ignoreCancelled;

    @Getter @Setter
    private Event.Result result = Event.Result.ALLOW;

    @NotNull
    public static <E extends Event, T extends Event> Action<E, T> create(
            Consumer<E> action,
            Class<T> executeOnSupportedEvent,
            Function<T, E> customEventSupplier,
            BiPredicate<T, IMenu> executeWhen,
            Consumer<E> onCancelAction,
            boolean ignoreCancelled) {
        return new Action<>(action, customEventSupplier, executeOnSupportedEvent, executeWhen, onCancelAction, ignoreCancelled);
    }

    @NotNull
    public static <E extends Event, T extends Event> Action<E, T> createOnlyConsumer(
            Consumer<E> action, Class<T> executeOnSupportedEvent,
            Function<T, E> customEventSupplier,
            Consumer<E> onCancelAction,
            boolean ignoreCancelled) {
        return new Action<>(action, customEventSupplier, executeOnSupportedEvent, (e, t) -> true, onCancelAction, ignoreCancelled);
    }

    @NotNull
    public static <E extends Event, T extends Event> Action<E, T> createOnlyPredicate(
            Consumer<E> action,
            Class<T> executeOnSupportedEvent,
            Function<T, E> customEventSupplier,
            BiPredicate<T, IMenu> executeWhen,
            boolean ignoreCancelled) {
        return new Action<>(action, customEventSupplier, executeOnSupportedEvent, executeWhen, (ignored) -> {}, ignoreCancelled);
    }

    @NotNull
    public static <E extends Event, T extends Event> Action<E, T> create(
            Consumer<E> action,
            Class<T> executeOnSupportedEvent,
            Function<T, E> customEventSupplier,
            BiPredicate<T, IMenu> executeWhen,
            boolean ignoreCancelled) {
        return new Action<>(action, customEventSupplier, executeOnSupportedEvent, executeWhen, (ignored) -> {}, ignoreCancelled);
    }

    @NotNull
    public static <E extends Event, T extends Event> Action<E, T> create(
            Consumer<E> action, Class<T> executeOnSupportedEvent,
            Function<T, E> customEventSupplier,
            BiPredicate<T, IMenu> executeWhen,
            Consumer<E> onCancelAction) {
        return new Action<>(action, customEventSupplier, executeOnSupportedEvent, executeWhen, onCancelAction, false);
    }

    @NotNull
    @Contract(value = "_, _, _, _ -> new", pure = true)
    public static <E extends Event, T extends Event> Action<E, T> createOnlyConsumer(
            Consumer<E> action,
            Class<T> executeOnSupportedEvent,
            Function<T, E> customEventSupplier,
            Consumer<E> onCancelAction) {
        return new Action<>(action, customEventSupplier, executeOnSupportedEvent, (e, t) -> true, onCancelAction, false);
    }

    @NotNull
    public static <E extends Event, T extends Event> Action<E, T> createOnlyPredicate(
            Consumer<E> action,
            Class<T> executeOnSupportedEvent,
            Function<T, E> customEventSupplier,
            BiPredicate<T, IMenu> executeWhen) {
        return new Action<>(action, customEventSupplier, executeOnSupportedEvent, executeWhen, (ignored) -> {}, false);
    }

    @NotNull
    @Contract(value = "_, _, _, _ -> new", pure = true)
    public static <E extends Event, T extends Event> Action<E, T> create(
            Consumer<E> action,
            Class<T> executeOnSupportedEvent,
            Function<T, E> customEventSupplier,
            BiPredicate<T, IMenu> executeWhen) {
        return new Action<>(action, customEventSupplier, executeOnSupportedEvent, executeWhen, (ignored) -> {}, false);
    }
}
