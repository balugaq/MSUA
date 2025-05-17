package com.balugaq.msua;

import com.google.common.base.Preconditions;
import io.papermc.lib.PaperLib;
import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.provider.entrypoint.DependencyContext;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.java.LibraryLoader;
import org.bukkit.plugin.java.PluginClassLoader;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

@ApiStatus.Experimental
@SuppressWarnings({"unchecked", "deprecation", "removal", "UnstableApiUsage"})
public class PluginUtil {
    public static final Server server = Bukkit.getServer();
    @Deprecated
    public static final List<PluginClassLoader> loaders = new CopyOnWriteArrayList<>();
    @Deprecated
    public static final LibraryLoader libraryLoader = new LibraryLoader(server.getLogger());

    @SneakyThrows
    @ApiStatus.Obsolete
    public static void adaptedLoadPlugin(Plugin plugin, boolean loadDependencies) {
        if (loadDependencies) {
            var dependencies = getDependencies(plugin);
            for (var p : dependencies) {
                adaptedLoadPlugin(p, true);
            }
        }

        handlePaperLoadPlugin(plugin);
    }

    @SneakyThrows
    @ApiStatus.Obsolete
    public static void adaptedUnloadPlugin(Plugin plugin, boolean unloadChildren) {
        if (unloadChildren) {
            var children = getChildren(plugin);
            for (var p : children) {
                adaptedUnloadPlugin(p, true);
            }
        }

        handlePaperUnloadPlugin(plugin);
    }

    @SneakyThrows
    @ApiStatus.Experimental
    @NotNull
    public static synchronized Plugin loadJar(@NotNull File file) {
        return loadJar(file, true);
    }

    @SneakyThrows
    @ApiStatus.Experimental
    @NotNull
    public static synchronized Plugin loadJar(@NotNull File file, boolean loadPlugin) {
        Preconditions.checkArgument(file != null, "File cannot be null");
        // MSUA start - bypass paper
        // // Paper start
        // if (true) {
        //     try {
        //         return this.paperPluginManager.loadJar(file);
        //     } catch (org.bukkit.plugin.InvalidDescriptionException ignored) {
        //         return null;
        //     }
        // }
        // // Paper end
        // MSUA end

        Plugin result = loadPlugin0(file, true);

        // MSUA start - load plugin
        if (result != null && loadPlugin) {
            adaptedLoadPlugin(result, true);
        }
        // MSUA end

        return result;
    }


    @SneakyThrows
    @ApiStatus.Experimental
    @ApiStatus.Internal
    @Nullable
    public static Plugin loadPlugin0(@NotNull File file, boolean loadDependencies) {
        Preconditions.checkArgument(file != null, "File cannot be null");

        if (!file.exists()) {
            throw new InvalidPluginException(new FileNotFoundException(file.getPath() + " does not exist"));
        }

        final PluginDescriptionFile description;
        try {
            description = getPluginDescription(file);
        } catch (InvalidDescriptionException ex) {
            throw new InvalidPluginException(ex);
        }

        final File parentFile = server.getPluginsFolder(); // Paper
        final File dataFolder = new File(parentFile, description.getName());
        final File oldDataFolder = new File(parentFile, description.getRawName());

        // Found old data folder
        if (dataFolder.equals(oldDataFolder)) {
            // They are equal -- nothing needs to be done!
        } else if (dataFolder.isDirectory() && oldDataFolder.isDirectory()) {
            server.getLogger().warning(String.format(
                    "While loading %s (%s) found old-data folder: `%s' next to the new one `%s'",
                    description.getFullName(),
                    file,
                    oldDataFolder,
                    dataFolder
            ));
        } else if (oldDataFolder.isDirectory() && !dataFolder.exists()) {
            if (!oldDataFolder.renameTo(dataFolder)) {
                throw new InvalidPluginException("Unable to rename old data folder: `" + oldDataFolder + "' to: `" + dataFolder + "'");
            }
            server.getLogger().log(Level.INFO, String.format(
                    "While loading %s (%s) renamed data folder: `%s' to `%s'",
                    description.getFullName(),
                    file,
                    oldDataFolder,
                    dataFolder
            ));
        }

        if (dataFolder.exists() && !dataFolder.isDirectory()) {
            throw new InvalidPluginException(String.format(
                    "Projected datafolder: `%s' for %s (%s) exists and is not a directory",
                    dataFolder,
                    description.getFullName(),
                    file
            ));
        }

        for (final String pluginName : description.getDepend()) {
            Plugin current = server.getPluginManager().getPlugin(pluginName);

            if (current == null) {
                // MSUA start - load dependencies
                if (loadDependencies) {
                    var files = Arrays.stream(server.getPluginsFolder().listFiles()).filter(f -> f.getName().endsWith(".jar")).toList();
                    for (var f : files) {
                        var plugin = loadJar(f, false);
                        if (plugin.getName().equals(pluginName)) {
                            adaptedLoadPlugin(plugin, true);
                            break;
                        }
                    }
                    continue;
                }
                // MSUA end

                throw new UnknownDependencyException("Unknown dependency " + pluginName + ". Please download and install " + pluginName + " to run this plugin.");
            }
        }

        server.getUnsafe().checkSupported(description);

        var jar = new JarFile(file);
        final PluginClassLoader loader;
        try {
            // MSUA start - fix loader
            //loader = new PluginClassLoader(JavaPluginLoader.class.getClassLoader(), description, dataFolder, file, (libraryLoader != null) ? libraryLoader.createLoader(description) : null, null, null);
            loader = new PluginClassLoader(JavaPluginLoader.class.getClassLoader(), description, dataFolder, file, (libraryLoader != null) ? libraryLoader.createLoader(description) : null, jar, new DependencyContext() {
                @Override
                public boolean isTransitiveDependency(@NotNull PluginMeta pluginMeta, @NotNull PluginMeta pluginMeta1) {
                    return Bukkit.getPluginManager().isTransitiveDependency(pluginMeta, pluginMeta1);
                }

                @Override
                public boolean hasDependency(@NotNull String s) {
                    if (PaperLib.isPaper() && Bukkit.getPluginManager() instanceof SimplePluginManager spm) {
                        /* io.papermc.paper.plugin.manager.PaperPluginManagerImpl */
                        var impl = spm.paperPluginManager;
                        return (boolean) ReflectionUtil.invokeMethod(impl, "hasDependency", s);
                    } else {
                        return Bukkit.getPluginManager().getPlugin(s) != null;
                    }
                }
            }); // Paper
            // MSUA end
        } catch (InvalidPluginException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new InvalidPluginException(ex);
        }

        loaders.add(loader);

        return loader.getPlugin();
    }

    @SneakyThrows
    @ApiStatus.Experimental
    @NotNull
    public static PluginDescriptionFile getPluginDescription(@NotNull File file) throws InvalidDescriptionException {
        Preconditions.checkArgument(file != null, "File cannot be null");

        JarFile jar = null;
        InputStream stream = null;

        try {
            jar = new JarFile(file);
            JarEntry entry = jar.getJarEntry("plugin.yml");

            if (entry == null) {
                throw new InvalidDescriptionException(new FileNotFoundException("Jar does not contain plugin.yml"));
            }

            stream = jar.getInputStream(entry);

            return new PluginDescriptionFile(stream);

        } catch (IOException | YAMLException ex) {
            throw new InvalidDescriptionException(ex);
        } finally {
            if (jar != null) {
                try {
                    jar.close();
                } catch (IOException ignored) {
                }
            }
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    @SneakyThrows
    @ApiStatus.Experimental
    public static void handlePaperLoadPlugin(Plugin plugin) {
        handleSpigotLoadPlugin(plugin);
        if (PaperLib.isPaper() && Bukkit.getPluginManager() instanceof SimplePluginManager spm) {
            /* io.papermc.paper.plugin.manager.PaperPluginManagerImpl */
            var impl = spm.paperPluginManager;
            var loadPlugin = ReflectionUtil.getMethod(impl.getClass(), "loadPlugin", Plugin.class);
            loadPlugin.invoke(impl, plugin);
        }
    }

    @SneakyThrows
    @ApiStatus.Obsolete
    public static void handleSpigotLoadPlugin(Plugin plugin) {
        /* Reflection */
        List<Plugin> plugins = ((List<Plugin>) ReflectionUtil.getValue(Bukkit.getPluginManager(), "plugins"));
        plugins.add(plugin);
        /* Reflection */
        Map<String, Plugin> lookupNames = ((Map<String, Plugin>) ReflectionUtil.getValue(Bukkit.getPluginManager(), "lookupNames"));
        lookupNames.put(plugin.getDescription().getName().toLowerCase(Locale.ENGLISH), plugin); // Paper
        for (String provided : plugin.getDescription().getProvides()) {
            lookupNames.putIfAbsent(provided.toLowerCase(Locale.ENGLISH), plugin); // Paper
        }
    }

    @SneakyThrows
    @ApiStatus.Experimental
    public static void handlePaperUnloadPlugin(Plugin plugin) {
        handleSpigotUnloadPlugin(plugin);
        if (PaperLib.isPaper() && Bukkit.getPluginManager() instanceof SimplePluginManager spm) {
            /* io.papermc.paper.plugin.manager.PaperPluginManagerImpl */
            var impl = spm.paperPluginManager;
            /* io.papermc.plugin.manager.PaperPluginInstanceManager */
            var instanceManager = ReflectionUtil.getValue(impl, "instanceManager");

            PluginMeta configuration = plugin.getPluginMeta();

            List<Plugin> plugins = (List<Plugin>) ReflectionUtil.getValue(instanceManager, "plugins");
            plugins.remove(plugin);
            Map<String, Plugin> lookupNames = (Map<String, Plugin>) ReflectionUtil.getValue(instanceManager, "lookupNames");

            lookupNames.remove(configuration.getName().toLowerCase(Locale.ENGLISH), plugin);
            for (String providedPlugin : configuration.getProvidedPlugins()) {
                lookupNames.remove(providedPlugin.toLowerCase(Locale.ENGLISH), plugin);
            }

            /* io.papermc.paper.plugin.entrypoint.dependency.MetaDependencyTree */
            var dependencyTree = ReflectionUtil.getValue(instanceManager, "dependencyTree");
            var remove = ReflectionUtil.getMethod(dependencyTree.getClass(), "remove", PluginMeta.class);
            remove.invoke(dependencyTree, configuration);
        }

        unloadJar(plugin);
    }

    @SneakyThrows
    @ApiStatus.Obsolete
    public static void handleSpigotUnloadPlugin(Plugin plugin) {
        var plugins = (List<Plugin>) ReflectionUtil.getValue(Bukkit.getPluginManager(), "plugins");
        plugins.remove(plugin);
        var lookupNames = (Map<String, Plugin>) ReflectionUtil.getValue(Bukkit.getPluginManager(), "lookupNames");
        lookupNames.remove(plugin.getDescription().getName().toLowerCase(Locale.ENGLISH));
        for (String provided : plugin.getDescription().getProvides()) {
            lookupNames.remove(provided.toLowerCase(Locale.ENGLISH));
        }
    }

    @SneakyThrows
    @ApiStatus.Obsolete
    public static void enablePlugin(Plugin plugin, boolean enableDependencies) {
        if (enableDependencies) {
            var dependencies = getDependencies(plugin);
            for (var p : dependencies) {
                enablePlugin(p, true);
            }
        }

        Bukkit.getPluginManager().enablePlugin(plugin);
    }

    @SneakyThrows
    @ApiStatus.Obsolete
    public static void disablePlugin(Plugin plugin, boolean disableChildren) {
        if (disableChildren) {
            var children = getChildren(plugin);
            for (var p : children) {
                disablePlugin(p, true);
            }
        }

        Bukkit.getPluginManager().disablePlugin(plugin);
    }

    @SneakyThrows
    @ApiStatus.Experimental
    public static void unloadJar(Plugin plugin) {
        if (plugin.getClass().getClassLoader() instanceof Closeable c) {
            unloadJar(c);
        } else {
            MSUA.console(new RuntimeException("Plugin(" + plugin.getName() + ") class loader is not closeable"));
        }
    }

    @SneakyThrows
    @ApiStatus.Experimental
    public static void unloadJar(Closeable classLoader) {
        classLoader.close();
    }

    public static List<Plugin> getChildren(Plugin plugin) {
        List<Plugin> children = new ArrayList<>();
        for (var p : Bukkit.getPluginManager().getPlugins()) {
            if (p != plugin && getDependencies(p).contains(plugin)) {
                children.add(p);
            }
        }

        return children;
    }

    public static List<Plugin> getDependencies(Plugin plugin) {
        if (!PaperLib.isPaper()) {
            return plugin.getDescription().getDepend().stream().map(Bukkit.getPluginManager()::getPlugin).toList();
        } else {
            return plugin.getPluginMeta().getPluginDependencies().stream().map(Bukkit.getPluginManager()::getPlugin).toList();
        }
    }
}
