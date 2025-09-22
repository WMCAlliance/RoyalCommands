/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.rcommands.attributes.SCmdAdd;
import org.royaldev.royalcommands.rcommands.attributes.SCmdClear;
import org.royaldev.royalcommands.rcommands.attributes.SCmdList;
import org.royaldev.royalcommands.rcommands.attributes.SCmdRemove;

@ReflectCommand
public class CmdAttributes extends ParentCommand {

    public CmdAttributes(final RoyalCommands instance, final String name) {
        super(instance, name, true);
        super.addSubCommand(new SCmdAdd(this.plugin, this));
        super.addSubCommand(new SCmdClear(this.plugin, this));
        super.addSubCommand(new SCmdList(this.plugin, this));
        super.addSubCommand(new SCmdRemove(this.plugin, this));
    }

    public boolean validHand(CommandSender cs) {
        if (!(cs instanceof final Player p)) {
            cs.sendMessage(MessageColor.NEGATIVE + "This command is only available to players!");
            return false;
        }
        ItemStack hand = p.getInventory().getItemInMainHand();
        if (RUtils.isBlockAir(hand.getType())) {
            cs.sendMessage(MessageColor.NEGATIVE + "You cannot apply attributes to air!");
            return false;
        }
        return true;
    }
}
