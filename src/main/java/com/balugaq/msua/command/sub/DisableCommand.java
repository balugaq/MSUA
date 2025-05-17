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
public class DisableCommand implements MSUACommand {
    @Override
    public @NotNull AccessLevel accessLevel() {
        return AccessLevel.op;
    }

    @Override
    public @NotNull String name() {
        return "disable";
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        var disableChildren = getArgument(args, 1, "<arg>").equalsIgnoreCase(FLAG_DISABLE_CHILDREN);

        var pluginName = getArgument(args, 1 + (disableChildren ? 1 : 0), "<plugin_name>");
        var plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        if (plugin == null) {
            wrongUsage(sender, "/msua disable [" + FLAG_DISABLE_CHILDREN + "] <plugin>");
            return true;
        }

        if (!plugin.isEnabled()) {
            MSUA.complain(sender, "Plugin " + pluginName + " is already disabled.");
        }

        PluginUtil.disablePlugin(plugin, disableChildren);
        MSUA.blue(sender, "Disabled " + pluginName + ".");
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
            raw.add(FLAG_DISABLE_CHILDREN);
        }

        if (args.length == 2 || args.length == 3 && args[1].equalsIgnoreCase(FLAG_DISABLE_CHILDREN)) {
            raw.addAll(Arrays.stream(Bukkit.getPluginManager().getPlugins()).map(Plugin::getName).toList());
        }
        return raw;
    }
}
