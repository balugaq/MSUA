package com.balugaq.msua;

import com.google.common.base.Preconditions;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

@ApiStatus.Experimental
@SuppressWarnings({"unchecked", "deprecation", "removal"})
public class PluginUtil {
    public static final Server server = Bukkit.getServer();
    public static final List<PluginClassLoader> loaders = new CopyOnWriteArrayList<>();
    public static final LibraryLoader libraryLoader = new LibraryLoader(server.getLogger());

    @SneakyThrows
    @ApiStatus.Experimental
    @Nullable
    public static synchronized Plugin loadPlugin(@NotNull File file) {
        Preconditions.checkArgument(file != null, "File cannot be null");
        // Bypass Paper
        // // Paper start
        // if (true) {
        //     try {
        //         return this.paperPluginManager.loadPlugin(file);
        //     } catch (org.bukkit.plugin.InvalidDescriptionException ignored) {
        //         return null;
        //     }
        // }
        // // Paper end
        Plugin result = null;
        result = loadPlugin0(file);

        if (result != null) {
            /* Reflection */
            List<Plugin> plugins = ((List<Plugin>) ReflectionUtil.getValue(Bukkit.getPluginManager(), "plugins"));
            plugins.add(result);
            /* Reflection */
            Map<String, Plugin> lookupNames = ((Map<String, Plugin>) ReflectionUtil.getValue(Bukkit.getPluginManager(), "lookupNames"));
            lookupNames.put(result.getDescription().getName().toLowerCase(Locale.ENGLISH), result); // Paper
            for (String provided : result.getDescription().getProvides()) {
                lookupNames.putIfAbsent(provided.toLowerCase(Locale.ENGLISH), result); // Paper
            }
        }

        return result;
    }

    @SneakyThrows
    @ApiStatus.Experimental
    @ApiStatus.Internal
    public static Plugin loadPlugin0(@NotNull File file) {
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
                throw new UnknownDependencyException("Unknown dependency " + pluginName + ". Please download and install " + pluginName + " to run this plugin.");
            }
        }

        server.getUnsafe().checkSupported(description);

        var jar = new JarFile(file);
        final PluginClassLoader loader;
        try {
            loader = new PluginClassLoader(JavaPluginLoader.class.getClassLoader(), description, dataFolder, file, (libraryLoader != null) ? libraryLoader.createLoader(description) : null, jar, new DependencyContext() {
                @Override
                public boolean isTransitiveDependency(@NotNull PluginMeta pluginMeta, @NotNull PluginMeta pluginMeta1) {
                    return false;
                }

                @Override
                public boolean hasDependency(@NotNull String s) {
                    return false;
                }
            }); // Paper
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
    public static void handlePaperEnablePlugin(Plugin plugin) {
        Bukkit.getPluginManager().enablePlugin(plugin);
        if (Bukkit.getPluginManager() instanceof SimplePluginManager spm) {
            /* io.papermc.paper.plugin.manager.PaperPluginManagerImpl */
            var impl = spm.paperPluginManager;
            var loadPlugin = ReflectionUtil.getMethod(impl.getClass(), "loadPlugin", Plugin.class);
            loadPlugin.invoke(impl, plugin);
        }
    }
}
