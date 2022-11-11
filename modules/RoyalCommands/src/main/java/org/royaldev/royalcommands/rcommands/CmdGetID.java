/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;

@ReflectCommand
public class CmdGetID extends TabCommand {

    public CmdGetID(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{});
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (!(cs instanceof Player)) {
            cs.sendMessage(MessageColor.NEGATIVE + "This command is only available to players!");
            return true;
        }
        Player p = (Player) cs;
        ItemStack hand = p.getInventory().getItemInMainHand();
        // int id = hand.getTypeId(); // TODO: Do something better here
		int id = -1;
        short damage = hand.getDurability();
        byte data = hand.getData().getData();
        String name = RUtils.getItemName(hand);
        Map<Enchantment, Integer> enchants = hand.getEnchantments();
        cs.sendMessage(MessageColor.NEUTRAL + name + MessageColor.POSITIVE + ": " + MessageColor.NEUTRAL + id + MessageColor.POSITIVE + " (damage: " + MessageColor.NEUTRAL + damage + MessageColor.POSITIVE + ", materialdata: " + MessageColor.NEUTRAL + data + MessageColor.POSITIVE + ")");
        if (!enchants.isEmpty()) {
            cs.sendMessage(MessageColor.POSITIVE + "Enchantments:");
            for (Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                int lvl = entry.getValue();
                cs.sendMessage(" " + MessageColor.NEUTRAL + entry.getKey().getName().toLowerCase() + " " + lvl);
            }
        }
        return true;
    }
}
