package me.flame.menus.menu.animation;

import me.flame.menus.menu.Menu;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class AnimationScheduler implements Runnable {
    private BukkitTask service;

    Animation animation;
    private final int delay, repeat;
    private final Menu menu;

    public AnimationScheduler(Animation animation, Menu menu, int delay, int repeat) {
        this.animation = animation;
        this.delay = delay;
        this.repeat = repeat;
        this.menu = menu;
    }

    @Override
    public void run() {
        Frame frame = animation.next(menu);
        if (frame == null) this.cancel();
    }

    public void start() {
        this.service = Bukkit.getScheduler().runTaskTimerAsynchronously(menu.manager().getPlugin(), this, delay, repeat);
    }

    public void cancel() {
        if (this.service != null && !this.service.isCancelled()) this.service.cancel();
        Frame frame = this.animation.frames.get(0);
        if (frame != null) frame.reset(menu);
    }
}