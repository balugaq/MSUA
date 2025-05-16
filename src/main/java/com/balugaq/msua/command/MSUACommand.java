package com.balugaq.msua.command;

import com.balugaq.msua.MSUA;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface MSUACommand {
    String UNKNOWN = "Unknown";
    @CanIgnoreReturnValue boolean execute(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args);
    boolean executable(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args);
    @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args);
    @NotNull AccessLevel accessLevel();
    @NotNull String name();

    default boolean isCommand(@NotNull String[] args, @NotNull String name) {
        return args.length > 0 && args[0].equalsIgnoreCase(name);
    }

    default String getArgument(@NotNull String[] args, int index) throws CommandMissingArgException {
        return getArgument(args, index, UNKNOWN);
    }

    default String getArgument(@NotNull String[] args, int index, String meaning) throws CommandMissingArgException {
        if (args.length <= index) {
            throw new CommandMissingArgException(List.of(meaning));
        }

        return args[index];
    }

    default String getBehindArgument(@NotNull String[] args, int start) {
        return getBehindArgument(args, start, UNKNOWN);
    }

    default String getBehindArgument(@NotNull String[] args, int start, String meaning) {
        if (args.length <= start) {
            throw new CommandMissingArgException(List.of(meaning));
        }

        return String.join(" ", List.of(args).subList(start, args.length));
    }

    default void missingRequirements(@NotNull CommandSender sender, @NotNull List<String> missingArgs) {
        if (missingArgs.size() == 1) {
            MSUA.complain(sender, "Missing requirement: " + missingArgs.get(0));
        } else {
            MSUA.complain(sender, "Missing requirements: " + String.join(", ", missingArgs));
        }
    }

    default void wrongUsage(@NotNull CommandSender sender, @NotNull String usage) {
        MSUA.complain(sender, "Usage: " + usage);
    }

    default void debug(@Nullable Object obj) {
        MSUA.console(asString(obj));
    }

    static String asString(@Nullable Object obj) {
        return obj == null ? "null" : obj.toString();
    }
}
