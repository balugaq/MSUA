package com.balugaq.msua.command;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Obsolete
public interface IAccessLevel extends Comparable<IAccessLevel> {
    IAccessLevel op = new AccessLevel(4);
    IAccessLevel player = new AccessLevel(1);

    /**
     * Gets the level of access
     *
     * @return the level of access
     */
    int getLevel();

    @Override
    default int compareTo(@NotNull IAccessLevel o) {
        return Integer.compare(getLevel(), o.getLevel());
    }

    class AccessLevel implements IAccessLevel {
        public AccessLevel(int level) {
            this.level = level;
        }

        public final int level;

        @Override
        public int getLevel() {
            return level;
        }
    }
}
