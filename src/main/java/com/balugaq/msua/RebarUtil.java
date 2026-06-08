package com.balugaq.msua;

import io.github.pylonmc.pylon.content.machines.smelting.SmelteryController;
import io.github.pylonmc.rebar.Rebar;
import io.github.pylonmc.rebar.addon.RebarAddon;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.PhantomBlock;
import io.github.pylonmc.rebar.block.RebarBlock;
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
            sendOpMessage("Unregistering ", addon.getDisplayName(), " BlockStorage#cleanup");
            ReflectionUtil.invokeMethod(BlockStorage.INSTANCE, "cleanup$rebar", addon);
        } catch (Exception e) {
            MSUA.console(e);
        }
        try {
            sendOpMessage("Unregistering ", addon.getDisplayName(), " EntityStorage#cleanup");
            ReflectionUtil.invokeMethod(EntityStorage.INSTANCE, "cleanup$rebar", addon);
        } catch (Exception e) {
            MSUA.console(e);
        }

        sendOpMessage("Unregistering ", addon.getDisplayName(), " RebarRegistry.GAMETESTS#unregisterAllFromAddon");
        RebarRegistry.GAMETESTS.unregisterAllFromAddon(addon);

        sendOpMessage("Unregistering ", addon.getDisplayName(), " GuidePage ItemButton");
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
        sendOpMessage("Unregistering ", addon.getDisplayName(), " RebarRegistry.ITEMS#unregisterAllFromAddon");
        RebarRegistry.ITEMS.unregisterAllFromAddon(addon);

        sendOpMessage("Unregistering ", addon.getDisplayName(), " RebarRegistry.ITEM_TAGS#unregisterAllFromAddon");
        RebarRegistry.ITEM_TAGS.unregisterAllFromAddon(addon);

        sendOpMessage("Unregistering ", addon.getDisplayName(), " GuidePage FluidButton");

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
        sendOpMessage("Unregistering ", addon.getDisplayName(), " RebarRegistry.FLUIDS#unregisterAllFromAddon");
        RebarRegistry.FLUIDS.unregisterAllFromAddon(addon);

        sendOpMessage("Unregistering ", addon.getDisplayName(), " RebarRegistry.BLOCKS#unregisterAllFromAddon");
        RebarRegistry.BLOCKS.unregisterAllFromAddon(addon);
        sendOpMessage("Unregistering ", addon.getDisplayName(), " RebarRegistry.ENTITIES#unregisterAllFromAddon");
        RebarRegistry.ENTITIES.unregisterAllFromAddon(addon);
        sendOpMessage("Unregistering ", addon.getDisplayName(), " RebarRegistry.RECIPE_TYPES#unregisterAllFromAddon");
        RebarRegistry.RECIPE_TYPES.unregisterAllFromAddon(addon);

        sendOpMessage("Unregistering ", addon.getDisplayName(), " GuidePage ResearchButton");
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
        sendOpMessage("Unregistering ", addon.getDisplayName(), " RebarRegistry.RESEARCHES#unregisterAllFromAddon");
        RebarRegistry.RESEARCHES.unregisterAllFromAddon(addon);

        sendOpMessage("Unregistering ", addon.getDisplayName(), " GuidePage PageButton");
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
        sendOpMessage("Unregistering ", addon.getDisplayName(), " RebarRegistry.ADDONS#unregister");
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

    public static void disableAddon(RebarAddon ra, Map<Location, RebarBlock> normals) {
        sendOpMessage("Calling RebarBlockUnloadEvent");
        // call unload event for blocks from the addon
        for (var loc : normals.keySet()) {
            if (BlockStorage.get(loc) instanceof PhantomBlock pb) {
                new RebarBlockUnloadEvent(pb.getBlock(), pb).callEvent();
            }
        }

        if (ra.getJavaPlugin().getName().equals("Pylon")) {
            sendOpMessage("Handling SmelteryController");
            for (var rebar : normals.values()) {
                if (rebar instanceof SmelteryController sc) {
                    // pixels are not persistent, in order to simulate the server stopping, remove the pixels.
                    ReflectionUtil.invokeMethod(sc, "removePixels");
                }
            }
        }
    }

    /**
     * @see Rebar#loadRecipes()
     */
    public static void enableAddon(RebarAddon ra) {
        sendOpMessage("Reloading recipes");

        for (Object typeObj : RebarRegistry.RECIPE_TYPES) {
            if (!(typeObj instanceof ConfigurableRecipeType<?> type)) continue;

            ConfigSection config = (ConfigSection) ReflectionUtil.invokeStaticMethod(ConfigSection.class, "fromResource", ra.getJavaPlugin(), ReflectionUtil.getValue(type, "filePath", String.class));
            if (config == null) continue;
            type.loadFromConfig(config);
        }

        Path recipesDir = Rebar.INSTANCE.getDataPath().resolve("recipes");
        if (Files.exists(recipesDir)) {
            try (Stream<Path> recipeDirs = Files.list(recipesDir)) {
                for (Path recipeDir : (Iterable<Path>) recipeDirs::iterator) {
                    if (!Files.isDirectory(recipeDir)) continue;

                    String namespace = recipeDir.getFileName().toString();
                    if (namespace.contains(".")) {
                        namespace = namespace.substring(0, namespace.lastIndexOf('.'));
                    }

                    try (Stream<Path> recipePaths = Files.list(recipeDir)) {
                        for (Path recipePath : (Iterable<Path>) recipePaths::iterator) {
                            if (!Files.isRegularFile(recipePath)) continue;
                            String fileName = recipePath.getFileName().toString();
                            String extension = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.') + 1) : "";
                            if (!"yml".equals(extension) && !"yaml".equals(extension)) continue;

                            String nameWithoutExt = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
                            NamespacedKey key = new NamespacedKey(namespace, nameWithoutExt);

                            Object recipeTypeObj = RebarRegistry.RECIPE_TYPES.get(key);
                            if (!(recipeTypeObj instanceof ConfigurableRecipeType<?> type)) continue;

                            ConfigSection config = (ConfigSection) ReflectionUtil.invokeStaticMethod(ConfigSection.class, "fromOrThrow", recipePath);
                            type.loadFromConfig(config);
                        }
                    }
                }
            } catch (IOException e) {
                sendOpMessage("Failed to read recipes directory: " + e.getMessage());
                e.printStackTrace();
            }
        }

        sendOpMessage("Reloading chunks");
        Map<Location, UUID> phantoms = new HashMap<>();
        for (var rebar : BlockStorage.getLoadedRebarBlocks()) {
            if (!(rebar instanceof PhantomBlock pb)) continue;
            phantoms.put(pb.getBlock().getLocation(), ReflectionUtil.getValue(pb, "errorOutlineEntityId", UUID.class));
        }

        // make BlockStorage reload rebar data
        sendOpMessage("Reloading BlockStorage");
        Object tasks = ReflectionUtil.getValue(BlockStorage.INSTANCE, "chunkAutosaveTasks");
        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                Object job = ReflectionUtil.invokeMethod(tasks, "remove", new ChunkPosition(chunk));
                if (job != null) ReflectionUtil.invokeMethod(job, "cancel");
                ReflectionUtil.invokeMethod(RebarUtil.getBlockStorageInstance(), "onChunkLoad", new ChunkLoadEvent(chunk, false));
            }
        }

        // remove phantom outlines
        sendOpMessage("Removing phantom outlines");
        for (var entry : phantoms.entrySet()) {
            if (!(BlockStorage.get(entry.getKey()) instanceof PhantomBlock)) {
                Entity entity = entry.getKey().getWorld().getEntity(entry.getValue());
                if (entity != null) entity.remove();
            }
        }
        sendOpMessage("Reloaded chunks");
    }

    public static void sendOpMessage(Object... msg) {
        Object[] objs = new Object[msg.length+1];
        objs[0] = "[RebarExtension] ";
        System.arraycopy(msg, 0, objs, 1, msg.length);
        MSUA.sendOpMessage(objs);
    }
}
