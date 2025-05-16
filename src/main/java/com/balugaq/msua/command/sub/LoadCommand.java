package com.balugaq.msua.command.sub;

import com.balugaq.msua.FileUtil;
import com.balugaq.msua.MSUA;
import com.balugaq.msua.PluginUtil;
import com.balugaq.msua.command.AccessLevel;
import com.balugaq.msua.command.MSUACommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoadCommand implements MSUACommand {
    @Override
    public @NotNull AccessLevel accessLevel() {
        return AccessLevel.op;
    }

    @Override
    public @NotNull String name() {
        return "load";
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        var simpleName = getBehindArgument(args, 1);
        var plugin = new File(FileUtil.pluginFolder, simpleName + ".jar");
        if (!plugin.exists()) {
            wrongUsage(sender, "/msua load <plugin>");
            return true;
        }

        try {
            var p = PluginUtil.loadPlugin(plugin);
            if (p != null) {
                if (p.isEnabled()) {
                    MSUA.complain(sender, "Plugin " + simpleName + " is already loaded.");
                    return true;
                }
                PluginUtil.handlePaperEnablePlugin(p);
                MSUA.blue(sender, "Loaded " + simpleName + ".");
            } else {
                MSUA.complain(sender, "Unable to load plugin " + simpleName + ".");
            }
            return true;
        } catch (Throwable e) {
            MSUA.complain(sender, "Unable to load plugin " + simpleName + ".", e);
        }

        return false;
    }

    @Override
    public boolean executable(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return isCommand(args, name());
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> simpleNames = new ArrayList<>();
        var files = FileUtil.pluginFolder.listFiles();
        if (files == null) {
            return simpleNames;
        }

        var loaded = Arrays.stream(Bukkit.getPluginManager().getPlugins()).map(plugin -> FileUtil.getJarFile(plugin.getClass())).toList();

        for (File file : files) {
            if (file.isDirectory()) {
                continue;
            }

            if (!file.getName().endsWith(".jar")) {
                continue;
            }

            if (loaded.contains(file)) {
                continue;
            }

            simpleNames.add(file.getName().substring(0, file.getName().length() - 4));
        }

        return simpleNames;
    }
}
