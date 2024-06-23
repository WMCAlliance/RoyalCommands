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
import org.bukkit.event.entity.EntityDamageEvent;
import org.royaldev.royalcommands.AuthorizationHandler.PermType;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RoyalCommands;

import java.util.ArrayList;
import java.util.List;

@ReflectCommand
public class CmdKill extends TabCommand {

    public CmdKill(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.ONLINE_PLAYER.getShort(), CompletionType.CUSTOM.getShort()});
    }

    @Override
    protected List<String> getCustomCompletions(final CommandSender cs, final Command cmd, final String label, final String[] args, final String arg) {
        ArrayList<String> causes = new ArrayList<>();
        for (EntityDamageEvent.DamageCause cause : EntityDamageEvent.DamageCause.values()) {
            if (!cause.name().toLowerCase().startsWith(arg.toLowerCase())) continue;
            causes.add(cause.name().toLowerCase());
        }
        return causes;
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (args.length < 1) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        Player t = this.plugin.getServer().getPlayer(args[0]);
        if (t == null || this.plugin.isVanished(t, cs)) {
            cs.sendMessage(MessageColor.NEGATIVE + "That player does not exist!");
            return true;
        }
        if (this.ah.isAuthorized(t, cmd, PermType.EXEMPT)) {
            cs.sendMessage(MessageColor.NEGATIVE + "You cannot kill that player!");
            return true;
        }
        if (args.length > 1){
            Player p = (Player) cs;
            try{
                EntityDamageEvent.DamageCause cause = EntityDamageEvent.DamageCause.valueOf(args[1].toUpperCase());
                t.setLastDamageCause(new EntityDamageByEntityEvent(p, t, cause, 0D));
            } catch (IllegalArgumentException e) {
                cs.sendMessage(MessageColor.NEGATIVE + "Please use a valid cause of death.");
                return true;
            }

        }
        t.setHealth(0);
        cs.sendMessage(MessageColor.POSITIVE + "You have killed " + MessageColor.NEUTRAL + t.getDisplayName() + MessageColor.POSITIVE + ".");
        return true;
    }
}
