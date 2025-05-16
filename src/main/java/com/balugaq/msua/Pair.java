package com.balugaq.msua;

import lombok.Data;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Obsolete
@Data
public class Pair<T, U> {
    public final T first;
    public final U second;

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }
}
