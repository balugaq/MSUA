package com.balugaq.msua;

import io.github.pylonmc.rebar.addon.RebarAddon;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.content.guide.RebarGuide;
import io.github.pylonmc.rebar.entity.EntityStorage;
import io.github.pylonmc.rebar.guide.button.FluidButton;
import io.github.pylonmc.rebar.guide.button.ItemButton;
import io.github.pylonmc.rebar.guide.button.PageButton;
import io.github.pylonmc.rebar.guide.button.ResearchButton;
import io.github.pylonmc.rebar.guide.pages.base.SimpleStaticGuidePage;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.registry.RebarRegistry;
import lombok.experimental.UtilityClass;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApiStatus.Obsolete
@UtilityClass
public class RebarUnregisterUtil {
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
}
