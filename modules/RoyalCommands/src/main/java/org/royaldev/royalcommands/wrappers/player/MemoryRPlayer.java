/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.wrappers.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.WorldManager;
import org.royaldev.royalcommands.configuration.PlayerConfiguration;
import org.royaldev.royalcommands.configuration.PlayerConfigurationManager;
import org.royaldev.royalcommands.listeners.BackpackListener;
import org.royaldev.royalcommands.rcommands.home.Home;
import org.royaldev.royalcommands.rcommands.nick.Nick;
import org.royaldev.royalcommands.wrappers.teleport.PlayerTeleporter;

/**
 * Wrapper class for {@link org.bukkit.entity.Player}. Stores instances in memory by the player's UUID key.
 */
public class MemoryRPlayer implements RPlayer {

    private static final Map<UUID, RPlayer> players = Collections.synchronizedMap(new HashMap<UUID, RPlayer>());
    private final UUID uuid;
    private final Nick nick;

    private MemoryRPlayer(final OfflinePlayer op) {
        this.uuid = op.getUniqueId();
        this.nick = new Nick(this);
    }

    private MemoryRPlayer(final String name) {
        this.uuid = this.getRoyalCommands().getServer().getOfflinePlayer(name).getUniqueId();
        this.nick = new Nick(this);
    }

    private MemoryRPlayer(final UUID uuid) {
        this.uuid = uuid;
        this.nick = new Nick(this);
    }

    public static RPlayer getRPlayer(final OfflinePlayer op) {
        return MemoryRPlayer.getRPlayer(op.getUniqueId());
    }

    public static RPlayer getRPlayer(final String name) {
        return MemoryRPlayer.getRPlayer(RoyalCommands.getInstance().getServer().getOfflinePlayer(name).getUniqueId());
    }

    public static RPlayer getRPlayer(final UUID uuid) {
        synchronized (MemoryRPlayer.players) {
            if (MemoryRPlayer.players.containsKey(uuid)) return MemoryRPlayer.players.get(uuid);
            final RPlayer rp = new MemoryRPlayer(uuid);
            MemoryRPlayer.players.put(uuid, rp);
            return rp;
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || obj.getClass() != MemoryRPlayer.class) return false;
        final RPlayer other = (RPlayer) obj;
        final UUID otherUUID = other.getUUID();
        return otherUUID != null && otherUUID.equals(this.getUUID());
    }

    /**
     * Gets this player's backpack.
     *
     * @param w World to get backpack in
     * @return Backpack - never null
     */
    @Override
    public Inventory getBackpack(final World w) {
        String worldGroup = WorldManager.il.getWorldGroup(w);
        if (worldGroup == null) worldGroup = "w-" + w.getName();
        int invSize = this.getPlayerConfiguration().getInt("backpack." + worldGroup + ".size", -1);
        if (invSize < 9) invSize = 36;
        if (invSize % 9 != 0) invSize = 36;
        final Inventory i = Bukkit.createInventory(new BackpackListener.BackpackHolder(this.getUUID(), w), invSize, "Backpack");
        if (!this.getPlayerConfiguration().isSet("backpack." + worldGroup + ".item")) return i;
        for (int slot = 0; slot < invSize; slot++) {
            final ItemStack is = this.getPlayerConfiguration().getItemStack("backpack." + worldGroup + ".item." + slot);
            if (is == null) continue;
            i.setItem(slot, is);
        }
        return i;
    }

    /**
     * Gets the maximum amount of homes this player is allowed to have. If there is no limit, -1 will be returned.
     *
     * @return Home limit
     */
    @Override
    public int getHomeLimit() {
        final String name = this.getOfflinePlayer().getName();
        String group;
        if (RoyalCommands.getInstance().vh.usingVault() && this.getPlayer() != null) {
            try {
                group = RoyalCommands.getInstance().vh.getPermission().getPrimaryGroup(this.getPlayer());
            } catch (final Exception e) {
                group = "";
            }
        } else group = "";
        if (group == null) group = "";
        int limit;
        final FileConfiguration c = RoyalCommands.getInstance().getConfig();
        if (c.isSet("homes.limits.players." + name)) limit = c.getInt("homes.limits.players." + name, -1);
        else limit = c.getInt("homes.limits.groups." + group, -1);
        return limit;
    }

    @Override
    public List<String> getHomeNames() {
        final List<String> names = new ArrayList<>();
        for (final Home h : this.getHomes()) {
			if(h == null || h.getName() == null) continue;
            names.add(h.getName());
        }
        return names;
    }

    /**
     * Gets all homes for this player.
     *
     * @return A list
     */
    @Override
    public List<Home> getHomes() {
        final List<Home> homes = new ArrayList<>();
        final PlayerConfiguration pc = this.getPlayerConfiguration();
        if (pc == null || !pc.isSet("home")) return homes;
        for (final String name : pc.getConfigurationSection("home").getKeys(false)) {
            homes.add(Home.fromPConfManager(this.getPlayerConfiguration(), name));
        }
        return homes;
    }

    @Override
    public String getName() {
        return this.getOfflinePlayer().getName();
    }

    @Override
    public Nick getNick() {
        return this.nick;
    }

    @Override
    public OfflinePlayer getOfflinePlayer() {
        final Player online = this.getPlayer();
        return online != null ? online : RoyalCommands.getInstance().getServer().getOfflinePlayer(this.getUUID());
    }

    @Override
    public Player getPlayer() {
        return RoyalCommands.getInstance().getServer().getPlayer(this.getUUID());
    }

    @Override
    public PlayerConfiguration getPlayerConfiguration() {
        return PlayerConfigurationManager.getConfiguration(this.uuid);
    }

    @Override
    public RoyalCommands getRoyalCommands() {
        return RoyalCommands.getInstance();
    }

    @Override
    public PlayerTeleporter getTeleporter() {
        final Player p = this.getPlayer();
        if (p == null) return null;
        return new PlayerTeleporter(p);
    }

    @Override
    public UUID getUUID() {
        return this.uuid;
    }

    @Override
    public boolean isOnline() {
        return this.getPlayer() != null;
    }

    @Override
    public boolean isSameAs(final UUID uuid) {
        return this.getUUID() != null && this.getUUID().equals(uuid);
    }

    @Override
    public boolean isSameAs(final OfflinePlayer op) {
        return this.isSameAs(op.getUniqueId());
    }

    @Override
    public void sendMessage(final String message) {
        final Player p = this.getPlayer();
        if (p == null) return;
        p.sendMessage(message);
    }
}
