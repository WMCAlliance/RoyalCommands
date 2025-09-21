/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;

@ReflectCommand
public class CmdLess extends TabCommand {

    public CmdLess(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{});
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (!(cs instanceof Player)) {
            cs.sendMessage(MessageColor.NEGATIVE + "This command is only available to players!");
            return true;
        }
        Player p = (Player) cs;
        if (args.length > 0 && args[0].equalsIgnoreCase("all")) {
            for (ItemStack i : p.getInventory()) {
                if (i == null || i.getType().equals(Material.AIR)) continue;
                i.setAmount(1);
            }
            cs.sendMessage(MessageColor.POSITIVE + "All items in your inventory have been reduced to one.");
            return true;
        }
        ItemStack hand = p.getInventory().getItemInMainHand();
        if (RUtils.isBlockAir(hand.getType())) {
            cs.sendMessage(MessageColor.NEGATIVE + "You can't spawn air!");
            return true;
        }
        hand.setAmount(1);
        cs.sendMessage(MessageColor.POSITIVE + "All of the item in hand, except for one, has been removed.");
        return true;
    }
}
