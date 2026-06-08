package com.balugaq.msua;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author balugaq
 */
@NullMarked
@Getter
public class IntegrationManager {
    private boolean enabledPylon;
    private boolean enabledRebar;
    private boolean enabledSlimefun;
    private final List<IIntegration> integrations = new ArrayList<>();

    public void setup() {
        Bukkit.getScheduler().runTaskLaterAsynchronously(MSUA.instance(), () -> {
            PluginManager pm = Bukkit.getPluginManager();

            enabledPylon = pm.isPluginEnabled("Pylon");
            enabledRebar = pm.isPluginEnabled("Rebar");
            enabledSlimefun = pm.isPluginEnabled("Slimefun");
        }, 2L);
    }

    private void setupIntegration(boolean enabled, Supplier<IIntegration> integrationSupplier) {
        if (enabled) {
            IIntegration integration = integrationSupplier.get();
            try {
                integration.setup();
                integrations.add(integration);
                MSUA.console("Loaded " + integration.getClass().getSimpleName());
            } catch (Exception e) {
                MSUA.console("An error occurred when loading " + integration.getClass().getSimpleName());
                MSUA.console(e);
            }
        }
    }

    public void shutdown() {
        for (IIntegration integration : integrations) {
            try {
                integration.shutdown();
                MSUA.console("Unloaded " + integration.getClass().getSimpleName());
            } catch (Exception e) {
                MSUA.console("An error occurred when unloading " + integration.getClass().getSimpleName());
                MSUA.console(e);
            }
        }
    }
}