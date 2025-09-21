/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.gui.inventory;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.tools.Vector2D;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * An inventory with items that can be clicked on to provide custom actions, forming a primitive type of GUI.
 */
public class InventoryGUI {

    private static final String TAG_NAME = "ig-tag";
    private final Inventory base;
    private final Map<NamespacedKey, ClickHandler> clickHandlers = new HashMap<>();
    private final UUID identifier = UUID.randomUUID();

    /**
     * Creates an InventoryGUI with the given name and 45 slots.
     *
     * @param name Name
     */
    public InventoryGUI(final String name) {
        this(name, 45);
    }

    /**
     * Creates an InventoryGUI with the given name and given amount of slots, which must be a multiple of nine.
     *
     * @param name Name
     * @param size Slots as a multiple of nine
     */
    public InventoryGUI(final String name, final int size) {
        this.base = RoyalCommands.getInstance().getServer().createInventory(new GUIHolder(this), size, name);
    }

    /**
     * Creates an InventoryGUI with the given base inventory. This will be the inventory that is used in underlying
     * methods to change items in.
     *
     * @param base Inventory to use as a base
     */
    public InventoryGUI(final Inventory base) {
        this.base = base;
    }

    /**
     * Tags an ItemStack with the given Key, which can be used to quickly get the item again.
     *
     * @param is   ItemStack to tag
     * @param key Key to tag the item with
     * @return The tagged ItemStack
     */
    private ItemStack tagItem(final ItemStack is, final NamespacedKey key) {
        if (is == null || RUtils.isBlockAir(is.getType())) return is;
        ItemMeta meta = is.getItemMeta();
        if(!meta.hasAttributeModifiers()){
            meta.addAttributeModifier(Attribute.FOLLOW_RANGE, new AttributeModifier(key, 0D, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND.getGroup()));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            is.setItemMeta(meta);
        }return is;

    }

    /**
     * Adds an item to the GUI. The given {@link ClickHandler} will be associated with this item. The item will be
     * tagged with a random UUID.
     *
     * @param clickHandler ClickHandler to use for the item
     * @param x            X-coordinate of the position the item will be added at
     * @param y            Y-coordinate
     * @param guiItem      Item to add
     */
    public void addItem(final ClickHandler clickHandler, final int x, final int y, final GUIItem guiItem) {
        this.addItem(new NamespacedKey(InventoryGUI.TAG_NAME, String.valueOf(UUID.randomUUID())), clickHandler, x, y, guiItem);
    }

    /**
     * Adds an item to the GUI. The given {@link ClickHandler} will be associated with this item. The item will be
     * tagged with the given Key.
     *
     * @param key         Key to tag the item with
     * @param clickHandler ClickHandler to use for the item
     * @param x            X-coordinate of the position the item will be added at
     * @param y            Y-coordinate
     * @param guiItem      Item to add
     */
    public void addItem(final NamespacedKey key, final ClickHandler clickHandler, final int x, final int y, final GUIItem guiItem) {
        final int slot = this.getSlot(x, y);
        if (slot > this.getBase().getSize() - 1) {
            throw new IllegalArgumentException("Location does not exist.");
        }
        final ItemStack is = this.tagItem(guiItem.makeItemStack(), key);
        this.getClickHandlers().put(key, clickHandler);
        this.getBase().setItem(slot, is);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || !(obj instanceof InventoryGUI)) return false;
        final InventoryGUI other = (InventoryGUI) obj;
        return other.getIdentifier().equals(this.getIdentifier());
    }

    /**
     * Gets the base inventory being used for this GUI.
     *
     * @return Inventory
     */
    public Inventory getBase() {
        return this.base;
    }

    /**
     * Gets the ClickHandler of the given ItemStack.
     *
     * @param is ItemStack to get ClickHandler of
     * @return ClickHandler or null if none is associated
     */
    public ClickHandler getClickHandler(final ItemStack is) {
        final NamespacedKey key = this.getTag(is);
        return key == null ? null : this.getClickHandlers().get(key);
    }

    /**
     * Returns the map of Key tags to ClickHandlers.
     *
     * @return Map
     */
    public Map<NamespacedKey, ClickHandler> getClickHandlers() {
        return this.clickHandlers;
    }

    /**
     * Gets the UUID identifier of this InventoryGUI.
     *
     * @return UUID
     */
    public UUID getIdentifier() {
        return this.identifier;
    }

    /**
     * Gets an ItemStack from this GUI by its Key tag.
     *
     * @param key Key tag of item
     * @return ItemStack or null if no matching Key
     */
    public ItemStack getItemStack(final NamespacedKey key) {
        if (key == null) return null;
        for (final ItemStack is : this.getBase()) {
            if (!key.equals(this.getTag(is))) continue;
            return is;
        }
        return null;
    }

    /**
     * Gets the inventory slot represented by the given X- and Y-coordinates.
     *
     * @param x X-coordinate
     * @param y Y-coordinate
     * @return slot
     */
    public int getSlot(final int x, final int y) {
        return ((y - 1) * 9) + (x - 1);
    }

    /**
     * Gets the Key tag of the given ItemStack.
     *
     * @param is ItemStack to get the tag of
     * @return Key or null if no tag
     */
    public NamespacedKey getTag(final ItemStack is) {
        if (is == null || RUtils.isBlockAir(is.getType())) return null;
        ItemMeta meta = is.getItemMeta();
        if(meta.hasAttributeModifiers()){
            for (final AttributeModifier a : meta.getAttributeModifiers().values()) {
                if (!a.getKey().getNamespace().equals(InventoryGUI.TAG_NAME)) continue;
                return a.getKey();
            }
        }return null;
    }

    /**
     * Gets the X- and Y-coordinates from a slot.
     *
     * @param slot slot
     * @return Vector2D with the coordinates
     */
    public Vector2D getXYFromSlot(final int slot) {
        final int xraw = slot % 9;
        final int yraw = (slot - xraw) / 9;
        return new Vector2D(xraw + 1, yraw + 1);
    }

    /**
     * Replaces the original ItemStack with the replacement.
     *
     * @param original    Original
     * @param replacement Replacement
     */
    public void replaceItemStack(final ItemStack original, final ItemStack replacement) {
        this.replaceItemStack(this.getTag(original), replacement);
    }

    /**
     * Replaces the ItemStack with the given Key tag with the replacement ItemStack.
     *
     * @param key        Tag of ItemStack to replace
     * @param replacement Replacement ItemStack
     */
    public void replaceItemStack(final NamespacedKey key, final ItemStack replacement) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        for (int i = 0; i < this.getBase().getSize(); i++) {
            final ItemStack is = this.getBase().getItem(i);
            if (!key.equals(this.getTag(is))) continue;
            this.getBase().setItem(i, this.tagItem(replacement, key));
            break;
        }
    }

    /**
     * Sets the ItemMeta of the given ItemStack.
     *
     * @param is   ItemStack
     * @param name New name or null to not change
     * @param lore New lore or null to not change
     * @return ItemStack with new meta
     */
    public ItemStack setItemMeta(final ItemStack is, final String name, final String... lore) {
        final ItemMeta im = is.getItemMeta();
        if (name != null) im.setDisplayName(name);
        if (lore != null) im.setLore(Arrays.asList(lore));
        is.setItemMeta(im);
        return is;
    }

    /**
     * Sets the name of the ItemStack tagged with the given Key.
     *
     * @param key Key of the ItemStack
     * @param name New name
     */
    public void setName(final NamespacedKey key, final String name) {
        final ItemStack is = this.getItemStack(key);
        if (is == null) throw new IllegalArgumentException("No such ItemStack Key found");
        this.replaceItemStack(key, this.setItemMeta(is, name));
    }

    /**
     * Sets the name of the given ItemStack.
     *
     * @param is   ItemStack
     * @param name New name
     */
    public void setName(final ItemStack is, final String name) {
        this.setName(this.getTag(is), name);
    }

    /**
     * Replaces the given ItemStack with itself. This is often used when modifying an ItemStack that was obtained
     * through other methods. Calling this method will update the stack in the inventory, showing all changes.
     *
     * @param updated Updated ItemStack
     */
    public void updateItemStack(final ItemStack updated) {
        this.replaceItemStack(this.getTag(updated), updated);
    }
}
