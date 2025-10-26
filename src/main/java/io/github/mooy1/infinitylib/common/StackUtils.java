package io.github.mooy1.infinitylib.common;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import lombok.experimental.UtilityClass;

import org.bukkit.Material;
// import org.bukkit.NamespacedKey; // Tidak terpakai lagi
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
// import org.bukkit.persistence.PersistentDataType; // Tidak terpakai lagi

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
// import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack; // Tidak terpakai lagi
// import io.github.thebusybiscuit.slimefun4.implementation.Slimefun; // Tidak terpakai lagi

@UtilityClass
@ParametersAreNonnullByDefault
public final class StackUtils {

    // private static final NamespacedKey ID_KEY = Slimefun.getItemDataService().getKey(); // Tidak terpakai lagi

    @Nullable
    public static String getId(ItemStack item) {
        // Diperbaiki: Menggunakan SlimefunItem.getByItem untuk mendapatkan ID
        SlimefunItem sfItem = SlimefunItem.getByItem(item);
        return sfItem == null ? null : sfItem.getId();
    }

    /*
    @Nullable
    public static String getId(ItemMeta meta) {
        // Metode ini tidak lagi aman digunakan secara langsung, gunakan getByItem
        return meta.getPersistentDataContainer().get(ID_KEY, PersistentDataType.STRING);
    }
    */

    @Nonnull
    public static String getIdOrType(ItemStack item) {
        // Diperbaiki: Menggunakan SlimefunItem.getByItem
        SlimefunItem sfItem = SlimefunItem.getByItem(item);
        if (sfItem != null) {
            return sfItem.getId();
        } else {
            return item.getType().name();
        }
    }

    @Nullable
    public static ItemStack itemById(String id) {
        SlimefunItem item = SlimefunItem.getById(id);
        return item == null ? null : item.getItem().clone();
    }

    @Nonnull
    public static ItemStack itemByIdOrType(String idOrType) {
        SlimefunItem item = SlimefunItem.getById(idOrType);
        return item == null ? new ItemStack(Material.valueOf(idOrType)) : item.getItem().clone();
    }

    /**
     * Returns true when both items:
     * - Are null or air
     * - Have the same slimefun id
     * - Have the same type and display name or lack thereof (if vanilla)
     */
    public static boolean isSimilar(@Nullable ItemStack first, @Nullable ItemStack second) {
        if (first == null || first.getType().isAir()) {
            return second == null || second.getType().isAir();
        } else if (second == null || second.getType().isAir()) {
            return false;
        }

        // Diperbaiki: Logika perbandingan untuk API Slimefun modern
        SlimefunItem sfFirst = SlimefunItem.getByItem(first);
        SlimefunItem sfSecond = SlimefunItem.getByItem(second);

        if (sfFirst != null) {
            // Item pertama adalah item Slimefun
            return sfSecond != null && sfFirst.getId().equals(sfSecond.getId());
        } else if (sfSecond != null) {
            // Item pertama vanilla, item kedua Slimefun
            return false;
        }

        // Kedua item adalah vanilla, gunakan logika lama (tanpa cek ID)
        if (first.hasItemMeta()) {
            if (second.hasItemMeta()) {
                ItemMeta firstMeta = first.getItemMeta();
                ItemMeta secondMeta = second.getItemMeta();

                if (first.getType() != second.getType()) {
                    return false;
                }

                if (firstMeta.hasDisplayName()) {
                    return secondMeta.hasDisplayName()
                            && firstMeta.getDisplayName().equals(secondMeta.getDisplayName());
                } else {
                    return !secondMeta.hasDisplayName();
                }
            } else {
                return false;
            }
        } else if (second.hasItemMeta()) {
            return false;
        } else {
            return first.getType() == second.getType();
        }
    }

}
