package com.balugaq.msua.command.sub;

import com.balugaq.msua.MSUA;
import com.balugaq.msua.command.AccessLevel;
import com.balugaq.msua.command.MSUACommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

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
        var pluginName = getArgument(args, 1);
        var plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        if (plugin == null) {
            wrongUsage(sender, "/msua unload <plugin>");
            return true;
        }

        if (!plugin.isEnabled()) {
            MSUA.complain(sender, "Plugin " + pluginName + " is already unloaded.");
            return true;
        }

        Bukkit.getPluginManager().disablePlugin(plugin);
        MSUA.blue(sender, "Unloaded " + pluginName + ".");
        return true;
    }

    @Override
    public boolean executable(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return isCommand(args, name());
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Arrays.stream(Bukkit.getPluginManager().getPlugins()).map(Plugin::getName).toList();
    }
}
