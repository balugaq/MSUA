package com.balugaq.msua;

import io.github.pylonmc.rebar.Rebar;
import io.github.pylonmc.rebar.addon.RebarAddon;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.PhantomBlock;
import io.github.pylonmc.rebar.content.guide.RebarGuide;
import io.github.pylonmc.rebar.entity.EntityStorage;
import io.github.pylonmc.rebar.event.RebarBlockUnloadEvent;
import io.github.pylonmc.rebar.guide.button.FluidButton;
import io.github.pylonmc.rebar.guide.button.ItemButton;
import io.github.pylonmc.rebar.guide.button.PageButton;
import io.github.pylonmc.rebar.guide.button.ResearchButton;
import io.github.pylonmc.rebar.guide.pages.base.SimpleStaticGuidePage;
import io.github.pylonmc.rebar.item.RebarItem;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
            MSUA.sendOpMessage("Unregistering ", addon.getDisplayName(), " BlockStorage#cleanup");
            ReflectionUtil.invokeMethod(BlockStorage.INSTANCE, "cleanup$rebar", addon);
        } catch (Exception e) {
            MSUA.console(e);
        }
        try {
            MSUA.sendOpMessage("Unregistering ", addon.getDisplayName(), " EntityStorage#cleanup");
            ReflectionUtil.invokeMethod(EntityStorage.INSTANCE, "cleanup$rebar", addon);
        } catch (Exception e) {
            MSUA.console(e);
        }

        MSUA.sendOpMessage("Unregistering ", addon.getDisplayName(), " RebarRegistry.GAMETESTS#unregisterAllFromAddon");
        RebarRegistry.GAMETESTS.unregisterAllFromAddon(addon);

        MSUA.sendOpMessage("Unregistering ", addon.getDisplayName(), " GuidePage ItemButton");
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
        MSUA.sendOpMessage("Unregistering ", addon.getDisplayName(), " RebarRegistry.ITEMS#unregisterAllFromAddon");
        RebarRegistry.ITEMS.unregisterAllFromAddon(addon);

        MSUA.sendOpMessage("Unregistering ", addon.getDisplayName(), " RebarRegistry.ITEM_TAGS#unregisterAllFromAddon");
        RebarRegistry.ITEM_TAGS.unregisterAllFromAddon(addon);

        MSUA.sendOpMessage("Unregistering ", addon.getDisplayName(), " GuidePage FluidButton");

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
        MSUA.sendOpMessage("Unregistering ", addon.getDisplayName(), " RebarRegistry.FLUIDS#unregisterAllFromAddon");
        RebarRegistry.FLUIDS.unregisterAllFromAddon(addon);

        MSUA.sendOpMessage("Unregistering ", addon.getDisplayName(), " RebarRegistry.BLOCKS#unregisterAllFromAddon");
        RebarRegistry.BLOCKS.unregisterAllFromAddon(addon);
        MSUA.sendOpMessage("Unregistering ", addon.getDisplayName(), " RebarRegistry.ENTITIES#unregisterAllFromAddon");
        RebarRegistry.ENTITIES.unregisterAllFromAddon(addon);
        MSUA.sendOpMessage("Unregistering ", addon.getDisplayName(), " RebarRegistry.RECIPE_TYPES#unregisterAllFromAddon");
        RebarRegistry.RECIPE_TYPES.unregisterAllFromAddon(addon);

        MSUA.sendOpMessage("Unregistering ", addon.getDisplayName(), " GuidePage ResearchButton");
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
        MSUA.sendOpMessage("Unregistering ", addon.getDisplayName(), " RebarRegistry.RESEARCHES#unregisterAllFromAddon");
        RebarRegistry.RESEARCHES.unregisterAllFromAddon(addon);

        MSUA.sendOpMessage("Unregistering ", addon.getDisplayName(), " GuidePage PageButton");
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
        MSUA.sendOpMessage("Unregistering ", addon.getDisplayName(), " RebarRegistry.ADDONS#unregister");
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

    public static void disablePlugin(RebarAddon ra, Set<Location> normals) {
        // call unload event for blocks from the addon
        for (var rebar : normals) {
            if (BlockStorage.get(rebar.getBlock()) instanceof PhantomBlock pb) {
                new RebarBlockUnloadEvent(pb.getBlock(), pb).callEvent();
            }
        }
    }

    public static void enablePlugin(RebarAddon ra) {
        Map<Location, UUID> phantoms = new HashMap<>();
        for (var rebar : BlockStorage.getLoadedRebarBlocks()) {
            if (!(rebar instanceof PhantomBlock pb)) continue;
            phantoms.put(pb.getBlock().getLocation(), ReflectionUtil.getValue(pb, "errorOutlineEntityId", UUID.class));
        }

        // make BlockStorage reload rebar data
        Object tasks = ReflectionUtil.getValue(BlockStorage.INSTANCE, "chunkAutosaveTasks");
        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                Object job = ReflectionUtil.invokeMethod(tasks, "remove", new ChunkPosition(chunk));
                if (job != null) ReflectionUtil.invokeMethod(job, "cancel");
                ReflectionUtil.invokeMethod(RebarUtil.getBlockStorageInstance(), "onChunkLoad", new ChunkLoadEvent(chunk, false));
            }
        }

        // remove phantom outlines
        for (var entry : phantoms.entrySet()) {
            if (!(BlockStorage.get(entry.getKey()) instanceof PhantomBlock)) {
                Entity entity = entry.getKey().getWorld().getEntity(entry.getValue());
                if (entity != null) entity.remove();
            }
        }
        MSUA.sendOpMessage("[RebarExtension] Reloaded chunks");
    }
}
