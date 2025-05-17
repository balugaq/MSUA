package com.balugaq.msua;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.api.geo.GEOResource;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.Radioactive;
import io.github.thebusybiscuit.slimefun4.core.multiblocks.MultiBlockMachine;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@ApiStatus.Obsolete
@SuppressWarnings("deprecation")
@UtilityClass
public class UnregisterUtil {
    public static void unregisterItem(SlimefunItem item) {
        if (item instanceof Radioactive) {
            Slimefun.getRegistry().getRadioactiveItems().remove(item);
        }

        if (item instanceof GEOResource geor) {
            Slimefun.getRegistry().getGEOResources().remove(geor.getKey());
        }

        if (item instanceof MultiBlockMachine mbm) {
            Slimefun.getRegistry().getMultiBlocks().remove(mbm.getMultiBlock());
        }

        Slimefun.getRegistry().getTickerBlocks().remove(item.getId());
        Slimefun.getRegistry().getEnabledSlimefunItems().remove(item);

        Slimefun.getRegistry().getSlimefunItemIds().remove(item.getId());
        Slimefun.getRegistry().getAllSlimefunItems().remove(item);
        Slimefun.getRegistry().getMenuPresets().remove(item.getId());
        Slimefun.getRegistry().getBarteringDrops().remove(item.getItem());

        if (item.getRecipeType() == RecipeType.MOB_DROP) {
            var recipe = item.getRecipe();
            if (recipe.length < 5) {
                return;
            }

            var itemStack = recipe[4];
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                return;
            }

            var meta = itemStack.getItemMeta();
            if (meta == null) {
                return;
            }

            String mob = ChatColor.stripColor(meta.getDisplayName()
                    .toUpperCase(Locale.ROOT)
                    .replace(' ', '_'));

            try {
                EntityType entity = EntityType.valueOf(mob);
                Set<ItemStack> dropping = Slimefun.getRegistry().getMobDrops().get(entity);
                if (dropping == null) {
                    return;
                }

                var output = item.getRecipeOutput();
                dropping.remove(output);
                Slimefun.getRegistry().getMobDrops().put(entity, dropping);
            } catch (IllegalArgumentException ignored) {
                return;
            }
        }
    }

    public static void unregisterAddon(@NotNull SlimefunAddon addon) {
        unregisterAllItems(addon);
        unregisterItemGroups(addon);
        unregisterAllGEOResources(addon);
    }

    public static void unregisterAllItems(@NotNull SlimefunAddon addon) {
        List<SlimefunItem> items = new ArrayList<>(Slimefun.getRegistry().getAllSlimefunItems());
        for (SlimefunItem item : items) {
            if (item.getAddon() == addon) {
                unregisterItem(item);
            }
        }
    }

    public static void unregisterItemGroups(@NotNull SlimefunAddon addon) {
        Set<ItemGroup> itemGroups = new HashSet<>();
        for (ItemGroup itemGroup : Slimefun.getRegistry().getAllItemGroups()) {
            if (Objects.equals(itemGroup.getAddon(), addon)) {
                itemGroups.add(itemGroup);
            }
        }
        for (ItemGroup itemGroup : itemGroups) {
            unregisterItemGroup(itemGroup);
        }
    }

    public static void unregisterAllGEOResources(@NotNull SlimefunAddon addon) {
        List<NamespacedKey> toRemove = new ArrayList<>();
        var resources = Slimefun.getRegistry().getGEOResources();
        for (var key : resources.keySet()) {
            if (key.getNamespace().equalsIgnoreCase(addon.getName())) {
                toRemove.add(key);
            }
        }
        for (var key : toRemove) {
            resources.remove(key);
        }
    }

    public static void unregisterItemGroup(@Nullable ItemGroup itemGroup) {
        if (itemGroup == null) {
            return;
        }

        Slimefun.getRegistry().getAllItemGroups().remove(itemGroup);
    }
}
