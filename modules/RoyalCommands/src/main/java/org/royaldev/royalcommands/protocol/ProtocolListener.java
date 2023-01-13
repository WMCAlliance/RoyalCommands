/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.protocol;

import org.royaldev.royalcommands.attribute.NbtFactory;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.inventory.ItemStack;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.protocol.packets.WrapperPlayServerSetSlot;
import org.royaldev.royalcommands.protocol.packets.WrapperPlayServerWindowItems;
import org.royaldev.royalcommands.spawninfo.SpawnInfo;

public class ProtocolListener {

    protected static final String NBT_INFO_KEY = "rcmds-spawninfo";
    private final RoyalCommands plugin;
    //final SpawnRenameProcessor srp;
    private final ProtocolManager pm = ProtocolLibrary.getProtocolManager();

    public ProtocolListener(RoyalCommands instance) {
        this.plugin = instance;
		//this.srp = new SpawnRenameProcessor(plugin);
    }

    public void createSetSlotListener() {
        this.pm.addPacketListener(new PacketAdapter(PacketAdapter.params(this.plugin, Server.SET_SLOT)) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (!Config.useProtocolLib) return;
                final WrapperPlayServerSetSlot p = new WrapperPlayServerSetSlot(event.getPacket());
                ItemStack is = p.getSlotData();
                if (is == null) return;
                final SpawnInfo si = SpawnInfo.SpawnInfoManager.getSpawnInfo(is);
                if (!si.isSpawned() && !si.hasComponents()) return;
                is = SpawnInfo.SpawnInfoManager.removeSpawnInfo(is);
                final NbtFactory.NbtCompound nbtc = NbtFactory.fromItemTag(is);
                nbtc.put(NBT_INFO_KEY, si.toString());
                NbtFactory.setItemTag(is, nbtc);
                p.setSlotData(is);
            }
        });
    }

    public void createWindowItemsListener() {
        this.pm.addPacketListener(new PacketAdapter(PacketAdapter.params(this.plugin, Server.WINDOW_ITEMS)) {
            @Override
            public void onPacketSending(PacketEvent event) {
				/* TODO This makes items disappear when switching worlds (invisble, not deleted) */
                if (!Config.useProtocolLib) return;
                final WrapperPlayServerWindowItems p = new WrapperPlayServerWindowItems(event.getPacket());
                final List<ItemStack> newItems = new ArrayList<>();
                for (ItemStack is : p.getSlotData()) {
                    if (is == null) { // SpawnInfoManager can't take null ItemStacks
                        newItems.add(null);
                        continue;
                    }
                    final SpawnInfo si = SpawnInfo.SpawnInfoManager.getSpawnInfo(is);
                    if (!si.isSpawned() && !si.hasComponents()) continue;
                    is = SpawnInfo.SpawnInfoManager.removeSpawnInfo(is);
                    final NbtFactory.NbtCompound nbtc = NbtFactory.fromItemTag(is);
                    nbtc.put(NBT_INFO_KEY, si.toString());
                    NbtFactory.setItemTag(is, nbtc);
                    newItems.add(is);
                }
                p.setSlotData(newItems);
            }
        });
    }

    public void initialize() {
        this.createSetSlotListener();
        this.createWindowItemsListener();
    }

    public void uninitialize() {
        this.pm.removePacketListeners(this.plugin);
    }
}
