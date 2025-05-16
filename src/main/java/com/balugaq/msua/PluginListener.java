package com.balugaq.msua;

import com.balugaq.msua.api.Rollbacker;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Obsolete
public class PluginListener implements Listener {
    @SneakyThrows
    @EventHandler
    public void onDisablePlugin(PluginDisableEvent event) {
        Plugin jp = event.getPlugin();

        var rollback = Rollbacker.ROLLBACKS.get(jp);
        if (rollback != null) {
            try {
                rollback.run();
            } finally {
                Rollbacker.ROLLBACKS.remove(jp);
            }
        }

        unregisterVanillaRecipes(jp);
        unloadListeners(jp);

        patchSlimefunAddon(jp);
    }

    public void patchSlimefunAddon(Plugin plugin) {
        if (plugin instanceof SlimefunAddon addon) {
            UnregisterUtil.unregisterAddon(addon);
        }
    }

    public void unloadListeners(Plugin plugin) {
        HandlerList.unregisterAll(plugin);
    }

    @SneakyThrows
    public void unregisterVanillaRecipes(Plugin plugin) {
        var iter = Bukkit.recipeIterator();
        while (iter.hasNext()) {
            var recipe = iter.next();
            if (recipe instanceof Keyed keyed) {
                var namespacedKey = keyed.getKey();
                if (namespacedKey.getNamespace().equalsIgnoreCase(plugin.getName())) {
                    iter.remove();
                }
            }
        }
    }
}
