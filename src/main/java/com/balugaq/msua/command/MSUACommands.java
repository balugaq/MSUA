package com.balugaq.msua.command;

import com.balugaq.msua.MSUA;
import com.balugaq.msua.command.sub.LoadCommand;
import com.balugaq.msua.command.sub.UnloadCommand;
import com.balugaq.msua.command.sub.WtfCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MSUACommands implements TabExecutor {
    public static final List<MSUACommand> commands = new ArrayList<>();
    static {
        commands.add(new LoadCommand());
        commands.add(new UnloadCommand());
        commands.add(new WtfCommand());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        for (var cmd : commands) {
            if (AccessLevel.op.compareTo(cmd.accessLevel()) > 0 && !sender.isOp() && !sender.hasPermission("msua.admin")) {
                continue;
            }

            if (cmd.executable(sender, command, label, args)) {
                try {
                    cmd.execute(sender, command, label, args);
                } catch (CommandMissingArgException e) {
                    cmd.missingRequirements(sender, e.getMissingRequirements());
                } catch (Throwable e) {
                    MSUA.complain(sender, e);
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length <= 1) {
            return commands.stream().filter(cmd -> accessible(cmd, sender)).map(MSUACommand::name).toList();
        }

        for (var cmd : commands) {
            if (!accessible(cmd, sender)) {
                continue;
            }

            if (cmd.executable(sender, command, label, args)) {
                return cmd.tabComplete(sender, command, label, args);
            }
        }

        return new ArrayList<>();
    }

    public static boolean accessible(@NotNull MSUACommand cmd, @NotNull CommandSender sender) {
        return cmd.accessLevel().compareTo(AccessLevel.op) >= 0 && (sender.isOp() || sender.hasPermission("msua.admin"));
    }
}
