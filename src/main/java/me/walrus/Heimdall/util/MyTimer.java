package me.walrus.Heimdall.util;

import me.walrus.Heimdall.Heimdall;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.TimeUnit;

public class MyTimer implements Runnable {

    protected final Plugin plugin;
    protected long delay;
    protected long end;
    protected BukkitTask task;
    private Player player;
    private int secondstorun;

    public MyTimer(Plugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void set(int seconds) {
        this.delay = TimeUnit.SECONDS.toNanos(seconds);
        this.secondstorun = seconds;
    }

    public void start() {
        this.end = this.delay + System.nanoTime();
        if (this.task != null) {
            this.task.cancel();
        }
        this.task = plugin.getServer().getScheduler().runTaskLater(plugin, this, secondstorun * 20);
    }

    @Override
    public void run() {
        Heimdall.ticketCreateCooldown.remove(player);
    }

    public String timeLeft() {
        long left = this.end - System.nanoTime();
        long minutes = (left / 1000000000) / 60;
        int seconds = (int) (left / 1000000000) % 60;
        return String.format("There is &7%d %s &cand &7%d %s &cleft",
                minutes, this.getPlural(minutes, "minute", "minutes"),
                seconds, this.getPlural(seconds, "second", "seconds"));
    }

    protected String getPlural(long value, String single, String plural) {
        return (value == 1) ? single : plural;
    }

}