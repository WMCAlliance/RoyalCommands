/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands.trade;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.gui.inventory.ClickHandler;
import org.royaldev.royalcommands.gui.inventory.GUIItem;
import org.royaldev.royalcommands.gui.inventory.InventoryGUI;
import org.royaldev.royalcommands.rcommands.trade.clickhandlers.AddPartyCommand;
import org.royaldev.royalcommands.rcommands.trade.clickhandlers.DoNothing;
import org.royaldev.royalcommands.rcommands.trade.clickhandlers.GiveHelpBook;
import org.royaldev.royalcommands.rcommands.trade.clickhandlers.RemindOtherParty;
import org.royaldev.royalcommands.rcommands.trade.clickhandlers.RemoveItem;
import org.royaldev.royalcommands.rcommands.trade.clickhandlers.ToggleTradeAcceptance;
import org.royaldev.royalcommands.rcommands.trade.guiitems.AddCommandItem;
import org.royaldev.royalcommands.rcommands.trade.guiitems.DoNothingItem;
import org.royaldev.royalcommands.rcommands.trade.guiitems.HelpItem;
import org.royaldev.royalcommands.rcommands.trade.guiitems.RemindItem;
import org.royaldev.royalcommands.rcommands.trade.guiitems.ToggleAcceptanceItem;
import org.royaldev.royalcommands.rcommands.trade.tradables.Tradable;
import org.royaldev.royalcommands.rcommands.trade.tradables.TradableCommand;
import org.royaldev.royalcommands.rcommands.trade.tradables.TradableItem;
import org.royaldev.royalcommands.tools.Pair;
import org.royaldev.royalcommands.tools.Vector2D;
import org.royaldev.royalcommands.wrappers.player.MemoryRPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/*
* FIXME:
* Trade accept button sometimes doesn't update until reopen (?)
 */

/**
 * A trade between two {@link Party Parties} involving a mix of items and commands.
 */
public class Trade {

    private static final Map<Pair<UUID, UUID>, Trade> trades = new HashMap<>();
    private final Map<Party, UUID> parties = new HashMap<>();
    private final Map<Party, Boolean> acceptances = new HashMap<>();
    private final InventoryGUI inventoryGUI;
    private final UUID acceptButtonUUID = UUID.randomUUID();

    public Trade(final UUID trader, final UUID tradee) {
        this.parties.put(Party.TRADER, trader);
        this.parties.put(Party.TRADEE, tradee);
        this.acceptances.put(Party.TRADER, false);
        this.acceptances.put(Party.TRADEE, false);
        this.inventoryGUI = this.makeInventoryGUI();
        Trade.trades.put(new Pair<>(trader, tradee), this);
    }

    /**
     * Gets the trade that matches both UUIDs. The order they are provided in does not matter.
     *
     * @param first  First UUID to match
     * @param second Second UUID to match
     * @return A trade or null if none has been initiated
     */
    public static Trade getTradeFor(final UUID first, final UUID second) {
        for (final Map.Entry<Pair<UUID, UUID>, Trade> entry : Trade.trades.entrySet()) {
            final Pair<UUID, UUID> pair = entry.getKey();
            if ((pair.getFirst().equals(first) || pair.getSecond().equals(first)) && (pair.getFirst().equals(second) || pair.getSecond().equals(second))) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Gets all the trades involving the given UUID.
     *
     * @param uuid UUID to search for
     * @return A trade or null if none has been initiated
     */
    public static List<Trade> getTradesFor(final UUID uuid) {
        final List<Trade> trades = new ArrayList<>();
        for (final Map.Entry<Pair<UUID, UUID>, Trade> entry : Trade.trades.entrySet()) {
            final Pair<UUID, UUID> pair = entry.getKey();
            if (pair.getFirst().equals(uuid) || pair.getSecond().equals(uuid)) {
                trades.add(entry.getValue());
            }
        }
        return trades;
    }

    /**
     * Removes this trade from the map of open trades. It should be picked up by garbage collection.
     */
    private void destroy() {
        for (final Map.Entry<Pair<UUID, UUID>, Trade> entry : Trade.trades.entrySet()) {
            if (!entry.getValue().equals(this)) continue;
            Trade.trades.remove(entry.getKey());
            break;
        }
    }

    /**
     * Gets the name of this trade, formulated as "trader name/tradee name"
     *
     * @return Name of trade
     */
    private String getTradeName() {
        final String traderName = MemoryRPlayer.getRPlayer(this.get(Party.TRADER)).getName();
        final String tradeeName = MemoryRPlayer.getRPlayer(this.get(Party.TRADEE)).getName();
        final String tradeName = traderName + "/" + tradeeName;
        return tradeName.length() > 32 ? tradeName.substring(0, 32) : tradeName;
    }

    /**
     * Creates an {@link org.royaldev.royalcommands.gui.inventory.InventoryGUI} for this trade. If this has already been
     * called, it will return null. Otherwise, it will return a new InventoryGUI. To get it after it has already been
     * created, use {@link #getInventoryGUI()}.
     *
     * @return InventoryGUI or null
     */
    private InventoryGUI makeInventoryGUI() {
        if (this.getInventoryGUI() != null) return null;
        final InventoryGUI inventoryGUI = new InventoryGUI(this.getTradeName());
        if (Config.allowCommandsTrade) {
            inventoryGUI.addItem(
                    new AddPartyCommand(this, Party.TRADER),
                    5, 1,
                    new AddCommandItem(this, Party.TRADER));
            inventoryGUI.addItem(
                    new AddPartyCommand(this, Party.TRADEE),
                    5, 2,
                    new AddCommandItem(this, Party.TRADEE));
        } else {
            inventoryGUI.addItem(new DoNothing(), 5, 1, new DoNothingItem());
            inventoryGUI.addItem(new DoNothing(), 5, 2, new DoNothingItem());
        }
        inventoryGUI.addItem(
            this.acceptButtonUUID,
            new ToggleTradeAcceptance(this),
            5, 3,
            new ToggleAcceptanceItem(this)
        );
        inventoryGUI.addItem(
            new RemindOtherParty(this),
            5, 4,
            new RemindItem()
        );
        inventoryGUI.addItem(
            new GiveHelpBook(),
            5, 5,
            new HelpItem()
        );
        return inventoryGUI;
    }

    /**
     * Processes this ItemStack before adding to the inventory. If it is a special, it will be processed into whatever
     * it should be and added to the returned list.
     *
     * @param is ItemStack to process
     * @return List, never null
     */
    private List<Tradable> processSpecialItem(final ItemStack is) {
        final List<Tradable> trades = new ArrayList<>();
        if (this.getInventoryGUI().getClickHandler(is) instanceof RemoveItem) { // TODO: Do this better
            // TODO: Extract logic
            final ItemMeta im = is.getItemMeta();
            for (final String lore : im.getLore()) {
                if (!lore.startsWith(MessageColor.NEUTRAL + "/")) continue;
                trades.add(new TradableCommand(this, lore.substring((MessageColor.NEUTRAL + "/").length())));
            }
            return trades;
        }
        return null;
    }

    /**
     * Sends a message to all parties in this trade, if they are online.
     *
     * @param message Message to send
     */
    private void sendMessageToAll(final String message) {
        final Player trader = this.getPlayer(Party.TRADER);
        final Player tradee = this.getPlayer(Party.TRADEE);
        if (trader != null) trader.sendMessage(message);
        if (tradee != null) tradee.sendMessage(message);
    }

    /**
     * Processes the trade. This will run through the list of tradables provided by {@link #getTradablesFor(Party)}. It
     * will first run through TRADER tradables, then TRADEE tradables. The
     * {@link org.royaldev.royalcommands.rcommands.trade.tradables.Tradable#trade(Party, Party)} method will be called
     * for each one. If all was successful, the trade will be destroyed. Otherwise, a message will be sent and the trade
     * will be left open. The trade inventory will then close.
     *
     * @return If all was successful
     */
    private boolean trade() {
        final List<Tradable> tradeeTradables = this.getTradablesFor(Party.TRADEE); // tradee has offered
        final List<Tradable> traderTradables = this.getTradablesFor(Party.TRADER); // trader has offered
        if (!this.areBothPartiesOnline()) return false;
        boolean allSuccessful = true;
        for (final Tradable trade : tradeeTradables) {
            final boolean success = trade.trade(Party.TRADEE, Party.TRADER);
            if (success) trade.destroy();
            if (allSuccessful) allSuccessful = success;
        }
        for (final Tradable trade : traderTradables) {
            final boolean success = trade.trade(Party.TRADER, Party.TRADEE);
            if (success) trade.destroy();
            if (allSuccessful) allSuccessful = success;
        }
        if (allSuccessful) {
            this.destroy();
        } else {
            this.sendMessageToAll(MessageColor.NEGATIVE + "The trade did not successfully complete. Please check the trade to see any remaining items.");
            this.setAcceptance(Party.TRADER, false);
            this.setAcceptance(Party.TRADEE, false);
        }
        Party.TRADER.closeTrade(this);
        Party.TRADEE.closeTrade(this);
        return allSuccessful;
    }

    /**
     * Adds an item to the InventoryGUI for this trade.
     *
     * @param clickHandler ClickHandler of the item
     * @param party        Side of the inventory to add the item to
     * @param guiItem      Item to add
     */
    public void addItem(final ClickHandler clickHandler, final Party party, final GUIItem guiItem) {
        final int freeSlot = party.getNextFreeSlot(this.getInventoryGUI().getBase());
        if (freeSlot == -1) return; // TODO: Throw?
        final Vector2D xy = this.getInventoryGUI().getXYFromSlot(freeSlot);
        this.getInventoryGUI().addItem(clickHandler, xy.getX(), xy.getY(), guiItem);
    }

    /**
     * Returns if both parties are currently online.
     *
     * @return true if online, false if otherwise
     */
    public boolean areBothPartiesOnline() {
        return this.getPlayer(Party.TRADEE) != null && this.getPlayer(Party.TRADER) != null;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || !(obj instanceof Trade)) return false;
        final Trade t = (Trade) obj;
        return this.getInventoryGUI().equals(t.getInventoryGUI()) && this.parties.equals(t.parties);
    }

    /**
     * Gets the UUID for the given party.
     *
     * @param party Party to get UUID of
     * @return UUID or null
     */
    public UUID get(final Party party) {
        return this.parties.get(party);
    }

    /**
     * Gets the Party of the given UUID.
     *
     * @param uuid UUID to get Party of.
     * @return Party or null
     */
    public Party get(final UUID uuid) {
        for (final Map.Entry<Party, UUID> entry : this.parties.entrySet()) {
            if (!entry.getValue().equals(uuid)) continue;
            return entry.getKey();
        }
        return null;
    }

    /**
     * Gets the InventoryGUI for this trade. This may be null if {@link #makeInventoryGUI()} was never called.
     *
     * @return InventoryGUI or null
     */
    public InventoryGUI getInventoryGUI() {
        return this.inventoryGUI;
    }

    /**
     * Gets the name of the Player that corresponds to the given Party. This is a convenience method.
     *
     * @param party Party to get name of
     * @return Name
     */
    public String getName(final Party party) {
        return MemoryRPlayer.getRPlayer(this.get(party)).getName();
    }

    /**
     * Gets the Player that corresponds to the given Party. This is a convenience method. This may return null if the
     * Player is not online.
     *
     * @param party Party to get Player of
     * @return Player or null
     */
    public Player getPlayer(final Party party) {
        return MemoryRPlayer.getRPlayer(this.get(party)).getPlayer();
    }

    /**
     * Gets all Tradables on the given Party's side of the trade.
     *
     * @param party Party to get Tradables for
     * @return List of Tradables, never null
     */
    public List<Tradable> getTradablesFor(final Party party) {
        final List<Tradable> items = new ArrayList<>();
        final Inventory base = this.getInventoryGUI().getBase();
        for (int i = 0; i < base.getSize(); i++) {
            if (!party.canAccessSlot(i)) continue;
            final ItemStack item = base.getItem(i);
            if (item == null || item.getType() == Material.AIR) continue;
            final List<Tradable> special = this.processSpecialItem(item);
            if (special == null) items.add(new TradableItem(this, item));
            else items.addAll(special);
        }
        return items;
    }

    /**
     * Gets the lore for the trade status item for the given party.
     *
     * @param party Party to get lore for
     * @return String
     */
    public String getTradeStatusLore(final Party party) {
        final boolean accepted = this.hasAccepted(party);
        final StringBuilder sb = new StringBuilder().append(MessageColor.NEUTRAL).append(this.getName(party)).append(": ");
        sb.append(accepted ? MessageColor.POSITIVE : MessageColor.NEGATIVE).append(accepted ? "Accepted" : "Declined");
        return sb.toString();
    }

    /**
     * Checks to see if the given party has accepted the trade.
     *
     * @param party Party to check
     * @return true if the trade has been accepted, false if otherwise
     */
    public boolean hasAccepted(final Party party) {
        return this.acceptances.get(party);
    }

    /**
     * Checks to see if both parties have accepted the trade.
     *
     * @return true if both have accepted, false if not
     */
    public boolean haveBothAccepted() {
        return this.hasAccepted(Party.TRADER) && this.hasAccepted(Party.TRADEE);
    }

    /**
     * Sets the acceptance of the trade for the given party.
     *
     * @param party      Party to set acceptance of
     * @param acceptance Acceptance to set
     * @return acceptance
     */
    public boolean setAcceptance(final Party party, final boolean acceptance) {
        this.acceptances.put(party, acceptance);
        this.updateAcceptButton();
        if (this.haveBothAccepted()) this.trade();
        return acceptance;
    }

    /**
     * Shows the InventoryGUI for this trade to the given UUID.
     *
     * @param uuid UUID to show to
     */
    public void showInventoryGUI(final UUID uuid) {
        final Party party = this.get(uuid);
        if (party == null) throw new IllegalArgumentException("No such UUID");
        final Player p = MemoryRPlayer.getRPlayer(uuid).getPlayer();
        if (p == null) return;
        p.openInventory(this.getInventoryGUI().getBase());
    }

    /**
     * Toggles the acceptance for the given party.
     *
     * @param party Party to toggle acceptance for
     * @return New acceptance status
     */
    public boolean toggleAcceptance(final Party party) {
        return this.setAcceptance(party, !this.acceptances.get(party));
    }

    /**
     * Toggles acceptance for the given UUID.
     *
     * @param uuid UUID to toggle acceptance for
     * @return New acceptance status
     */
    public boolean toggleAcceptance(final UUID uuid) {
        final Party party = this.get(uuid);
        if (party == null) throw new IllegalArgumentException("No such UUID");
        return this.toggleAcceptance(party);
    }

    /**
     * Updates the accept trade button with new lore to reflect the acceptance status of both parties.
     */
    public void updateAcceptButton() {
        final ItemStack updateButton = this.getInventoryGUI().getItemStack(this.acceptButtonUUID);
        final List<String> lore = new ToggleAcceptanceItem(this).getLore();
        this.getInventoryGUI().updateItemStack(this.getInventoryGUI().setItemMeta(
            updateButton,
            null,
            lore.toArray(new String[lore.size()])
        ));
    }
}
