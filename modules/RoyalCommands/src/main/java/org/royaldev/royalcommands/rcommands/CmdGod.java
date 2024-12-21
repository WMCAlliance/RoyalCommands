/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.royaldev.royalcommands.AuthorizationHandler.PermType;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.configuration.PlayerConfiguration;
import org.royaldev.royalcommands.configuration.PlayerConfigurationManager;

@ReflectCommand
public class CmdGod extends TabCommand {

    public CmdGod(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.ONLINE_PLAYER.getShort()});
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (args.length < 1) {
            if (!(cs instanceof Player)) {
                cs.sendMessage(cmd.getDescription());
                return false;
            }
            Player t = (Player) cs;
            PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(t);
			t.setHealth(t.getAttribute(Attribute.MAX_HEALTH).getValue());
            t.setFoodLevel(20);
            t.setSaturation(20F);
            if (!pcm.getBoolean("godmode")) {
                cs.sendMessage(MessageColor.POSITIVE + "You have enabled godmode for yourself.");
                pcm.set("godmode", true);
                return true;
            } else {
                cs.sendMessage(MessageColor.POSITIVE + "You have disabled godmode for yourself.");
                pcm.set("godmode", false);
                return true;
            }
        }
        if (args.length > 0) {
            if (!this.ah.isAuthorized(cs, cmd, PermType.OTHERS)) {
                cs.sendMessage(MessageColor.NEGATIVE + "You don't have permission for that!");
                this.plugin.getLogger().warning(cs.getName() + " was denied access to the command!");
                return true;
            }
            Player t = this.plugin.getServer().getPlayer(args[0]);
            PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(t);
            if (t != null) {
                if (!pcm.getBoolean("godmode")) {
                    if (!pcm.exists()) {
                        cs.sendMessage(MessageColor.NEGATIVE + "That player doesn't exist!");
                        return true;
                    }
                    t.setHealth(t.getAttribute(Attribute.MAX_HEALTH).getValue());
                    t.setFoodLevel(20);
                    t.setSaturation(20F);
                    t.sendMessage(MessageColor.POSITIVE + "The player " + MessageColor.NEUTRAL + cs.getName() + MessageColor.POSITIVE + " has enabled godmode for you!");
                    cs.sendMessage(MessageColor.POSITIVE + "You have enabled godmode for " + MessageColor.NEUTRAL + t.getName() + MessageColor.POSITIVE + ".");
                    pcm.set("godmode", true);
                    return true;
                } else {
                    t.setHealth(t.getAttribute(Attribute.MAX_HEALTH).getValue());
                    t.setFoodLevel(20);
                    t.setSaturation(20F);
                    t.sendMessage(MessageColor.NEGATIVE + "The player " + MessageColor.NEUTRAL + cs.getName() + MessageColor.NEGATIVE + " has disabled godmode for you!");
                }
                cs.sendMessage(MessageColor.POSITIVE + "You have disabled godmode for " + MessageColor.NEUTRAL + t.getName() + MessageColor.POSITIVE + ".");
                pcm.set("godmode", false);
                return true;
            }
        }
        OfflinePlayer t2 = RUtils.getOfflinePlayer(args[0]);
        PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(t2);
        if (!pcm.getBoolean("godmode")) {
            if (!pcm.exists()) {
                cs.sendMessage(MessageColor.NEGATIVE + "That player doesn't exist!");
                return true;
            }
            if (t2.isOnline()) {
                Player pl = (Player) t2;
                pl.setHealth(pl.getAttribute(Attribute.MAX_HEALTH).getValue());
                pl.setFoodLevel(20);
                pl.setSaturation(20F);
                pl.sendMessage(MessageColor.POSITIVE + "The player " + MessageColor.NEUTRAL + cs.getName() + MessageColor.POSITIVE + " has enabled godmode for you!");
            }
            cs.sendMessage(MessageColor.POSITIVE + "You have enabled godmode for " + MessageColor.NEUTRAL + t2.getName() + MessageColor.POSITIVE + ".");
            pcm.set("godmode", true);
            return true;
        } else {
            if (t2.isOnline()) {
                Player pl = (Player) t2;
                pl.setHealth(pl.getAttribute(Attribute.MAX_HEALTH).getValue());
                pl.setFoodLevel(20);
                pl.setSaturation(20F);
                pl.sendMessage(MessageColor.NEGATIVE + "The player " + MessageColor.NEUTRAL + cs.getName() + MessageColor.NEGATIVE + " has disabled godmode for you!");
            }
            cs.sendMessage(MessageColor.POSITIVE + "You have disabled godmode for " + MessageColor.NEUTRAL + t2.getName() + MessageColor.POSITIVE + ".");
            pcm.set("godmode", false);
            return true;
        }
    }
}
