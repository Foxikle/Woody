package me.flame.menus.menu.animation;

import lombok.Setter;
import me.flame.menus.menu.Menu;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.Optional;
import java.util.concurrent.*;

public class AnimationScheduler implements Runnable {
    private transient final ScheduledExecutorService task;
    private transient BukkitTask bukkitTask;

    Animation animation;
    private final int delay, repeat;

    public AnimationScheduler(Animation animation, int delay, int repeat) {
        this.animation = animation;
        this.delay = delay;
        this.repeat = repeat;
        this.task = null;
        this.bukkitTask = Bukkit.getScheduler().runTaskTimer(Menu.getPlugin(), this, delay, repeat);
    }

    public AnimationScheduler(Animation animation, int delay, int repeat, ScheduledExecutorService service) {
        this.animation = animation;
        this.task = service;
        this.delay = delay;
        this.repeat = repeat;
        this.bukkitTask = null;
        task.scheduleAtFixedRate(this, delay * 50L, repeat * 50L, TimeUnit.MILLISECONDS);
    }

    private void readObject() {

    }

    @Override
    public void run() {
        Frame frame = animation.next();
        if (frame == null) this.tryCancel();
    }

    public void start() {
        if (task != null) {
            task.scheduleAtFixedRate(this, delay * 50L, repeat * 50L, TimeUnit.MILLISECONDS);
        } else if (bukkitTask != null) {
            this.bukkitTask = Bukkit.getScheduler().runTaskTimer(Menu.getPlugin(), this, delay, repeat);
        }
    }

    public void tryCancel() {
        /*if (this.isSchedulingWithFolia()) {
            ScheduledTask scheduledTask = (ScheduledTask) this.scheduledTask;
            if (!scheduledTask.isCancelled()) task.cancel();
            Optional.of(frames.get(0)).ifPresent(Frame::reset);
            return;
        }*/
        if (task != null && !task.isShutdown()) {
            task.shutdown();
        } else if (bukkitTask != null && bukkitTask.isCancelled()) {
            bukkitTask.cancel();
        }
        Optional.of(animation.frames.get(0)).ifPresent(Frame::reset);
    }

    /*public boolean isSchedulingWithFolia() {
        return VersionHelper.IS_FOLIA && scheduledTask != null;
    }

    public boolean isNotSchedulingWithFolia() {
        return !VersionHelper.IS_FOLIA && task != null;
    }*/
}