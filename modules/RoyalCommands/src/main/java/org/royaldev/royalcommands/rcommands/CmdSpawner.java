/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;

@ReflectCommand
public class CmdSpawner extends TabCommand {

    public CmdSpawner(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.CUSTOM.getShort()});
    }
	
	@Override
	protected List<String> getCustomCompletions(final CommandSender cs, final Command cmd, final String label, final String[] args, final String arg) {
		ArrayList<String> spawnable = new ArrayList<>();
            for (EntityType et : EntityType.values()) {
                if (!et.isAlive()) continue;
                if (!et.isSpawnable()) continue;
				String lowerCaseName = et.toString().toLowerCase();
				if (!this.ah.isAuthorized(cs, "rcmds.spawnmob." + lowerCaseName) && !this.ah.isAuthorized(cs, "rcmds.spawnmob.*")) continue;
                if (!lowerCaseName.startsWith(arg.toLowerCase())) continue;
				spawnable.add(lowerCaseName);
			}
		return spawnable;
	}

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (!(cs instanceof Player)) {
            cs.sendMessage(MessageColor.NEGATIVE + "This command is only available to players!");
            return true;
        }
        if (args.length < 1) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        Player p = (Player) cs;
        Block bb = RUtils.getTarget(p);
        if (bb == null) {
            cs.sendMessage(MessageColor.NEGATIVE + "No block found!");
            return true;
        }
        if (!(bb.getState() instanceof CreatureSpawner)) {
            cs.sendMessage(MessageColor.NEGATIVE + "That's not a mob spawner!");
            return true;
        }
        CreatureSpawner crs = (CreatureSpawner) bb.getState();
        EntityType ct;
        try {
            ct = EntityType.valueOf(args[0].toUpperCase());
        } catch (Exception e) {
            cs.sendMessage(MessageColor.NEGATIVE + "Invalid mob!");
            return true;
        }
        if (!this.ah.isAuthorized(cs, "rcmds.spawnmob." + ct.name().toLowerCase()) && !this.ah.isAuthorized(cs, "rcmds.spawnmob.*")) {
            cs.sendMessage(MessageColor.NEGATIVE + "You cannot use mob type " + MessageColor.NEUTRAL + ct.name().toLowerCase() + MessageColor.NEGATIVE + ".");
            return true;
        }
        crs.setSpawnedType(ct);
        cs.sendMessage(MessageColor.POSITIVE + "Spawner type set to " + MessageColor.NEUTRAL + crs.getSpawnedType().toString().toLowerCase().replace("_", " ") + MessageColor.POSITIVE + ".");
        return true;
    }
}
