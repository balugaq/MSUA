package com.balugaq.msua;

import io.github.pylonmc.rebar.addon.RebarAddon;
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

        MSUA.sendOpMessage("Unregistering vanilla recipes ", jp.getName());
        unregisterVanillaRecipes(jp);
        MSUA.sendOpMessage("Unregistering listeners ", jp.getName());
        unloadListeners(jp);

        if (MSUA.instance().getIntegrationManager().isEnabledSlimefun()) {
            if (jp instanceof SlimefunAddon addon) {
                MSUA.sendOpMessage("Disabling SlimefunAddon ", addon.getName());
                SlimefunUtil.unregisterAddon(addon);
            }
        }
        if (Bukkit.getPluginManager().isPluginEnabled("Rebar")) {
            if (jp instanceof RebarAddon addon) {
                MSUA.sendOpMessage("Disabling RebarAddon ", addon.getDisplayName());
                RebarUtil.unregisterAddon(addon);
            }
        }
    }

    public void unloadListeners(Plugin plugin) {
        HandlerList.unregisterAll(plugin);
    }

    @SuppressWarnings("DataFlowIssue")
    @SneakyThrows
    public void unregisterVanillaRecipes(Plugin plugin) {
        var iter = Bukkit.recipeIterator();
        while (iter.hasNext()) {
            var recipe = iter.next();
            if (recipe instanceof Keyed keyed) {
                var namespacedKey = keyed.getKey();
                if (namespacedKey.getNamespace().equalsIgnoreCase(plugin.getName())) {
                    // see RecipeIterator
                    ReflectionUtil.invokeMethod(ReflectionUtil.getValue(ReflectionUtil.getValue(
                            Nms.getRecipeManager(),
                            "recipes"),
                                "byKey"),
                                    "remove",
                                ReflectionUtil.invokeMethod(ReflectionUtil.getValue(iter,
                                    "currentRecipe"),
                                        "id"));
                    ReflectionUtil.invokeMethod(ReflectionUtil.getValue(
                            iter,
                            "recipes"),
                                "remove");
                }
            }
        }
        ReflectionUtil.invokeMethod(Nms.getRecipeManager(),
                    "finalizeRecipeLoading");
        ReflectionUtil.invokeMethod(ReflectionUtil.invokeMethod(
                Nms.getMinecraftServer(),
                "getPlayerList"),
                    "reloadRecipes");
    }
}
