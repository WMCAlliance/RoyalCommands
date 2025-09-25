package org.royaldev.royalcommands.protocol;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.spawninfo.SpawnInfo;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTString;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;

import io.github.retrooper.packetevents.util.SpigotConversionUtil;

public class ProtocolListener implements PacketListener {

    protected static final String NBT_INFO_KEY = "rcmds-spawninfo";
    private final RoyalCommands plugin;

    public ProtocolListener(RoyalCommands instance) {
        this.plugin = instance;
    }

    private ItemStack checkAndRemoveSpawnInfo(ItemStack is) {
        if (is == null)
            return null;

        org.bukkit.inventory.ItemStack bis = SpigotConversionUtil.toBukkitItemStack(is);
        final SpawnInfo si = SpawnInfo.SpawnInfoManager.getSpawnInfo(bis);
        if (!si.isSpawned() && !si.hasComponents()) return is;
        is = SpigotConversionUtil.fromBukkitItemStack(SpawnInfo.SpawnInfoManager.removeSpawnInfo(bis));
        NBTCompound nbtc = is.getOrCreateTag();
        nbtc.setTag(NBT_INFO_KEY, new NBTString(si.toString()));
        is.setNBT(nbtc);
        return is;
    }

    @Override
    public void onPacketSend(PacketSendEvent e) {
        if (!Config.itemSpawnTag || !Config.usePacketevents)
            return;

        PacketTypeCommon t = e.getPacketType();
        Player p = e.getPlayer();
        if (p == null)
            return;

        if (t == PacketType.Play.Server.SET_SLOT) {
            WrapperPlayServerSetSlot slot = new WrapperPlayServerSetSlot(e);
            ItemStack is = slot.getItem();
            ItemStack newIs = checkAndRemoveSpawnInfo(is);
            if (newIs == null)
                return;
            slot.setItem(is);

        }
        if (t == PacketType.Play.Server.WINDOW_ITEMS) {
            WrapperPlayServerWindowItems items = new WrapperPlayServerWindowItems(e);
            final List<ItemStack> newItems = new ArrayList<>();
            for (ItemStack is : items.getItems()) {
                ItemStack newIs = checkAndRemoveSpawnInfo(is);
                if (is == null || newIs == null) {
                    newItems.add(null);
                    continue;
                }
                newItems.add(newIs);
            }
            items.setItems(newItems);
        }
    }

    public void initialize() {
        PacketEvents.getAPI().getEventManager().registerListener(this, PacketListenerPriority.NORMAL);
        PacketEvents.getAPI().init();
    }

    public void uninitialize() {
        PacketEvents.getAPI().getEventManager().unregisterAllListeners();
        PacketEvents.getAPI().terminate();
    }
}
