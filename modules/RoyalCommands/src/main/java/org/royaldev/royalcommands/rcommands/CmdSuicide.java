/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RoyalCommands;

@ReflectCommand
public class CmdSuicide extends TabCommand {

    public CmdSuicide(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{});
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (!(cs instanceof Player)) {
            cs.sendMessage(MessageColor.NEGATIVE + "This command is only available to players!");
            return true;
        }
        Player p = (Player) cs;
        p.setLastDamageCause(new EntityDamageByEntityEvent(p, p, DamageCause.SUICIDE, 0D));
        p.setHealth(0);
        // There may be a better solution idk
        if(p.getServer().getPluginManager().getPlugin("RoyalDeath") == null){
            this.plugin.getServer().broadcastMessage(MessageColor.NEGATIVE + "The player " + MessageColor.NEUTRAL + p.getDisplayName() + MessageColor.NEGATIVE + " committed suicide.");
        }
        return true;
    }
}
