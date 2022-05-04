/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.gui.inventory;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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

    private static final String TAG_NAME = "IG:Tag";
    private final Inventory base;
    private final Map<UUID, ClickHandler> clickHandlers = new HashMap<>();
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
     * Tags an ItemStack with the given UUID, which can be used to quickly get the item again.
     *
     * @param is   ItemStack to tag
     * @param uuid UUID to tag the item with
     * @return The tagged ItemStack
     */
    private ItemStack tagItem(final ItemStack is, final UUID uuid) {
        if (is == null || is.getType() == Material.AIR) return is;
        ItemMeta meta = is.getItemMeta();
        if(!meta.hasAttributeModifiers()){
            meta.addAttributeModifier(Attribute.GENERIC_FOLLOW_RANGE, new AttributeModifier(uuid, InventoryGUI.TAG_NAME, 0D, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));
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
        this.addItem(UUID.randomUUID(), clickHandler, x, y, guiItem);
    }

    /**
     * Adds an item to the GUI. The given {@link ClickHandler} will be associated with this item. The item will be
     * tagged with the given UUID.
     *
     * @param uuid         UUID to tag the item with
     * @param clickHandler ClickHandler to use for the item
     * @param x            X-coordinate of the position the item will be added at
     * @param y            Y-coordinate
     * @param guiItem      Item to add
     */
    public void addItem(final UUID uuid, final ClickHandler clickHandler, final int x, final int y, final GUIItem guiItem) {
        final int slot = this.getSlot(x, y);
        if (slot > this.getBase().getSize() - 1) {
            throw new IllegalArgumentException("Location does not exist.");
        }
        final ItemStack is = this.tagItem(guiItem.makeItemStack(), uuid);
        this.getClickHandlers().put(uuid, clickHandler);
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
        final UUID uuid = this.getTag(is);
        return uuid == null ? null : this.getClickHandlers().get(uuid);
    }

    /**
     * Returns the map of UUID tags to ClickHandlers.
     *
     * @return Map
     */
    public Map<UUID, ClickHandler> getClickHandlers() {
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
     * Gets an ItemStack from this GUI by its UUID tag.
     *
     * @param uuid UUID tag of item
     * @return ItemStack or null if no matching UUID
     */
    public ItemStack getItemStack(final UUID uuid) {
        if (uuid == null) return null;
        for (final ItemStack is : this.getBase()) {
            if (!uuid.equals(this.getTag(is))) continue;
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
     * Gets the UUID tag of the given ItemStack.
     *
     * @param is ItemStack to get the tag of
     * @return UUID or null if no tag
     */
    public UUID getTag(final ItemStack is) {
        if (is == null || is.getType() == Material.AIR) return null;
        ItemMeta meta = is.getItemMeta();
        if(meta.hasAttributeModifiers()){
            for (final AttributeModifier a : meta.getAttributeModifiers().values()) {
                if (!a.getName().equals(InventoryGUI.TAG_NAME)) continue;
                return a.getUniqueId();
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
     * Replaces the ItemStack with the given UUID tag with the replacement ItemStack.
     *
     * @param uuid        Tag of ItemStack to replace
     * @param replacement Replacement ItemStack
     */
    public void replaceItemStack(final UUID uuid, final ItemStack replacement) {
        if (uuid == null) throw new IllegalArgumentException("UUID cannot be null");
        for (int i = 0; i < this.getBase().getSize(); i++) {
            final ItemStack is = this.getBase().getItem(i);
            if (!uuid.equals(this.getTag(is))) continue;
            this.getBase().setItem(i, this.tagItem(replacement, uuid));
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
     * Sets the name of the ItemStack tagged with the given UUID.
     *
     * @param uuid UUID of the ItemStack
     * @param name New name
     */
    public void setName(final UUID uuid, final String name) {
        final ItemStack is = this.getItemStack(uuid);
        if (is == null) throw new IllegalArgumentException("No such ItemStack UUID found");
        this.replaceItemStack(uuid, this.setItemMeta(is, name, null));
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
