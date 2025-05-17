package com.balugaq.msua.command.sub;

import com.balugaq.msua.FileUtil;
import com.balugaq.msua.MSUA;
import com.balugaq.msua.PluginUtil;
import com.balugaq.msua.command.AccessLevel;
import com.balugaq.msua.command.MSUACommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ApiStatus.Obsolete
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
        var loadDependencies = getArgument(args, 1, "<arg>").equalsIgnoreCase(FLAG_LOAD_DEPENDENCIES);

        var simpleName = getBehindArgument(args, 1 + (loadDependencies ? 1 : 0), "<file_name>");
        var file = new File(FileUtil.pluginFolder, simpleName + ".jar");
        if (!file.exists()) {
            wrongUsage(sender, "/msua load [" + FLAG_LOAD_DEPENDENCIES + "] <plugin>");
            return true;
        }

        try {
            var plugin = PluginUtil.loadJar(file, false);
            if (plugin != null) {
                if (plugin.isEnabled()) {
                    MSUA.complain(sender, "Plugin " + simpleName + " is already loaded.");
                }

                PluginUtil.adaptedLoadPlugin(plugin, loadDependencies);
                PluginUtil.enablePlugin(plugin, loadDependencies);
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

        if (args.length == 2) {
            simpleNames.add(FLAG_LOAD_DEPENDENCIES);
        }

        if (args.length > 3) {
            return simpleNames;
        }

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
