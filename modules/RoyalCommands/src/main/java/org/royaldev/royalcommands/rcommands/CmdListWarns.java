/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import java.util.List;
import org.bukkit.OfflinePlayer;
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
public class CmdListWarns extends TabCommand {

    public CmdListWarns(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.ONLINE_PLAYER.getShort()});
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (!(cs instanceof Player) && args.length < 1) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        if (args.length > 1 && !this.ah.isAuthorized(cs, cmd, PermType.OTHERS)) {
            RUtils.dispNoPerms(cs, "You're not allowed to view other players' warnings.");
            return true;
        }
        OfflinePlayer op = RUtils.getOfflinePlayer(args.length > 0 ? args[0] : cs.getName());
        PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(op);
        if (!pcm.exists()) {
            cs.sendMessage(MessageColor.NEGATIVE + "That player does not exist!");
            return true;
        }
        List<String> warns = pcm.getStringList("warns");
        if (warns == null || warns.isEmpty()) {
            cs.sendMessage(MessageColor.NEUTRAL + op.getName() + MessageColor.POSITIVE + " has no warnings!");
            return true;
        }
        for (int i = 0; i < warns.size(); i++)
            cs.sendMessage(MessageColor.NEUTRAL + "" + (i + 1) + ". " + warns.get(i).split("\\u00b5")[0]);
        return true;
    }
}
