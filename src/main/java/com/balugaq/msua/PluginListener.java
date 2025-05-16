package com.balugaq.msua;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

public class PluginListener implements Listener {
    @EventHandler
    public void onDisablePlugin(PluginDisableEvent event) {
        unloadListeners(event.getPlugin());
        if (event.getPlugin() instanceof SlimefunAddon addon) {
            UnregisterUtil.unregisterAddon(addon);
        }
    }

    public void unloadListeners(Plugin plugin) {
        HandlerList.unregisterAll(plugin);
    }
}
