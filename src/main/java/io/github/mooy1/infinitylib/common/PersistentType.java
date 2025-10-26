package io.github.mooy1.infinitylib.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;

// import lombok.RequiredArgsConstructor; // Dihapus
// import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack; // Dihapus Sesuai Permintaan

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

/**
 * A class with some persistent data types
 *
 * @author Mooy1
 */
// @RequiredArgsConstructor // Anotasi ini dihapus
@ParametersAreNonnullByDefault
public final class PersistentType<T, Z> implements PersistentDataType<T, Z> {

    // Konstruktor manual ditambahkan untuk menggantikan @RequiredArgsConstructor
    public PersistentType(Class<T> primitive, Class<Z> complex, Function<Z, T> toPrimitive, Function<T, Z> toComplex) {
        this.primitive = primitive;
        this.complex = complex;
        this.toPrimitive = toPrimitive;
        this.toComplex = toComplex;
    }

    public static final PersistentDataType<byte[], ItemStack> ITEM_STACK = new PersistentType<>(
            byte[].class, ItemStack.class,
            itemStack -> {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                try (BukkitObjectOutputStream output = new BukkitObjectOutputStream(bytes)) {
                    output.writeObject(itemStack);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                return bytes.toByteArray();
            },
            arr -> {
                ByteArrayInputStream bytes = new ByteArrayInputStream(arr);
                try (BukkitObjectInputStream input = new BukkitObjectInputStream(bytes)) {
                    return (ItemStack) input.readObject();
                }
                catch (Exception e) {
                    e.printStackTrace();
                    return new SlimefunItemStack(
                            "ERROR_ITEM",   // ID unik untuk Slimefun
                            Material.STONE,
                            "&cERROR"
                    ).item();
                }
            }
    );

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static final PersistentDataType<byte[], List<ItemStack>> ITEM_STACK_LIST = new PersistentType<>(
            byte[].class, (Class) List.class,
            (List<ItemStack> itemList) -> { // Diperbaiki: Menambahkan tipe parameter eksplisit
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                try (BukkitObjectOutputStream output = new BukkitObjectOutputStream(bytes)) {
                    for (ItemStack item : itemList) { // Diperbaiki: Iterasi sebagai ItemStack
                        output.writeObject(item); // Diperbaiki: Tidak perlu cast
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                return bytes.toByteArray();
            },
            arr -> {
                ByteArrayInputStream bytes = new ByteArrayInputStream(arr);
                List<ItemStack> list = new ArrayList<>();
                try (BukkitObjectInputStream input = new BukkitObjectInputStream(bytes)) {
                    while (bytes.available() > 0) {
                        list.add((ItemStack) input.readObject());
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                return list;
            }
    );

    public static final PersistentDataType<byte[], Location> LOCATION = new PersistentType<>(
            byte[].class, Location.class,
            location -> {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                try (BukkitObjectOutputStream output = new BukkitObjectOutputStream(bytes)) {
                    output.writeObject(location);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                return bytes.toByteArray();
            },
            arr -> {
                ByteArrayInputStream bytes = new ByteArrayInputStream(arr);
                try (BukkitObjectInputStream input = new BukkitObjectInputStream(bytes)) {
                    return (Location) input.readObject();
                }
                catch (Exception e) {
                    e.printStackTrace();
                    return new Location(null, 0, 0, 0);
                }
            }
    );

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static final PersistentDataType<byte[], List<String>> STRING_LIST = new PersistentType<>(
            byte[].class, (Class) List.class,
            (List<String> stringList) -> { // Diperbaiki: Menambahkan tipe parameter eksplisit
                ByteArrayOutputStream bytes = new ByteArrayOutputStream(stringList.size() * 20); // Diperbaiki: Gunakan stringList
                for (String string : stringList) { // Diperbaiki: Gunakan stringList
                    byte[] arr = string.getBytes();
                    bytes.write(arr.length);
                    bytes.write(arr, 0, arr.length);
                }
                return bytes.toByteArray();
            },
            arr -> {
                ByteArrayInputStream bytes = new ByteArrayInputStream(arr);
                List<String> list = new ArrayList<>(arr.length / 20);
                try {
                    while (bytes.available() > 0) {
                        byte[] string = new byte[bytes.read()];
                        bytes.read(string, 0, string.length);
                        list.add(new String(string));
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                return list;
            }
    );

    /**
     * Only use this if you were using it prior to the slimefun breaking changes
     */
    @Deprecated
    public static final PersistentDataType<String, ItemStack> ITEM_STACK_OLD = new PersistentType<>(
            String.class, ItemStack.class,
            (ItemStack itemStack) -> { // Diperbaiki: Menambahkan tipe parameter eksplisit
                YamlConfiguration config = new YamlConfiguration();
                config.set("item", itemStack);
                return config.saveToString();
            },
            string -> {
                YamlConfiguration config = new YamlConfiguration();
                try {
                    config.loadFromString(string);
                }
                catch (InvalidConfigurationException e) {
                    e.printStackTrace();
                    // Diperbaiki: Menggunakan SlimefunItemStack sesuai permintaan
                    return new SlimefunItemStack("ERROR_ITEM_OLD", Material.STONE, "&cERROR (OLD)").item();
                }
                ItemStack item = config.getItemStack("item");
                // Diperbaiki: Menggunakan SlimefunItemStack sesuai permintaan
                return item != null ? item : new SlimefunItemStack("ERROR_ITEM_OLD", Material.STONE, "&cERROR (OLD)").item();
            }
    );

    private final Class<T> primitive;
    private final Class<Z> complex;
    private final Function<Z, T> toPrimitive;
    private final Function<T, Z> toComplex;

    @Nonnull
    @Override
    public Class<T> getPrimitiveType() {
        return primitive;
    }

    @Nonnull
    @Override
    public Class<Z> getComplexType() {
        return complex;
    }

    @Nonnull
    @Override
    public T toPrimitive(Z complex, PersistentDataAdapterContext context) {
        return toPrimitive.apply(complex);
    }

    @Nonnull
    @Override
    public Z fromPrimitive(T primitive, PersistentDataAdapterContext context) {
        return toComplex.apply(primitive);
    }

}

