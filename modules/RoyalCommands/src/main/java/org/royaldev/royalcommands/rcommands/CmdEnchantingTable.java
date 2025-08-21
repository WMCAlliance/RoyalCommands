/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.MenuType;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RoyalCommands;

import net.md_5.bungee.chat.TranslationRegistry;

@ReflectCommand
public class CmdEnchantingTable extends TabCommand {

    public CmdEnchantingTable(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{});
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (!(cs instanceof Player)) {
            cs.sendMessage(MessageColor.NEGATIVE + "This command is only available to players!");
            return true;
        }
        Player p = (Player) cs;
        p.openInventory(MenuType.ENCHANTMENT.create(p, TranslationRegistry.INSTANCE.translate("container.enchant")));
        p.sendMessage(MessageColor.POSITIVE + "Opened an enchanting table for you.");
        return true;
    }
}
