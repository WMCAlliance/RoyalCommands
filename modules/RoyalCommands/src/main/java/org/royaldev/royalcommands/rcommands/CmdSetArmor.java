/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.royaldev.royalcommands.AuthorizationHandler.PermType;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;

@ReflectCommand
public class CmdSetArmor extends TabCommand {

    public CmdSetArmor(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.LIST.getShort(), CompletionType.ONLINE_PLAYER.getShort()});
    }

    @Override
    protected List<String> customList(final CommandSender cs, final Command cmd, final String label, final String[] args, final String arg) {
        return new ArrayList<>(Arrays.asList("diamond", "gold", "iron", "leather", "chain", "none"));
    }
	
    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (args.length < 1) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        if (!(cs instanceof Player)) {
            cs.sendMessage(MessageColor.NEGATIVE + "This command is only available to players!");
            return true;
        }
        final Player p;
        if (args.length > 1) {
            if (!this.ah.isAuthorized(cs, cmd, PermType.OTHERS)) {
                RUtils.dispNoPerms(cs);
                return true;
            }
            p = this.plugin.getServer().getPlayer(args[1]);
            if (p == null || this.plugin.isVanished(p, cs)) {
                cs.sendMessage(MessageColor.NEGATIVE + "That player does not exist!");
                return true;
            }
            if (this.ah.isAuthorized(p, cmd, PermType.EXEMPT)) {
                cs.sendMessage(MessageColor.NEGATIVE + "You can't modify that player's armor!");
                return true;
            }
        } else p = (Player) cs;
        String set = args[0];
        ItemStack[] diamond = new ItemStack[]{new ItemStack(Material.DIAMOND_BOOTS), new ItemStack(Material.DIAMOND_LEGGINGS), new ItemStack(Material.DIAMOND_CHESTPLATE), new ItemStack(Material.DIAMOND_HELMET)};
        ItemStack[] gold = new ItemStack[]{new ItemStack(Material.GOLDEN_BOOTS), new ItemStack(Material.GOLDEN_LEGGINGS), new ItemStack(Material.GOLDEN_CHESTPLATE), new ItemStack(Material.GOLDEN_HELMET)};
        ItemStack[] iron = new ItemStack[]{new ItemStack(Material.IRON_BOOTS), new ItemStack(Material.IRON_LEGGINGS), new ItemStack(Material.IRON_CHESTPLATE), new ItemStack(Material.IRON_HELMET)};
        ItemStack[] leather = new ItemStack[]{new ItemStack(Material.LEATHER_BOOTS), new ItemStack(Material.LEATHER_LEGGINGS), new ItemStack(Material.LEATHER_CHESTPLATE), new ItemStack(Material.LEATHER_HELMET)};
        ItemStack[] chain = new ItemStack[]{new ItemStack(Material.CHAINMAIL_BOOTS), new ItemStack(Material.CHAINMAIL_LEGGINGS), new ItemStack(Material.CHAINMAIL_CHESTPLATE), new ItemStack(Material.CHAINMAIL_HELMET)};
        ItemStack[] none = new ItemStack[]{new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR)};
        if (set.equalsIgnoreCase("diamond")) {
            if (!this.ah.isAuthorized(cs, "rcmds.setarmor.diamond")) {
                cs.sendMessage(MessageColor.NEGATIVE + "You don't have permission for that type of material!");
                return true;
            } else {
                p.getInventory().setArmorContents(diamond);
                cs.sendMessage(MessageColor.POSITIVE + "Armor was set to " + set + ".");
                return true;
            }
        } else if (set.equalsIgnoreCase("gold")) {
            if (!this.ah.isAuthorized(cs, "rcmds.setarmor.gold")) {
                cs.sendMessage(MessageColor.NEGATIVE + "You don't have permission for that type of material!");
                return true;
            } else {
                p.getInventory().setArmorContents(gold);
                cs.sendMessage(MessageColor.POSITIVE + "Armor was set to " + set + ".");
                return true;
            }
        } else if (set.equalsIgnoreCase("iron")) {
            if (!this.ah.isAuthorized(cs, "rcmds.setarmor.iron")) {
                cs.sendMessage(MessageColor.NEGATIVE + "You don't have permission for that type of material!");
                return true;
            } else {
                p.getInventory().setArmorContents(iron);
                cs.sendMessage(MessageColor.POSITIVE + "Armor was set to " + set + ".");
                return true;
            }
        } else if (set.equalsIgnoreCase("leather")) {
            if (!this.ah.isAuthorized(cs, "rcmds.setarmor.leather")) {
                cs.sendMessage(MessageColor.NEGATIVE + "You don't have permission for that type of material!");
                return true;
            } else {
                p.getInventory().setArmorContents(leather);
                cs.sendMessage(MessageColor.POSITIVE + "Armor was set to " + set + ".");
                return true;
            }
        } else if (set.equalsIgnoreCase("chain")) {
            if (!this.ah.isAuthorized(cs, "rcmds.setarmor.chain")) {
                cs.sendMessage(MessageColor.NEGATIVE + "You don't have permission for that type of material!");
                return true;
            } else {
                p.getInventory().setArmorContents(chain);
                p.sendMessage(MessageColor.POSITIVE + "Armor was set to " + set + ".");
                return true;
            }
        } else if (set.equalsIgnoreCase("none")) {
            if (!this.ah.isAuthorized(cs, "rcmds.setarmor.none")) {
                cs.sendMessage(MessageColor.NEGATIVE + "You don't have permission for that type of material!");
                return true;
            } else {
                p.getInventory().setArmorContents(none);
                cs.sendMessage(MessageColor.POSITIVE + "Armor was cleared.");
                return true;
            }
        } else {
            cs.sendMessage(MessageColor.NEGATIVE + "The armor type must be diamond, gold, iron, leather, chain, or none.");
            return true;
        }
    }
}
