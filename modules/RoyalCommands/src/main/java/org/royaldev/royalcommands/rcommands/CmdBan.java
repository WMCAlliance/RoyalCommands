/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.royaldev.royalcommands.AuthorizationHandler.PermType;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.configuration.PlayerConfiguration;
import org.royaldev.royalcommands.configuration.PlayerConfigurationManager;

@ReflectCommand
public class CmdBan extends TabCommand {

    public CmdBan(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{TabCommand.CompletionType.ONLINE_PLAYER.getShort()});
    }
	
	
    @Override
    protected List<String> customList(final CommandSender cs, final Command cmd, final String label, final String[] args, final String arg) {
		/*
			TODO: Show the default ban message, but allow the user to type anything they like
			Currently, just showing the default message causes the user to get kicked for invalid chat - my guess is due to colour codes
			For now, we just won't show autocomplete
		*/
		// return new ArrayList<>(Arrays.asList(Config.banMessage));
		return new ArrayList<>();
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (args.length < 1) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        OfflinePlayer t = RUtils.getOfflinePlayer(args[0]);
        PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(t);
/*            if (!pcm.getConfExists()) {
                if (args.length > 1 && args[1].equalsIgnoreCase("true")) {
                    args = (String[]) ArrayUtils.remove(args, 1);
                } else {
                    cs.sendMessage(MessageColor.NEGATIVE + "That player does not exist!");
                    return true;
                }
            }*/
        if (!pcm.exists()) pcm.createFile();
        if (this.ah.isAuthorized(t, cmd, PermType.EXEMPT)) {
            cs.sendMessage(MessageColor.NEGATIVE + "You can't ban that player!");
            return true;
        }
        String banreason = (args.length > 1) ? RoyalCommands.getFinalArg(args, 1) : Config.banMessage;
        banreason = RUtils.colorize(banreason);
        pcm.set("banreason", banreason);
        pcm.set("banner", cs.getName());
        pcm.set("bannedat", new Date().getTime());
        pcm.set("bantime", null);
        cs.sendMessage(MessageColor.POSITIVE + "You have banned " + MessageColor.NEUTRAL + t.getName() + MessageColor.POSITIVE + ".");
        RUtils.banPlayer(t, cs, banreason);
        return true;
    }
}
