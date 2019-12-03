package me.walrus.Heimdall.listeners;

import me.walrus.Heimdall.Heimdall;
import me.walrus.Heimdall.network.UserData;
import me.walrus.Heimdall.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class LoginListener implements Listener {

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        UserData userData = new UserData(event.getPlayer());
        Heimdall.addUser(userData);
        if(Heimdall.notificationsToSend.containsKey(event.getPlayer())){
            Bukkit.getScheduler().scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("Heimdall"), () -> {
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                Util.sendMessage(event.getPlayer(), "&aWhile you were offline a staff member solved one of your tickets...");
                for(String s : Heimdall.notificationsToSend.get(event.getPlayer())){
                    Util.sendMessage(event.getPlayer(), s);
                }
            }, 60);
        }
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event) {
        Heimdall.removeUser(event.getPlayer());
    }
}
