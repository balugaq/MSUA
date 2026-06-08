package com.balugaq.msua;

import io.github.pylonmc.rebar.Rebar;
import io.github.pylonmc.rebar.addon.RebarAddon;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.PhantomBlock;
import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.content.guide.RebarGuide;
import io.github.pylonmc.rebar.entity.EntityStorage;
import io.github.pylonmc.rebar.event.RebarBlockUnloadEvent;
import io.github.pylonmc.rebar.guide.button.FluidButton;
import io.github.pylonmc.rebar.guide.button.ItemButton;
import io.github.pylonmc.rebar.guide.button.PageButton;
import io.github.pylonmc.rebar.guide.button.ResearchButton;
import io.github.pylonmc.rebar.guide.pages.base.SimpleStaticGuidePage;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.recipe.ConfigurableRecipeType;
import io.github.pylonmc.rebar.registry.RebarRegistry;
import io.github.pylonmc.rebar.util.position.ChunkPosition;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.world.ChunkLoadEvent;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@ApiStatus.Obsolete
@UtilityClass
public class RebarUtil {
    public static BlockStorage getBlockStorageInstance() {
        return HandlerList.getRegisteredListeners(Rebar.INSTANCE).stream()
                .filter(l -> l.getListener() instanceof BlockStorage)
                .map(l -> (BlockStorage) l.getListener()).findFirst().get();
    }

    public static void unregisterAddon(RebarAddon addon) {
        try {
            sendOpMessage("Unregistering ", addon.getJavaPlugin().getName(), " BlockStorage#cleanup");
            ReflectionUtil.invokeMethod(BlockStorage.INSTANCE, "cleanup$rebar", addon);
        } catch (Exception e) {
            MSUA.console(e);
        }
        try {
            sendOpMessage("Unregistering ", addon.getJavaPlugin().getName(), " EntityStorage#cleanup");
            ReflectionUtil.invokeMethod(EntityStorage.INSTANCE, "cleanup$rebar", addon);
        } catch (Exception e) {
            MSUA.console(e);
        }

        sendOpMessage("Unregistering ", addon.getJavaPlugin().getName(), " RebarRegistry.GAMETESTS#unregisterAllFromAddon");
        RebarRegistry.GAMETESTS.unregisterAllFromAddon(addon);

        sendOpMessage("Unregistering ", addon.getJavaPlugin().getName(), " GuidePage ItemButton");
        getGuidePages().values().forEach(p -> {
            List<Object> toRemove = new ArrayList<>();
            for (Object i : p.getButtons()) {
                if (!(i instanceof ItemButton ib)) {
                    continue;
                }

                RebarItem py = RebarItem.fromStack(ib.getCurrentStack());
                if (py == null) continue;
                if (py.getAddon() == addon) {
                    toRemove.add(i);
                }
            }
            p.getButtons().removeAll(toRemove);
        });
        sendOpMessage("Unregistering ", addon.getJavaPlugin().getName(), " RebarRegistry.ITEMS#unregisterAllFromAddon");
        RebarRegistry.ITEMS.unregisterAllFromAddon(addon);

        sendOpMessage("Unregistering ", addon.getJavaPlugin().getName(), " RebarRegistry.ITEM_TAGS#unregisterAllFromAddon");
        RebarRegistry.ITEM_TAGS.unregisterAllFromAddon(addon);

        sendOpMessage("Unregistering ", addon.getJavaPlugin().getName(), " GuidePage FluidButton");

        getGuidePages().values().forEach(p -> {
            List<Object> toRemove = new ArrayList<>();
            for (Object i : p.getButtons()) {
                if (!(i instanceof FluidButton fb)) {
                    continue;
                }

                if (fb.getCurrentFluid().getKey().getNamespace().equals(addon.getKey().getNamespace())) {
                    toRemove.add(i);
                }
            }
            p.getButtons().removeAll(toRemove);
        });
        sendOpMessage("Unregistering ", addon.getJavaPlugin().getName(), " RebarRegistry.FLUIDS#unregisterAllFromAddon");
        RebarRegistry.FLUIDS.unregisterAllFromAddon(addon);

        sendOpMessage("Unregistering ", addon.getJavaPlugin().getName(), " RebarRegistry.BLOCKS#unregisterAllFromAddon");
        RebarRegistry.BLOCKS.unregisterAllFromAddon(addon);
        sendOpMessage("Unregistering ", addon.getJavaPlugin().getName(), " RebarRegistry.ENTITIES#unregisterAllFromAddon");
        RebarRegistry.ENTITIES.unregisterAllFromAddon(addon);
        sendOpMessage("Unregistering ", addon.getJavaPlugin().getName(), " RebarRegistry.RECIPE_TYPES#unregisterAllFromAddon");
        RebarRegistry.RECIPE_TYPES.unregisterAllFromAddon(addon);

        sendOpMessage("Unregistering ", addon.getJavaPlugin().getName(), " GuidePage ResearchButton");
        getGuidePages().values().forEach(p -> {
            List<Object> toRemove = new ArrayList<>();
            for (Object i : p.getButtons()) {
                if (!(i instanceof ResearchButton rb)) {
                    continue;
                }

                if (rb.getResearch().getKey().getNamespace().equals(addon.getKey().getNamespace())) {
                    toRemove.add(i);
                }
            }
            p.getButtons().removeAll(toRemove);
        });
        sendOpMessage("Unregistering ", addon.getJavaPlugin().getName(), " RebarRegistry.RESEARCHES#unregisterAllFromAddon");
        RebarRegistry.RESEARCHES.unregisterAllFromAddon(addon);

        sendOpMessage("Unregistering ", addon.getJavaPlugin().getName(), " GuidePage PageButton");
        getGuidePages().values().forEach(p -> {
            List<Object> toRemove = new ArrayList<>();
            for (Object i : p.getButtons()) {
                if (!(i instanceof PageButton pb)) {
                    continue;
                }

                if (pb.getPage().getKey().getNamespace().equals(addon.getKey().getNamespace())) {
                    toRemove.add(i);
                }
            }
            p.getButtons().removeAll(toRemove);
        });
        sendOpMessage("Unregistering ", addon.getJavaPlugin().getName(), " RebarRegistry.ADDONS#unregister");
        if (RebarRegistry.ADDONS.contains(addon.getKey())) {
            RebarRegistry.ADDONS.unregister(addon);
        }
    }

    public static Map<NamespacedKey, SimpleStaticGuidePage> getGuidePages() {
        var pages = getSubPages(RebarGuide.getRootPage());
        pages.put(RebarGuide.getRootPage().getKey(), RebarGuide.getRootPage());
        return pages;
    }

    public static Map<NamespacedKey, SimpleStaticGuidePage> getSubPages(SimpleStaticGuidePage page) {
        var pages = new HashMap<NamespacedKey, SimpleStaticGuidePage>();
        for (Object button : page.getButtons()) {
            if (button instanceof PageButton) {
                var subPage = ((PageButton) button).getPage();
                if (subPage instanceof SimpleStaticGuidePage ssg) {
                    pages.put(subPage.getKey(), ssg);
                    pages.putAll(getSubPages(ssg));
                }
            }
        }
        return pages;
    }

    public static void sendOpMessage(Object... msg) {
        Object[] objs = new Object[msg.length+1];
        objs[0] = "[RebarExtension] ";
        System.arraycopy(msg, 0, objs, 1, msg.length);
        MSUA.sendOpMessage(objs);
        MSUA.console(objs);
    }
}
