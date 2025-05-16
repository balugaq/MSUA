package com.balugaq.msua.api;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

@ApiStatus.Obsolete
public class Rollbacker {
    public static final Map<Plugin, Runnable> ROLLBACKS = new HashMap<>();

    public static void addRollback(Plugin plugin, Runnable runnable) {
        ROLLBACKS.merge(plugin, runnable, (a, b) -> () -> {
            a.run();
            b.run();
        });
    }
}
