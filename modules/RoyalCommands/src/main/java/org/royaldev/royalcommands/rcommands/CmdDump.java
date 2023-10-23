/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import java.util.HashMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;

@ReflectCommand
public class CmdDump extends TabCommand {

    public CmdDump(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{});
    }

    public void dumpItems(Chest c, PlayerInventory i) {
        Inventory ci = c.getInventory();
        ItemStack[] pc = i.getContents();
        for (ItemStack aPc : pc) {
            if (aPc == null) continue;
            HashMap<Integer, ItemStack> left = ci.addItem(aPc.clone());
            i.removeItem(aPc.clone());
            if (left.isEmpty()) continue;
            for (ItemStack item : left.values()) {
                if (item == null) continue;
                i.addItem(item);
            }
        }
		// as i.getContents() returns armor and off-hand but i.removeItem() uses i.getStorageContents() which does not contain armor or off-hand, remove them separately
		ItemStack[] armor = new ItemStack[]{new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR)};
		i.setArmorContents(armor);
		i.setItemInOffHand(new ItemStack(Material.AIR));
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (args.length > 0 && args[0].equals("?")) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        if (!(cs instanceof Player)) {
            cs.sendMessage(MessageColor.NEGATIVE + "This command is only available to players!");
            return true;
        }
        Player p = (Player) cs;
        Block bl = RUtils.getTarget(p);
        if (!Config.dumpCreateChest) {
            if (!(bl.getState() instanceof Chest)) {
                cs.sendMessage(MessageColor.NEGATIVE + "That's not a chest!");
                return true;
            }
            Chest c = (Chest) bl.getState();
            if (!this.plugin.canAccessChest(p, bl)) {
                cs.sendMessage(MessageColor.NEGATIVE + "You cannot access that chest!");
                return true;
            }
            dumpItems(c, p.getInventory());
            cs.sendMessage(MessageColor.POSITIVE + "Items stored.");
            return true;
        }
        Location l = bl.getLocation();
        l.setY(l.getY() + 1);
        Block b = l.getBlock();
        if (b.getType() != Material.AIR) {
            cs.sendMessage(MessageColor.NEGATIVE + "Please make sure the block above is air.");
            return true;
        }
        if (!this.plugin.canBuild(p, b)) {
            cs.sendMessage(MessageColor.NEGATIVE + "You're not allowed to create blocks there!");
            return true;
        }
        if (Config.dumpUseInv) {
            if (!p.getInventory().contains(Material.CHEST)) {
                cs.sendMessage(MessageColor.NEGATIVE + "You have no chests to use!");
                return true;
            }
            int chestAt = -1;
            for (int i = 0; i < p.getInventory().getContents().length; i++) {
                ItemStack is = p.getInventory().getContents()[i];
                if (is == null) continue;
                if (is.getType() == Material.CHEST) {
                    chestAt = i;
                    break;
                }
            }
            if (chestAt == -1) {
                cs.sendMessage(MessageColor.NEGATIVE + "You have no chests to use!");
            }
            ItemStack chests = p.getInventory().getContents()[chestAt];
            chests.setAmount(chests.getAmount() - 1);
            p.getInventory().setItem(chestAt, chests);
        }
        b.setType(Material.CHEST);
        Chest c = (Chest) b.getState();
        dumpItems(c, p.getInventory());
        cs.sendMessage(MessageColor.POSITIVE + "Items stored.");
        return true;
    }
}
