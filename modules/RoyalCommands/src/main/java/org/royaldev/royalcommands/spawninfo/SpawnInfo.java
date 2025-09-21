/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.spawninfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;

/**
 * A class used in item-spawning to determine and store information about the spawn status of an item.
 * <br>
 * This class is {@link Serializable}, but it can also be stored as a String, as seen here:
 * {@link org.royaldev.royalcommands.spawninfo.SpawnInfo#toString()}.
 *
 * @author jkcclemens
 * @since 3.2.1
 */
public class SpawnInfo implements Serializable {

    private static final long serialVersionUID = 3232013L;
    /**
     * List of <em>spawned</em> components used in the creation of this item.
     */
    private final List<String> components = new ArrayList<>();
    /**
     * Name of player that spawned this item. If item is not spawned, this should be null. If it has no spawner, this
     * should be null.
     */
    private String spawner;
    /**
     * Whether the item is spawned or not.
     */
    private boolean spawned;
    /**
     * Whether the item was made with spawned components.
     */
    private boolean hasComponents;

    /**
     * Constructs a SpawnInfo object assuming that the item was not spawned.
     */
    public SpawnInfo() {
        this.spawner = null;
        this.spawned = false;
        this.hasComponents = false;
    }

    /**
     * Constructs a SpawnInfo object from a stored String.
     *
     * @param stored String to restore SpawnInfo from
     * @throws IllegalArgumentException If <code>stored</code> is null or invalid
     * @see org.royaldev.royalcommands.spawninfo.SpawnInfo#toString()
     */
    public SpawnInfo(final String stored) {
        if (stored == null) throw new IllegalArgumentException("String cannot be null!");
        final String[] splitStored = stored.split("/");
        if (splitStored.length < 4) throw new IllegalArgumentException("Invalid stored string!");
        this.spawner = splitStored[1];
        this.spawned = "true".equalsIgnoreCase(splitStored[0]);
        this.hasComponents = "true".equalsIgnoreCase(splitStored[2]);
        if (splitStored[3].startsWith("[") && splitStored[3].endsWith("]")) {
            this.components.addAll(Arrays.asList(splitStored[3].substring(1, splitStored[3].length() - 1).split(", ")));
        }
    }

    /**
     * Constructs a SpawnInfo object, assigning values to "spawner" and "spawned."
     *
     * @param spawner Name of player that spawned the item
     * @param spawned If the item is spawned
     */
    public SpawnInfo(final String spawner, final boolean spawned) {
        this.spawner = spawner;
        this.spawned = spawned;
        this.hasComponents = false;
    }

    /**
     * Constructs a SpawnInfo object, assigning default values.
     *
     * @param spawner       Name of player that spawned item
     * @param spawned       If the item is spawned
     * @param hasComponents If the item was made with spawned items
     * @param components    The spawned items the item was made with
     */
    public SpawnInfo(final String spawner, final boolean spawned, boolean hasComponents, Collection<String> components) {
        this.spawner = spawner;
        this.spawned = spawned;
        this.hasComponents = hasComponents;
        this.components.addAll(components);
    }

    /**
     * Gets the <em>spawned</em> components used to make this item. If there are none, an empty list will be returned.
     * Never returns null.
     *
     * @return List of spawned components; never null
     */
    public List<String> getComponents() {
        return components;
    }

    /**
     * Gets the name of the player that spawned this item. <strong>May be null</strong> if the item was not spawned or
     * has no spawner.
     *
     * @return String or null
     */
    public String getSpawner() {
        return spawner;
    }

    /**
     * Sets the name of the player that spawned this item. Use null if the item was not spawned or has no spawner.
     *
     * @param spawner Name of player or null
     */
    public void setSpawner(String spawner) {
        this.spawner = spawner;
    }

    /**
     * Returns if the item was made with spawned components.
     *
     * @return boolean
     */
    public boolean hasComponents() {
        return hasComponents;
    }

    /**
     * Returns if the item is spawned.
     *
     * @return boolean
     */
    public boolean isSpawned() {
        return spawned;
    }

    /**
     * Sets if the item is spawned.
     *
     * @param spawned true if it was spawned, false if not
     */
    public void setSpawned(boolean spawned) {
        this.spawned = spawned;
    }

    /**
     * Sets if the item was made with spawned components.
     *
     * @param hasComponents true if made with spawned components, false if not
     */
    public void setHasComponents(boolean hasComponents) {
        this.hasComponents = hasComponents;
    }

    /**
     * Gets the SpawnInfo object as a String. This String can be used to reconstruct the same SpawnInfo object.
     *
     * @return String representing SpawnInfo object
     * @see SpawnInfo#SpawnInfo(String)
     */
    @Override
    public String toString() {
        return String.valueOf(this.spawned) + "/" + this.spawner + "/" + this.hasComponents + "/" + this.components.toString();
    }

    public static final class SpawnInfoManager {
        /**
         * Applies spawn information to an ItemStack.
         * <br>
         * <strong>Note:</strong> ItemStacks containing AIR (ID: 0) will not have information applied, but will still
         * return the ItemStack.
         *
         * @param is ItemStack to apply information to
         * @param si SpawnInfo to apply
         * @return ItemStack with SpawnInfo applied; never null
         */
        public static ItemStack applySpawnInfo(ItemStack is, SpawnInfo si) {
            return applySpawnInfo(is, si.toString());
        }

        /**
         * Applies spawn information to an ItemStack.
         * <br>
         * <strong>Note:</strong> ItemStacks containing AIR (ID: 0) will not have information applied, but will still
         * return the ItemStack.
         *
         * @param is ItemStack to apply information to
         * @param s  SpawnInfo to apply (String form)
         * @return ItemStack with SpawnInfo applied; never null
         */
        public static ItemStack applySpawnInfo(ItemStack is, String s) {
            if (RUtils.isBlockAir(is.getType())) return is; // air; do not apply
            final ItemMeta im = is.getItemMeta();
            im.getPersistentDataContainer().set(new NamespacedKey(RoyalCommands.getInstance(), "spawned"), PersistentDataType.STRING, s);
            is.setItemMeta(im);
            return is;
        }

        /**
         * Retrieve the data stored in the item's data container.
         *
         * @return The stored data, or defaultValue if not found.
         */
        public static String getData(ItemStack is) {
            ItemMeta im = is.getItemMeta();
            return im.getPersistentDataContainer().get(new NamespacedKey(RoyalCommands.getInstance(), "spawned"), PersistentDataType.STRING);
        }

        /**
         * Gets SpawnInfo from an ItemStack. If the ItemStack has no SpawnInfo attached, a default SpawnInfo will be
         * returned.
         * <br>
         * <strong>Note:</strong> ItemStacks containing AIR (ID: 0) will always return a blank SpawnInfo, since they
         * cannot store NBT data.
         *
         * @param is ItemStack to obtain SpawnInfo from
         * @return SpawnInfo; never null
         */
        public static SpawnInfo getSpawnInfo(ItemStack is) {
            if (RUtils.isBlockAir(is.getType())) return new SpawnInfo(); // air cannot contain NBT data
            String stored = SpawnInfoManager.getData(is);
            if (stored == null || stored.isEmpty()) stored = "false/null/false/null";
            return new SpawnInfo(stored);
        }

        /**
         * Removes the data stored in the data container.
         */
        public static void removeData(ItemStack is) {
            ItemMeta meta = is.getItemMeta();
            PersistentDataContainer dc = meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(RoyalCommands.getInstance(), "spawned");
            if (dc.has(key, PersistentDataType.STRING)) {
                dc.remove(key);
                is.setItemMeta(meta);
            }
        }

        /**
         * Removes all SpawnInfo from an ItemStack, leaving it as it was prior to SpawnInfo application.
         *
         * @param is ItemStack to remove SpawnInfo from
         * @return ItemStack without SpawnInfo
         */
        public static ItemStack removeSpawnInfo(ItemStack is) {
            if (RUtils.isBlockAir(is.getType())) return is; // silly air
            SpawnInfoManager.removeData(is);
            return is;
        }
    }
}
