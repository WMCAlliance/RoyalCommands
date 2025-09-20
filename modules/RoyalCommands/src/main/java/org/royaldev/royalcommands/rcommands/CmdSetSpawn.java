/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.configuration.Configuration;

@ReflectCommand
public class CmdSetSpawn extends TabCommand {

    public CmdSetSpawn(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.LIST.getShort()});
    }

    @Override
    protected List<String> customList(final CommandSender cs, final Command cmd, final String label, final String[] args, final String arg) {
        String[] ranks = null;
        if (!this.plugin.vh.usingVault()) ranks = new String[0];
        else ranks = this.plugin.vh.getPermission().getGroups();
        return new ArrayList<>(Arrays.asList(ranks));
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (!(cs instanceof Player)) {
            cs.sendMessage(MessageColor.NEGATIVE + "This command is only available to players!");
            return true;
        }
        Player p = (Player) cs;
        String group = (args.length > 0) ? "." + args[0].toLowerCase() : "";
        Configuration spawns = Configuration.getConfiguration("spawns.yml");
        double x = p.getLocation().getX();
        double y = p.getLocation().getY();
        double z = p.getLocation().getZ();
        String w = p.getWorld().getName();
        if (group.equals("")) p.getWorld().setSpawnLocation((int) x, (int) y, (int) z);
        spawns.setLocation("spawns." + w + group, p.getLocation());
        p.getWorld().setSpawnLocation((int) x, (int) y, (int) z);
        String forGroup = (group.isEmpty()) ? "" : " for group " + MessageColor.NEUTRAL + group + MessageColor.POSITIVE;
        cs.sendMessage(MessageColor.POSITIVE + "The spawn point of " + MessageColor.NEUTRAL + RUtils.getMVWorldName(p.getWorld()) + MessageColor.POSITIVE + " is set" + forGroup + ".");
        return true;
    }
}
