/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.listeners;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.rcommands.CmdMonitor;

public class MonitorListener implements Listener {

    public static final List<String> openInvs = new ArrayList<>();
    private final RoyalCommands plugin;

    public MonitorListener(RoyalCommands instance) {
        this.plugin = instance;
    }

    private Player getMP(Player p) {
        final String name = CmdMonitor.monitors.get(p.getName());
        if (name == null) return null;
        return this.plugin.getServer().getPlayer(name);
    }

    private Player getVP(Player p) {
        final String name = CmdMonitor.viewees.get(p.getName());
        if (name == null) return null;
        return this.plugin.getServer().getPlayer(name);
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent e) {
        if (!CmdMonitor.monitors.containsKey(e.getPlayer().getName())) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void blockPlace(BlockPlaceEvent e) {
        if (!CmdMonitor.monitors.containsKey(e.getPlayer().getName())) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void interact(PlayerInteractEvent e) {
        if (!CmdMonitor.monitors.containsKey(e.getPlayer().getName())) return;
        if (e.getClickedBlock() == null) return; // Fixed NPE below?
        if (e.getClickedBlock().getState() instanceof Chest) {
            final Chest c = (Chest) e.getClickedBlock().getState();
            final Inventory i = this.plugin.getServer().createInventory(c.getInventory().getHolder(), c.getInventory().getSize());
            i.setContents(c.getInventory().getContents());
            e.getPlayer().openInventory(i);
            e.getPlayer().sendMessage(MessageColor.POSITIVE + "Opened chest in read-only mode; you can't make changes.");
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void joinViewee(PlayerJoinEvent e) {
        if (!CmdMonitor.viewees.containsKey(e.getPlayer().getName())) return;
        final Player t = this.getVP(e.getPlayer());
        if (t == null) return;
        t.hidePlayer(e.getPlayer());
    }

    @EventHandler
    public void onBlockViewee(BlockPlaceEvent e) {
        if (!CmdMonitor.viewees.containsKey(e.getPlayer().getName())) return;
        final Player t = this.getVP(e.getPlayer());
        if (t == null) return;
        t.getInventory().setContents(e.getPlayer().getInventory().getContents());
    }

    @EventHandler
    public void onChangeHold(PlayerItemHeldEvent e) {
        if (!CmdMonitor.viewees.containsKey(e.getPlayer().getName())) return;
        final Player t = this.getVP(e.getPlayer());
        if (t == null) return;
        t.getInventory().setContents(e.getPlayer().getInventory().getContents());
        t.getInventory().setHeldItemSlot(e.getNewSlot());
    }

    @EventHandler
    public void onDamageViewee(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        final Player p = (Player) e.getEntity();
        if (!CmdMonitor.viewees.containsKey(p.getName())) return;
        final Player t = this.getVP(p);
        if (t == null) return;
        if (p.getHealth() < 1) return;
        t.setHealth(p.getHealth());
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (!CmdMonitor.monitors.containsKey(e.getPlayer().getName())) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onFoodViewee(FoodLevelChangeEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        final Player p = (Player) e.getEntity();
        if (!CmdMonitor.viewees.containsKey(p.getName())) return;
        final Player t = this.getVP(p);
        if (t == null) return;
        if (p.getFoodLevel() < 1) return;
        t.setFoodLevel(p.getFoodLevel());
        t.setSaturation(p.getSaturation());
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        if (!MonitorListener.openInvs.contains(e.getWhoClicked().getName())) return;
        e.setCancelled(true);
    }

    /*@EventHandler
    public void onInvCloseMonitor(InventoryCloseEvent e) {
        if (!MonitorListener.openInvs.contains(e.getPlayer().getName())) return;
        e.getPlayer().openInventory(e.getInventory());
    }*/
    // Seems a bit harsh ^

    @EventHandler
    public void onInvCloseViewee(InventoryCloseEvent e) {
        if (!CmdMonitor.viewees.containsKey(e.getPlayer().getName())) return;
        if (!(e.getPlayer() instanceof Player)) return;
        final Player t = this.getVP((Player) e.getPlayer());
        if (t == null) return;
        t.closeInventory();
        MonitorListener.openInvs.remove(t.getName());
    }

    @EventHandler
    public void onInvOpen(InventoryOpenEvent e) {
        if (!(e.getPlayer() instanceof Player)) return;
        final Player p = (Player) e.getPlayer();
        if (!CmdMonitor.viewees.containsKey(p.getName())) return;
        final Player t = this.getVP(p);
        if (t == null) return;
        final Inventory i = e.getInventory();
        final Block b = RUtils.getTarget(p);
        switch (i.getType()) {
            case WORKBENCH:
                if (!b.getType().equals(Material.CRAFTING_TABLE)) return;
                t.openWorkbench(b.getLocation(), false);
                break;
            case ENCHANTING:
                if (!b.getType().equals(Material.ENCHANTING_TABLE)) return;
                t.openEnchanting(b.getLocation(), false);
                break;
            case BREWING:
            case CRAFTING:
            case DISPENSER:
                return;
        }
        t.openInventory(e.getInventory());
        MonitorListener.openInvs.add(t.getName());
    }

    @EventHandler
    public void onItemDropViewee(PlayerDropItemEvent e) {
        if (!CmdMonitor.viewees.containsKey(e.getPlayer().getName())) return;
        final Player t = this.getVP(e.getPlayer());
        if (t == null) return;
        t.getInventory().setContents(e.getPlayer().getInventory().getContents());
    }

    @EventHandler
    public void onItemPickupViewee(PlayerPickupItemEvent e) {
        if (!CmdMonitor.viewees.containsKey(e.getPlayer().getName())) return;
        final Player t = this.getVP(e.getPlayer());
        if (t == null) return;
        t.getInventory().setContents(e.getPlayer().getInventory().getContents());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!CmdMonitor.monitors.containsValue(e.getPlayer().getName())) return;
        final Player p = this.getVP(e.getPlayer());
        if (p == null) return;
        RUtils.silentTeleport(p, e.getPlayer());
        if (e.getPlayer().canSee(p)) e.getPlayer().hidePlayer(p);
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent e) {
        if (!CmdMonitor.monitors.containsKey(e.getPlayer().getName())) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (CmdMonitor.monitors.containsKey(e.getPlayer().getName())) return;
        for (final String pn : CmdMonitor.monitors.keySet()) {
            final Player p = this.plugin.getServer().getPlayer(pn);
            if (p == null) continue;
            e.getPlayer().hidePlayer(p);
        }
    }

    @EventHandler
    public void onRegainViewee(EntityRegainHealthEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        final Player p = (Player) e.getEntity();
        if (!CmdMonitor.viewees.containsKey(p.getName())) return;
        final Player t = this.getVP(p);
        if (t == null) return;
        if (p.getHealth() < 1) return;
        if (p.getHealth() < t.getHealth()) return;
        t.setHealth(p.getHealth());
    }

    @EventHandler
    public void onTele(PlayerTeleportEvent e) {
        if (!CmdMonitor.viewees.containsKey(e.getPlayer().getName())) return;
        final Player p = this.getVP(e.getPlayer());
        if (p == null) return;
        RUtils.silentTeleport(p, e.getPlayer());
        e.getPlayer().hidePlayer(p);
    }

}
