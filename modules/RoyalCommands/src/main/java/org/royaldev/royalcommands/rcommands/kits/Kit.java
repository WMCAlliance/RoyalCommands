/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands.kits;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.shaded.com.sk89q.util.config.ConfigurationNode;
import org.royaldev.royalcommands.wrappers.player.RPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * A class representing a kit.
 */
public class Kit {

    private final String name;
    private final String description;
    private final List<ItemStack> items;
    private final long cooldown;

    public Kit(final String name, final ConfigurationNode kit) {
        this.name = name;
        this.items = this.getItemStacks(kit.getNodeList("items", new ArrayList<ConfigurationNode>()));
        this.cooldown = RUtils.timeFormatToSeconds(kit.getString("cooldown", "0"));
        this.description = RUtils.colorize(kit.getString("description", ""));
    }

    /**
     * Adds a list of represented enchantments to an ItemStack.
     *
     * @param is           ItemStack to add enchantments to
     * @param enchantments List of nodes representing enchantments
     * @return ItemStack with enchantments applied
     */
    private ItemStack addEnchantments(final ItemStack is, final List<ConfigurationNode> enchantments) {
        for (final ConfigurationNode enchantment : enchantments) {
            final Enchantment realEnchantment = this.getEnchantment(enchantment);
            if (realEnchantment == null) continue;
            is.addUnsafeEnchantment(realEnchantment, enchantment.getInt("level", 1));
        }
        return is;
    }

    /**
     * Gets an enchantment from a node representing it.
     *
     * @param enchantment Node
     * @return Enchantment (may be null)
     */
    private Enchantment getEnchantment(final ConfigurationNode enchantment) {
        return Enchantment.getByName(enchantment.getString("type", "").toUpperCase());
    }

    /**
     * Gets an ItemStack from the given node
     *
     * @param item Node representing an ItemStack
     * @return ItemStack of null
     */
    private ItemStack getItemStack(final ConfigurationNode item) {
        final ItemStack is = RoyalCommands.inm.getItemStackFromAlias(item.getString("type"));
        final Damageable imd = (Damageable) is.getItemMeta();
        if (is == null) return null;
        is.setAmount(item.getInt("amount", 1));
        imd.setDamage(item.getShort("damage", (short) 0));
        final ItemMeta im = is.getItemMeta();
        im.setDisplayName(RUtils.colorize(item.getString("name")));
        im.setLore(this.getLore(item));
        is.setItemMeta(im);
        this.addEnchantments(is, item.getNodeList("enchantments", new ArrayList<ConfigurationNode>()));
        return is;
    }

    /**
     * Gets all of the ItemStacks represented by the list of nodes.
     *
     * @param items List of nodes representing items
     * @return List of ItemStacks, never null
     */
    private List<ItemStack> getItemStacks(final List<ConfigurationNode> items) {
        final List<ItemStack> itemStacks = new ArrayList<>();
        for (final ConfigurationNode item : items) {
            final ItemStack is = this.getItemStack(item);
            if (is == null) continue;
            itemStacks.add(is);
        }
        return itemStacks;
    }

    /**
     * Gets the lore for the given item.
     *
     * @param item Item to get lore for
     * @return List of strings, never null
     */
    private List<String> getLore(final ConfigurationNode item) {
        final List<String> lore = new ArrayList<>();
        if (item == null) return lore;
        for (final String loreItem : item.getStringList("lore", new ArrayList<String>())) {
            lore.add(RUtils.colorize(loreItem));
        }
        return lore;
    }

    /**
     * Gets the amount of seconds this kit's cooldown is.
     *
     * @return Seconds
     */
    public long getCooldown() {
        return this.cooldown;
    }

    /**
     * Gets the timestamp in milliseconds for when the cooldown for the given player will expire.
     *
     * @param rp Player
     * @return Cooldown expiration timestamp in milliseconds
     */
    public long getCooldownFor(final RPlayer rp) {
        final long lastUsed = this.getLastUsed(rp);
        return (this.getCooldown() * 1000L) + lastUsed;
    }

    /**
     * Gets the description of this kit, suitable for display to players.
     *
     * @return Description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Gets a cloned list of items contained in this kit.
     *
     * @return Cloned list
     */
    public List<ItemStack> getItems() {
        return new ArrayList<>(this.items);
    }

    /**
     * Gets the timestamp in milliseconds that the player last used this kit.
     *
     * @param rp Player
     * @return Timestamp in milliseconds
     */
    public long getLastUsed(final RPlayer rp) {
        return rp.getPlayerConfiguration().getLong("kits." + this.getName() + ".last_used", 0L);
    }

    /**
     * Gets the name of this kit.
     *
     * @return Name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Checks to see if the cooldown time has passed for the player using this kit. If this returns true, the player can
     * use the kit, if not, he can't.
     *
     * @param rp RPlayer using kit
     * @return If the player can use the kit
     */
    public boolean hasCooldownPassedFor(final RPlayer rp) {
        final long lastUsed = this.getLastUsed(rp);
        return !(this.getCooldown() == -1L && lastUsed != 0L) && lastUsed + (this.getCooldown() * 1000L) < System.currentTimeMillis();
    }

    /**
     * Sets the last time that the player used this kit.
     *
     * @param rp       Player using kit
     * @param lastUsed Timestamp in milliseconds
     */
    public void setLastUsed(final RPlayer rp, final long lastUsed) {
        if (this.getCooldown() == 0L) return;
        rp.getPlayerConfiguration().set("kits." + this.getName() + ".last_used", lastUsed);
    }
}
