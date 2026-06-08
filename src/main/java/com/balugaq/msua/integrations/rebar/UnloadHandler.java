package com.balugaq.msua.integrations.rebar;

import io.github.pylonmc.rebar.block.RebarBlock;

@FunctionalInterface
public interface UnloadHandler {
    void checkAndHandle(RebarBlock block);
}
