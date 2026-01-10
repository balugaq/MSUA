package com.balugaq.msua;

import io.github.pylonmc.pylon.core.addon.PylonAddon;
import io.github.pylonmc.pylon.core.block.BlockStorage;
import io.github.pylonmc.pylon.core.content.guide.PylonGuide;
import io.github.pylonmc.pylon.core.entity.EntityStorage;
import io.github.pylonmc.pylon.core.guide.button.FluidButton;
import io.github.pylonmc.pylon.core.guide.button.ItemButton;
import io.github.pylonmc.pylon.core.guide.button.PageButton;
import io.github.pylonmc.pylon.core.guide.button.ResearchButton;
import io.github.pylonmc.pylon.core.guide.pages.base.SimpleStaticGuidePage;
import io.github.pylonmc.pylon.core.item.PylonItem;
import io.github.pylonmc.pylon.core.registry.PylonRegistry;
import lombok.experimental.UtilityClass;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.ApiStatus;
import xyz.xenondevs.invui.item.Item;

import java.util.HashMap;
import java.util.Map;

@ApiStatus.Obsolete
@UtilityClass
public class PylonUnregisterUtil {
    public static void unregisterAddon(PylonAddon addon) {
        try {
            ReflectionUtil.invokeMethod(BlockStorage.INSTANCE, "cleanup$pylon_core", addon);
        } catch (Exception e) {
            MSUA.console(e);
        }
        try {
            ReflectionUtil.invokeMethod(EntityStorage.INSTANCE, "cleanup$pylon_core", addon);
        } catch (Exception e) {
            MSUA.console(e);
        }

        PylonRegistry.GAMETESTS.unregisterAllFromAddon(addon);

        getGuidePages().values().forEach(p -> p.getButtons().removeIf(i -> {
            if (!(i instanceof ItemButton ib)) {
                return false;
            }

            PylonItem py = PylonItem.fromStack(ib.getCurrentStack());
            if (py == null) return false;
            return py.getAddon() == addon;
        }));
        PylonRegistry.ITEMS.unregisterAllFromAddon(addon);

        PylonRegistry.ITEM_TAGS.unregisterAllFromAddon(addon);

        getGuidePages().values().forEach(p -> p.getButtons().removeIf(i -> {
            if (!(i instanceof FluidButton fb)) {
                return false;
            }

            return fb.getCurrentFluid().getKey().getNamespace().equals(addon.getKey().getNamespace());
        }));
        PylonRegistry.FLUIDS.unregisterAllFromAddon(addon);

        PylonRegistry.BLOCKS.unregisterAllFromAddon(addon);
        PylonRegistry.ENTITIES.unregisterAllFromAddon(addon);
        PylonRegistry.RECIPE_TYPES.unregisterAllFromAddon(addon);

        getGuidePages().values().forEach(p -> p.getButtons().removeIf(i -> {
            if (!(i instanceof ResearchButton rb)) {
                return false;
            }

            return rb.getResearch().getKey().getNamespace().equals(addon.getKey().getNamespace());
        }));
        PylonRegistry.RESEARCHES.unregisterAllFromAddon(addon);

        getGuidePages().values().forEach(p -> p.getButtons().removeIf(i -> {
            if (!(i instanceof PageButton pb)) {
                return false;
            }

            return pb.getPage().getKey().getNamespace().equals(addon.getKey().getNamespace());
        }));

        if (PylonRegistry.ADDONS.contains(addon.getKey())) {
            PylonRegistry.ADDONS.unregister(addon);
        }
    }

    public static Map<NamespacedKey, SimpleStaticGuidePage> getGuidePages() {
        var pages = getSubPages(PylonGuide.getRootPage());
        pages.put(PylonGuide.getRootPage().getKey(), PylonGuide.getRootPage());
        return pages;
    }

    public static Map<NamespacedKey, SimpleStaticGuidePage> getSubPages(SimpleStaticGuidePage page) {
        var pages = new HashMap<NamespacedKey, SimpleStaticGuidePage>();
        for (Item button : page.getButtons()) {
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
