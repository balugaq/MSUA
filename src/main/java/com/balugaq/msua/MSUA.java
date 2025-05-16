package com.balugaq.msua;

import com.balugaq.msua.command.MSUACommand;
import com.balugaq.msua.command.MSUACommands;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

@SuppressWarnings("deprecation")
public class MSUA extends JavaPlugin {
    public static final String PREFIX = ChatColor.BLUE + "[" + ChatColor.GREEN + "MSUA" + ChatColor.BLUE + "] " + ChatColor.WHITE;
    public static MSUA instance = null;
    public static final PluginListener PLUGIN_LISTENER = new PluginListener();

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

    public static MSUA instance() {
        return instance;
    }

    public static void complain(CommandSender sender, Throwable e) {
        sendMessage(sender, ChatColor.RED + "An error occurred: " + e.getMessage());
        e.printStackTrace();
    }

    public static void complain(CommandSender sender, Object msg) {
        sendMessage(sender, ChatColor.RED + MSUACommand.asString(msg));
    }

    public static void complain(CommandSender sender, Object msg, Throwable e) {
        complain(sender, msg);
        complain(sender, e);
    }

    public static void thumbs(CommandSender sender, Object msg) {
        sendMessage(sender, ChatColor.GREEN + MSUACommand.asString(msg));
    }

    public static void blue(CommandSender sender, Object msg) {
        sendMessage(sender, ChatColor.BLUE + MSUACommand.asString(msg));
    }

    public static void sendMessage(CommandSender sender, Object msg) {
        sender.sendMessage(PREFIX + MSUACommand.asString(msg));
    }

    public static void console(Object msg) {
        instance().getLogger().info(MSUACommand.asString(msg));
    }

    public static void console(Throwable e) {
        instance().getLogger().log(Level.SEVERE, null, e);
    }
}