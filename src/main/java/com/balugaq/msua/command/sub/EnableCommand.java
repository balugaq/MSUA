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
public class EnableCommand implements MSUACommand {
    @Override
    public @NotNull AccessLevel accessLevel() {
        return AccessLevel.op;
    }

    @Override
    public @NotNull String name() {
        return "enable";
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        var enableDependencies = getArgument(args, 1, "<arg>").equalsIgnoreCase(FLAG_ENABLE_DEPENDENCIES);

        var pluginName = getArgument(args, 1 + (enableDependencies ? 1 : 0), "<plugin_name>");
        var plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        if (plugin == null) {
            wrongUsage(sender, "/msua enable [" + FLAG_ENABLE_DEPENDENCIES + "] <plugin>");
            return true;
        }

        if (plugin.isEnabled()) {
            MSUA.complain(sender, "Plugin " + pluginName + " is already enabled.");
        }

        PluginUtil.enablePlugin(plugin, enableDependencies);
        MSUA.blue(sender, "Enabled " + pluginName + ".");
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
            raw.add(FLAG_ENABLE_DEPENDENCIES);
        }

        if (args.length == 2 || args.length == 3 && args[1].equalsIgnoreCase(FLAG_ENABLE_DEPENDENCIES)) {
            raw.addAll(Arrays.stream(Bukkit.getPluginManager().getPlugins()).map(Plugin::getName).toList());
        }
        return raw;
    }
}
