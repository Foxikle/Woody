package me.flame.menus.menu.animation;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import com.google.errorprone.annotations.CheckReturnValue;
import me.flame.menus.menu.Menu;
import me.flame.menus.menu.animation.variants.NormalAnimation;
import me.flame.menus.menu.animation.variants.RepeatedAnimation;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Animations in Woody are highly dependent on bukkit scheduler. shout out to bukkit
 * <p>
 * Example usage:
 * <pre>{@code
 *     IMenu menu = ...;
 *     menu.addAnimation(Animation.builder(menu)
 *                                .frames(
 *                                      Frame.builder().addItems(ItemBuilder.of(Material.STONE).buildItem()).build(),
 *                                      Frame.builder().addItems(ItemBuilder.of(Material.WOODEN_SWORD).buildItem()).build(),
 *                                      Frame.builder().addItems(ItemBuilder.of(Material.CARROT).buildItem()).build(),
 *                                      ...
 *                                )
 *                                .type(Type.REPEATED)
 *                                .build());
 * }</pre>
 * @author FlameyosFlow
 * @since 1.5.0, 100% Stabilized at 2.0.0
 */
@SuppressWarnings("unused")
public abstract class Animation {
    protected int frameIndex;
    protected final int delay, repeat;
    protected List<Frame> frames;

    protected AnimationScheduler scheduler;

    public Animation(int delay, int repeat, Frame @NotNull [] frames, AnimationScheduler scheduler) {
        this.delay = delay;
        this.repeat = repeat;
        this.frameIndex = 0;

        int length = frames.length;
        this.frames = ImmutableList.copyOf(frames);
        this.scheduler = scheduler;
    }


    public void reset() {
        frameIndex = 0;
    }

    @Nullable
    @CheckReturnValue
    public Frame next(Menu menu) {
        if (frames.size() == frameIndex) return this.onFinish(menu);
        Frame frame = frames.get(frameIndex);
        if (frame != null) {
            frameIndex++;
            frame.start(menu);
        }
        return frame;
    }

    @CanIgnoreReturnValue
    @Contract(pure = true)
    public Frame start(Menu menu) {
        frameIndex = 0;
        menu.setAnimating(true);
        menu.getActiveAnimations().add(this);
        if (this.scheduler == null) {
            this.scheduler = new AnimationScheduler(this, menu, delay, repeat);
        }
        return frames.get(frameIndex);
    }

    public void stop() {
        this.scheduler.cancel();
    }

    @CanIgnoreReturnValue
    public abstract Frame onFinish(Menu menu);

    /*
     * Builders
     */

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static Animation.Builder builder(Menu menu) {
        return new Animation.Builder(menu);
    }

    public int getFrameIndex() {
        return frameIndex;
    }

    public int getDelay() {
        return delay;
    }

    public int getRepeat() {
        return repeat;
    }

    public List<Frame> getFrames() {
        return frames;
    }

    public AnimationScheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(final AnimationScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public enum Type {
        NORMAL, REPEATED
    }

    public static class Builder {
        private Frame[] frames;
        private int delay = -1;
        private int repeat = -1;
        private Type type = Type.NORMAL;
        private AnimationScheduler scheduler;
        private final Menu menu;

        public Builder(Menu menu) {
            this.menu = menu;
        }

        public Builder frames(Frame... frames) {
            this.frames = frames;
            return this;
        }

        public Builder delay(int delay) {
            this.delay = delay;
            return this;
        }

        public Builder repeat(int repeat) {
            this.repeat = repeat;
            return this;
        }

        public Builder type(Type type) {
            this.type = type;
            return this;
        }

        public Animation build() {
            if (delay == -1 || repeat == -1) {
                throw new IllegalArgumentException(
                    (delay == -1 && repeat == -1 ? "(\"delay\" and \"repeat\" are)" : (repeat == -1 ? "\"repeat\" is" : "\"delay\" is")) + " -1/undefined in Animation building\n"
                            + "Fix: Define what you need, for example: \n" +
                            "Animation.builder()\n" +
                            "    .delay(5)\n" +
                            (delay == -1 ? "    ^^^^^^^^^\n" : "") +
                            "    .repeat(5)" +
                            (repeat == -1 ? "    ^^^^^^^^^\n" : "") +
                            "    .build()"
                );
            }
            if (scheduler == null)
                this.scheduler = new AnimationScheduler(null, menu, delay, repeat);
            Animation animation = type == Type.NORMAL ? new NormalAnimation(delay, repeat, frames, scheduler) : new RepeatedAnimation(delay, repeat, frames, scheduler);
            scheduler.animation = animation;
            return animation;
        }
    }
}