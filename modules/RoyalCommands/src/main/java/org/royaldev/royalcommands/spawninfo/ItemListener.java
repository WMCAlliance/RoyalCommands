/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.spawninfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.data.block.BlockData;
import org.royaldev.royalcommands.data.block.BlockData.BlockLocation;

public class ItemListener implements Listener {

    private final RoyalCommands plugin;

    public ItemListener(RoyalCommands instance) {
        this.plugin = instance;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        if (!Config.itemSpawnTag) return;
        final Player p = e.getPlayer();
        final Block b = e.getBlock();
        final BlockData bd = new BlockData(this.plugin, new BlockLocation(b.getLocation()));
        final Object o = bd.get("spawninfo");
        if (o == null) return;
        final SpawnInfo si = new SpawnInfo(o.toString());
        if (!si.isSpawned() && !si.hasComponents()) return;
        e.setCancelled(true);
        final List<ItemStack> drops = Collections.list(Collections.enumeration(b.getDrops(e.getPlayer().getInventory().getItemInMainHand())));
        for (int i = 0; i < drops.size(); i++) {
            ItemStack drop = drops.get(i);
            if (drop.getType() != b.getType()) continue;
            drop = RUtils.applySpawnLore(SpawnInfo.SpawnInfoManager.applySpawnInfo(drop, si));
            drops.remove(i);
            drops.add(i, drop);
            break;
        }
        b.setType(Material.AIR);
        if (p.getGameMode() != GameMode.CREATIVE) // don't drop blocks in creative mode, obviously
            for (ItemStack drop : drops) b.getWorld().dropItemNaturally(b.getLocation(), drop);
        bd.remove("spawninfo");
        bd.caughtSave();
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!Config.itemSpawnTag) return;
        final Block b = e.getBlock();
        final SpawnInfo si = SpawnInfo.SpawnInfoManager.getSpawnInfo(e.getItemInHand());
        if (!si.isSpawned() && !si.hasComponents()) return;
        final BlockData bd = new BlockData(this.plugin, new BlockLocation(b.getLocation()));
        bd.set("spawninfo", si.toString());
        bd.caughtSave();
    }

    @EventHandler(ignoreCancelled = true)
    public void onCraft(CraftItemEvent e) {
        if (!Config.itemSpawnTag) return;
        final CraftingInventory ci = e.getInventory();
        final List<ItemStack> spawnedItems = new ArrayList<>();
        for (ItemStack component : ci.getMatrix()) {
            if (component == null) continue;
            final SpawnInfo csi = SpawnInfo.SpawnInfoManager.getSpawnInfo(component);
            if (!csi.isSpawned() && !csi.hasComponents()) continue;
            spawnedItems.add(component);
        }
        if (spawnedItems.isEmpty()) return;
        final SpawnInfo si = SpawnInfo.SpawnInfoManager.getSpawnInfo(Objects.requireNonNull(ci.getResult()));
        si.setHasComponents(true);
        for (ItemStack spawned : spawnedItems) {
            final SpawnInfo ssi = SpawnInfo.SpawnInfoManager.getSpawnInfo(spawned);
            ItemMeta spawnedMeta = spawned.getItemMeta();
            Damageable spawnedDamageable = (Damageable)spawnedMeta;
            si.getComponents().add(String.format("{(%sx%s):%s}%s%s", spawned.getType().name(), spawned.getAmount(), spawnedDamageable.getDamage(), ((ssi.isSpawned()) ? " Spawned by: " + ssi.getSpawner() + "." : ""), ((ssi.hasComponents()) ? " Had spawned components." : "")));
        }
        ci.setResult(SpawnInfo.SpawnInfoManager.applySpawnInfo(ci.getResult(), si));
    }

    @EventHandler(ignoreCancelled = true)
    public void onSmelt(FurnaceSmeltEvent e) {
        if (!Config.itemSpawnTag) return;
        final Block b = e.getBlock();
        if (!(b instanceof Furnace)) return;
        final Furnace f = (Furnace) b;
        final SpawnInfo si = SpawnInfo.SpawnInfoManager.getSpawnInfo(e.getResult());
        if (SpawnInfo.SpawnInfoManager.getSpawnInfo(e.getSource()).isSpawned()) {
            if (!si.hasComponents()) si.setHasComponents(true);
            si.getComponents().add(e.getSource().toString());
        }
        if (SpawnInfo.SpawnInfoManager.getSpawnInfo(f.getInventory().getFuel()).isSpawned()) {
            if (!si.hasComponents()) si.setHasComponents(true);
            si.getComponents().add(f.getInventory().getFuel().toString());
        }
        e.setResult(SpawnInfo.SpawnInfoManager.applySpawnInfo(e.getResult(), si));
    }

}
