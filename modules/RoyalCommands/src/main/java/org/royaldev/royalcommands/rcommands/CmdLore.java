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
public class CmdLore extends TabCommand {

    public CmdLore(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{});
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
        Player p = (Player) cs;
        String loreText = RoyalCommands.getFinalArg(args, 0);
        ItemStack is = p.getInventory().getItemInMainHand();
        if (is == null || RUtils.isBlockAir(is.getType())) {
            cs.sendMessage(MessageColor.NEGATIVE + "You can't set lore on air!");
            return true;
        }
        if (loreText.equalsIgnoreCase("clear")) {
            is = RUtils.clearLore(is);
            cs.sendMessage(MessageColor.POSITIVE + "Reset the lore on your " + MessageColor.NEUTRAL + RUtils.getItemName(is) + MessageColor.POSITIVE + ".");
            return true;
        }
        is = RUtils.addLore(is, RUtils.colorize(loreText));
        p.getInventory().setItemInMainHand(is);
        cs.sendMessage(MessageColor.POSITIVE + "Set the lore on your " + MessageColor.NEUTRAL + RUtils.getItemName(is) + MessageColor.POSITIVE + ".");
        return true;
    }
}
