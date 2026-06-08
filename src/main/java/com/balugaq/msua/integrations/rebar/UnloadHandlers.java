package com.balugaq.msua.integrations.rebar;

import io.github.pylonmc.rebar.block.RebarBlock;

import java.util.ArrayList;
import java.util.List;

public class UnloadHandlers {
    private static final List<UnloadHandler> handlers = new ArrayList<>();

    public static void addUnloadHandler(UnloadHandler handler) {
        handlers.add(handler);
    }

    public static void handle(RebarBlock block) {
        for (UnloadHandler handler : handlers) {
            handler.checkAndHandle(block);
        }
    }
}
