package me.flame.menus.menu.actions;

import me.flame.menus.menu.Menu;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.*;

@SuppressWarnings("unused")
public record Action<E extends Event, T extends Event>(
         Consumer<E> action,
         Function<T, E> customEventSupplier,
         Class<T> executionArea,
         BiPredicate<T, Menu> executeIf,
         Consumer<E> onCancel,
         boolean ignoreCancelled,
         boolean async
) {
    public static <E extends Event, T extends Event> ActionBuilder<E, T> builder(Class<E> customEvent, Class<T> providedEvent) {
        return new ActionBuilder<>(customEvent, providedEvent);
    }

    /**
     * Creates the event.
     * @param event the event that gets mapped to the new event.
     * @return the new event
     */
    public E createEvent(T event) {
        return customEventSupplier.apply(event);
    }

    /**
     * Executes the action from the mapped event.
     * <p>
     * If "async" is set to "true", it will run in a CompletableFuture
     * @param event the event.
     */
    public void executeEvent(E event) {
        if (event == null) return;
        if (async) CompletableFuture.runAsync(() -> action.accept(event));
        else action.accept(event);
    }

    public boolean canExecute(T event, Menu menu) {
        return executeIf.test(event, menu);
    }

    public void executeCancellationEvent(E event) {
        onCancel.accept(event);
    }

    @Contract(" -> new")
    public @NotNull Action<E, T> copy() {
        return new Action<>(action, customEventSupplier, executionArea, executeIf, onCancel, ignoreCancelled, async);
    }

    public static class ActionBuilder<E extends Event, T extends Event> {
        Consumer<E> eventConsumer;
        Function<T, E> customEventSupplier;
        Class<T> executionArea;
        BiPredicate<T, Menu> executeIf = (unmappedEvent, menu) -> true;
        Consumer<E> onCancelAction = (event) -> {};
        boolean ignoreCancelled = false;
        boolean async = false;

        ActionBuilder(Class<E> customEvent, Class<T> providedEvent) {
            Objects.requireNonNull(customEvent);
            this.executionArea = Objects.requireNonNull(providedEvent);
        }

        public ActionBuilder<E, T> action(Consumer<E> eventConsumer) {
            this.eventConsumer = eventConsumer;
            return this;
        }

        public ActionBuilder<E, T> customEvent(Function<T, E> customEventSupplier) {
            this.customEventSupplier = customEventSupplier;
            return this;
        }

        public ActionBuilder<E, T> executeIf(BiPredicate<T, Menu> executeWhen) {
            this.executeIf = executeWhen;
            return this;
        }

        public ActionBuilder<E, T> onCancelAction(Consumer<E> onCancelAction) {
            this.onCancelAction = onCancelAction;
            return this;
        }

        public ActionBuilder<E, T> async(boolean async) {
            this.async = async;
            return this;
        }

        public ActionBuilder<E, T> ignoreCancelled(boolean ignoreCancelled) {
            this.ignoreCancelled = ignoreCancelled;
            return this;
        }

        public Action<E, T> build() {
            return new Action<>(eventConsumer, customEventSupplier, executionArea, executeIf, onCancelAction, ignoreCancelled, async);
        }
    }
}
