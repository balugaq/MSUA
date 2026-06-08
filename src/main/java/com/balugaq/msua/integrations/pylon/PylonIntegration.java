package com.balugaq.msua.integrations.pylon;

import com.balugaq.msua.RebarUtil;
import com.balugaq.msua.ReflectionUtil;
import com.balugaq.msua.integrations.IIntegration;
import com.balugaq.msua.integrations.rebar.UnloadHandlers;
import io.github.pylonmc.pylon.PylonKeys;
import org.bukkit.NamespacedKey;

public class PylonIntegration implements IIntegration {
    @Override
    public void setup() {
        NamespacedKey sck = PylonKeys.SMELTERY_CONTROLLER;
        UnloadHandlers.addUnloadHandler(block -> {
            if (block.getSchema().getKey().equals(sck)) {
                RebarUtil.sendOpMessage("Handling SmelteryController");
                // smeltery pixels are NOT persistent, they are only removed when server stops,
                // so remove the pixels, otherwise they duplicate.
                ReflectionUtil.invokeMethod(block, "removePixels");
            }
        });
    }

    @Override
    public void shutdown() {

    }
}
