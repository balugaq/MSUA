package com.balugaq.msua;

import com.balugaq.msua.command.MSUACommand;
import com.balugaq.msua.command.MSUACommands;
import io.papermc.lib.PaperLib;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

@ApiStatus.Obsolete
@SuppressWarnings("deprecation")
public class MSUA extends JavaPlugin {
    public static final String PREFIX = ChatColor.BLUE + "[" + ChatColor.GREEN + "MSUA" + ChatColor.BLUE + "] " + ChatColor.WHITE;
    public static final PluginListener PLUGIN_LISTENER = new PluginListener();
    public static MSUA instance = null;

    public static MSUA instance() {
        return instance;
    }

    public static void complain(CommandSender sender, Throwable e) {
        sendMessage(sender, ChatColor.RED, "An error occurred: " + e.getMessage());
        e.printStackTrace();
    }

    public static void complain(CommandSender sender, Object... msg) {
        sendMessage(sender, ChatColor.RED, msg);
    }

    public static void complain(CommandSender sender, Object msg, Throwable e) {
        complain(sender, msg);
        complain(sender, e);
    }

    public static void thumbs(CommandSender sender, Object... msg) {
        sendMessage(sender, ChatColor.GREEN, msg);
    }

    public static void blue(CommandSender sender, Object... msg) {
        sendMessage(sender, ChatColor.BLUE, msg);
    }

    public static void sendMessage(CommandSender sender, Object... msg) {
        sendMessage(sender, ChatColor.WHITE, msg);
    }

    public static void sendMessage(CommandSender sender, ChatColor color, Object... msgs) {
        Component c = Component.text(PREFIX).color(TextColor.color(color.asBungee().getColor().getRGB()));

        for (Object msg : msgs) {
            if (msg instanceof Component com) {
                c = c.append(com);
            } else {
                c = c.append(Component.text("" + msg));
            }
        }

        sender.sendMessage(c);
    }

    public static void console(Object... msgs) {
        instance().getLogger().info(String.join("", Arrays.stream(msgs).map(s -> "" + s).toList()));
    }

    public static void console(Throwable e) {
        instance().getLogger().log(Level.SEVERE, null, e);
    }

    public static void sendOpMessage(Object... objects) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOp()) {
                sendMessage(player, objects);
            }
        }
    }

    @Override
    public void onEnable() {
        instance = this;

        boolean pass = true;
        getLogger().info("MSUA Checking Environment");
        if (!PaperLib.isPaper()) {
            getLogger().severe("MSUA requires Paper or its fork to run. Please use Paper instead of Spigot or Bukkit.");
            pass = false;
        }

        int major = PaperLib.getMinecraftVersion();
        if (major < 20) {
            getLogger().severe("MSUA requires Paper 1.20 or higher to run. Please update your server.");
            pass = false;
        }

        if (!pass) {
            getLogger().severe("Environment check failed! Use MSUA on your own risk!");
        }

        getLogger().info("MSUA Starting...");
        Bukkit.getPluginManager().registerEvents(PLUGIN_LISTENER, this);
        Bukkit.getPluginCommand("msua").setExecutor(new MSUACommands());
        getLogger().info("MSUA is ready.");
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }
}