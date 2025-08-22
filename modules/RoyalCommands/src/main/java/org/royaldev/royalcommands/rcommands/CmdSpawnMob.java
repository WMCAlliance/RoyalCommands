/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;

import net.md_5.bungee.api.chat.TranslatableComponent;

@ReflectCommand
public class CmdSpawnMob extends TabCommand {

    public CmdSpawnMob(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.CUSTOM.getShort()});
    }

	@Override
	protected List<String> getCustomCompletions(final CommandSender cs, final Command cmd, final String label, final String[] args, final String arg) {
		ArrayList<String> spawnable = new ArrayList<>();
            for (EntityType et : Registry.ENTITY_TYPE) {
                if (!et.isAlive()) continue;
                if (!et.isSpawnable()) continue;
                NamespacedKey key = et.getKeyOrNull();
				String name = key.getKey();
				if (!this.ah.isAuthorized(cs, "rcmds.spawnmob." + name) && !this.ah.isAuthorized(cs, "rcmds.spawnmob.*")) continue;
                if (!RUtils.hasKeyMatch(key, arg)) continue;
				spawnable.add(key.toString());
			}
		return spawnable;
	}

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (!(cs instanceof Player)) {
            cs.sendMessage(MessageColor.NEGATIVE + "This command is only available to players!");
            return true;
        }
        Player p = (Player) cs;
        if (args.length < 1) {
            StringBuilder sb = new StringBuilder();
            for (EntityType et : Registry.ENTITY_TYPE) {
                if (!et.isAlive()) continue;
                if (!et.isSpawnable()) continue;
                sb.append(MessageColor.RESET);
                sb.append(", ");
                sb.append(MessageColor.NEUTRAL);
                sb.append(et.getKeyOrNull().getKey());
            }
            cs.sendMessage(sb.substring(4));
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        Block bb = RUtils.getTarget(p);
        EntityType c;
        Location l = bb.getLocation();
        l.setY(l.getY() + 1);
        try {
            c = Registry.ENTITY_TYPE.getOrThrow(NamespacedKey.fromString(args[0]));
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            for (EntityType et : Registry.ENTITY_TYPE) {
                if (!et.isAlive()) continue;
                if (!et.isSpawnable()) continue;
                sb.append(MessageColor.RESET);
                sb.append(", ");
                sb.append(MessageColor.NEUTRAL);
                sb.append(et.getKeyOrNull().getKey());
            }
            cs.sendMessage(sb.substring(4));
            cs.sendMessage(MessageColor.NEGATIVE + "Invalid mob!");
            return true;
        }
        if (!this.ah.isAuthorized(cs, "rcmds.spawnmob." + c.getKeyOrNull().getKey()) && !this.ah.isAuthorized(cs, "rcmds.spawnmob.*")) {
            cs.sendMessage(MessageColor.NEGATIVE + "You don't have permission to spawn " + MessageColor.NEUTRAL + c.name().toLowerCase() + MessageColor.NEGATIVE + ".");
            return true;
        }
        if (args.length > 1) {
            int i;
            try {
                i = Integer.parseInt(args[1]);
            } catch (Exception e) {
                cs.sendMessage(MessageColor.NEGATIVE + "Invalid amount specified!");
                return true;
            }
            if (i < 0) {
                cs.sendMessage(MessageColor.NEGATIVE + "Invalid amount specified!");
                return true;
            }
            if (i > Config.spawnmobLimit && !this.ah.isAuthorized(cs, "rcmds.exempt.limit.spawnmob")) {
                cs.sendMessage(MessageColor.NEGATIVE + "The amount specified was larger than the allowed amount.");
                cs.sendMessage(MessageColor.NEGATIVE + "Setting amount to " + MessageColor.NEUTRAL + Config.spawnmobLimit + MessageColor.NEGATIVE + ".");
                i = Config.spawnmobLimit;
            }
            try {
                for (int a = 0; a < i; a++) p.getWorld().spawnEntity(l, c);
                cs.sendMessage(MessageColor.POSITIVE + "Spawned " + MessageColor.NEUTRAL + i + MessageColor.POSITIVE + " of " + MessageColor.NEUTRAL + new TranslatableComponent(c.getTranslationKey()).toPlainText() + MessageColor.POSITIVE + ".");
            } catch (Exception e) {
                cs.sendMessage(MessageColor.NEGATIVE + "Uh-oh! This mob is not currently working with this command.");
            }
            return true;
        }
        Entity spawned;
        try {
            spawned = p.getWorld().spawnEntity(l, c);
        } catch (Exception e) {
            cs.sendMessage(MessageColor.NEGATIVE + "Uh-oh! This mob is not currently working with this command.");
            return true;
        }
        if (spawned != null)
            cs.sendMessage(MessageColor.POSITIVE + "Spawned " + MessageColor.NEUTRAL + "1" + MessageColor.POSITIVE + " of " + MessageColor.NEUTRAL + new TranslatableComponent(c.getTranslationKey()).toPlainText() + MessageColor.POSITIVE + ".");
        else
            cs.sendMessage(MessageColor.NEGATIVE + "Could not spawn " + MessageColor.NEUTRAL + new TranslatableComponent(c.getTranslationKey()).toPlainText() + MessageColor.NEGATIVE + ".");
        return true;
    }
}
