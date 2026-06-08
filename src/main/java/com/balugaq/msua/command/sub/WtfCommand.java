package com.balugaq.msua.command.sub;

import com.balugaq.msua.FileUtil;
import com.balugaq.msua.MSUA;
import com.balugaq.msua.command.IAccessLevel;
import com.balugaq.msua.command.IMSUACommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@ApiStatus.Obsolete
public class WtfCommand implements IMSUACommand {
    @Override
    public @NotNull IAccessLevel accessLevel() {
        return IAccessLevel.op;
    }

    @Override
    public @NotNull String name() {
        return "wtf";
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        var pluginName = getArgument(args, 1);
        var plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        if (plugin == null) {
            wrongUsage(sender, "/msua wtf <plugin>");
            return true;
        }

        MSUA.blue(sender, pluginName + "=" + FileUtil.getJarFile(plugin.getClass()));
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
