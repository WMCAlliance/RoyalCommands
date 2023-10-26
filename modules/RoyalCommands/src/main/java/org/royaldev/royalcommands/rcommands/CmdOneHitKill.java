/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.configuration.PlayerConfiguration;
import org.royaldev.royalcommands.configuration.PlayerConfigurationManager;

@ReflectCommand
public class CmdOneHitKill extends TabCommand {

    public CmdOneHitKill(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.ONLINE_PLAYER.getShort()});
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (args.length > 0) {
            Player t = this.plugin.getServer().getPlayer(args[0]);
            if (t == null || this.plugin.isVanished(t, cs)) {
                OfflinePlayer op = RUtils.getOfflinePlayer(args[0]);
                PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(op);
                if (!pcm.exists()) {
                    cs.sendMessage(MessageColor.NEGATIVE + "That player does not exist!");
                    return true;
                }
                boolean ohk = pcm.getBoolean("ohk");
                if (!ohk) {
                    pcm.set("ohk", true);
                    cs.sendMessage(MessageColor.POSITIVE + "You have enabled onehitkill mode for " + MessageColor.NEUTRAL + op.getName() + MessageColor.POSITIVE + ".");
                    return true;
                }
                pcm.set("ohk", false);
                cs.sendMessage(MessageColor.POSITIVE + "You have disabled onehitkill mode for " + MessageColor.NEUTRAL + op.getName() + MessageColor.POSITIVE + ".");
                return true;
            }
            Player p = this.plugin.getServer().getPlayer(args[0]);
            PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(p);
            if (!pcm.exists()) {
                cs.sendMessage(MessageColor.NEGATIVE + "That player does not exist!");
                return true;
            }
            boolean ohk = pcm.getBoolean("ohk");
            if (!ohk) {
                pcm.set("ohk", true);
                cs.sendMessage(MessageColor.POSITIVE + "You have enabled onehitkill mode for " + MessageColor.NEUTRAL + p.getName() + MessageColor.POSITIVE + ".");
                p.sendMessage(MessageColor.POSITIVE + "The player " + MessageColor.NEUTRAL + cs.getName() + MessageColor.POSITIVE + " has enabled onehitkill for you.");
                return true;
            }
            pcm.set("ohk", false);
            cs.sendMessage(MessageColor.POSITIVE + "You have disabled onehitkill mode for " + MessageColor.NEUTRAL + p.getName() + MessageColor.POSITIVE + ".");
            p.sendMessage(MessageColor.NEGATIVE + "The player " + MessageColor.NEUTRAL + cs.getName() + MessageColor.NEGATIVE + " has disabled your onehitkill.");
            return true;
        }
        if (args.length < 1) {
            if (!(cs instanceof Player)) {
                cs.sendMessage(cmd.getDescription());
                return false;
            }
            Player p = (Player) cs;
            PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(p);
            boolean ohk = pcm.getBoolean("ohk");
            if (!ohk) {
                pcm.set("ohk", true);
                p.sendMessage(MessageColor.POSITIVE + "You have enabled onehitkill for yourself.");
                return true;
            }
            pcm.set("ohk", false);
            p.sendMessage(MessageColor.POSITIVE + "You have disabled onehitkill for yourself.");
            return true;
        }
        return true;
    }
}
