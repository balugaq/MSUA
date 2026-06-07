package com.balugaq.msua.command.sub;

import com.balugaq.msua.MSUA;
import com.balugaq.msua.PluginUtil;
import com.balugaq.msua.command.AccessLevel;
import com.balugaq.msua.command.MSUACommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ApiStatus.Obsolete
public class UnloadCommand implements MSUACommand {
    @Override
    public @NotNull AccessLevel accessLevel() {
        return AccessLevel.op;
    }

    @Override
    public @NotNull String name() {
        return "unload";
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        var unloadChildren = getArgument(args, 1, "<arg>").equalsIgnoreCase(FLAG_UNLOAD_CHILDREN);

        var pluginName = getArgument(args, 1 + (unloadChildren ? 1 : 0), "<plugin_name>");
        var plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        if (plugin == null) {
            wrongUsage(sender, "/msua unload [" + FLAG_UNLOAD_CHILDREN + "] <plugin>");
            return true;
        }

        if (!plugin.isEnabled()) {
            MSUA.complain(sender, "Plugin " + pluginName + " is already unloaded.");
        }

        try {
            PluginUtil.disablePlugin(plugin, unloadChildren);
        } catch (Throwable e) {
            MSUA.complain(sender, e);
        }

        try {
            PluginUtil.adaptedUnloadPlugin(plugin, unloadChildren);
        } catch (Throwable e) {
            MSUA.complain(sender, e);
        }

        MSUA.blue(sender, "Unloaded " + pluginName + ".");
        return true;
    }

    @Override
    public boolean executable(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return isCommand(args, name());
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> raw = new ArrayList<>();
        if (args.length == 2) {
            raw.addAll(Arrays.stream(Bukkit.getPluginManager().getPlugins()).filter(Plugin::isEnabled).map(Plugin::getName).filter(name -> name.startsWith(args[args.length - 1])).toList());
        }

        if (args.length == 3) {
            raw.add(FLAG_UNLOAD_CHILDREN);
        }
        return raw;
    }
}
