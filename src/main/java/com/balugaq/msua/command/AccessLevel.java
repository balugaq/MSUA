package com.balugaq.msua.command;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public interface AccessLevel extends Comparable<AccessLevel> {
    AccessLevel op = new AccessLevelImpl(4);
    AccessLevel player = new AccessLevelImpl(1);

    /**
     * Gets the level of access
     * @return the level of access
     */
    int getLevel();

    @Override
    default int compareTo(@NotNull AccessLevel o) {
        return Integer.compare(getLevel(), o.getLevel());
    }

    @RequiredArgsConstructor
    class AccessLevelImpl implements AccessLevel {
        public final int level;

        @Override
        public int getLevel() {
            return level;
        }
    }
}
