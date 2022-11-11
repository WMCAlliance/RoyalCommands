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
import org.bukkit.entity.Animals;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RoyalCommands;

@ReflectCommand
public class CmdErase extends TabCommand {

    public CmdErase(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.LIST.getShort()});
    }
	/**
	 * TODO: Support more categories
	 */

    @Override
    protected List<String> customList(final CommandSender cs, final Command cmd, final String label, final String[] args, final String arg) {
        return new ArrayList<>(Arrays.asList("mobs", "monsters", "animals", "arrows", "boats", "littnt", "all", "minecart", "xp", "paintings", "drops"));
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
        String command = args[0];
        int radius = -1;
        if (args.length > 1) {
            try {
                radius = Integer.valueOf(args[1]);
                if (radius < 0) {
                    cs.sendMessage(MessageColor.NEGATIVE + "Invalid radius!");
                    return true;
                }
            } catch (Exception e) {
                cs.sendMessage(MessageColor.NEGATIVE + "Invalid radius!");
                return true;
            }
        }
        List<Entity> entlist = (radius < 0) ? p.getWorld().getEntities() : p.getNearbyEntities(radius, radius, radius);
        int count = 0;
        if (command.equalsIgnoreCase("mobs")) {
            for (Entity e : entlist) {
                if (!(e instanceof LivingEntity)) continue;
                if (e instanceof Player) continue;
                e.remove();
                count++;
            }
            cs.sendMessage(MessageColor.POSITIVE + "Removed " + MessageColor.NEUTRAL + count + " " + ((count != 1) ? "mobs" : "mob") + MessageColor.POSITIVE + ".");
        } else if (command.equalsIgnoreCase("monsters")) {
            for (Entity e : entlist) {
                if (!(e instanceof Monster)) continue;
                e.remove();
                count++;
            }
            cs.sendMessage(MessageColor.POSITIVE + "Removed " + MessageColor.NEUTRAL + count + " " + ((count != 1) ? "monsters" : "monster") + MessageColor.POSITIVE + ".");
        } else if (command.equalsIgnoreCase("animals")) {
            for (Entity e : entlist) {
                if (!(e instanceof Animals)) continue;
                e.remove();
                count++;
            }
            cs.sendMessage(MessageColor.POSITIVE + "Removed " + MessageColor.NEUTRAL + count + " " + ((count != 1) ? "animals" : "animal") + MessageColor.POSITIVE + ".");
        } else if (command.equalsIgnoreCase("arrows")) {
            for (Entity e : entlist) {
                if (e instanceof Arrow) {
                    e.remove();
                    count++;
                }
            }
            cs.sendMessage(MessageColor.POSITIVE + "Removed " + MessageColor.NEUTRAL + count + " " + ((count != 1) ? "arrows" : "arrow") + MessageColor.POSITIVE + ".");
        } else if (command.equalsIgnoreCase("boats")) {
            for (Entity e : entlist) {
                if (e instanceof Boat) {
                    e.remove();
                    count++;
                }
            }
            cs.sendMessage(MessageColor.POSITIVE + "Removed " + MessageColor.NEUTRAL + count + " " + ((count != 1) ? "boats" : "boat") + MessageColor.POSITIVE + ".");
        } else if (command.equalsIgnoreCase("littnt")) {
            for (Entity e : entlist) {
                if (e instanceof TNTPrimed) {
                    e.remove();
                    count++;
                }
            }
            cs.sendMessage(MessageColor.POSITIVE + "Removed " + MessageColor.NEUTRAL + count + " tnt" + MessageColor.POSITIVE + ".");
        } else if (command.equalsIgnoreCase("all")) {
            for (Entity e : entlist) {
                if (e instanceof Player) continue;
                e.remove();
                count++;
            }
            cs.sendMessage(MessageColor.POSITIVE + "Removed " + MessageColor.NEUTRAL + count + " " + ((count != 1) ? "entities" : "entity") + MessageColor.POSITIVE + ".");
        } else if (command.equalsIgnoreCase("minecarts")) {
            for (Entity e : entlist) {
                if (e instanceof Minecart) {
                    e.remove();
                    count++;
                }
            }
            cs.sendMessage(MessageColor.POSITIVE + "Removed " + MessageColor.NEUTRAL + count + " " + ((count != 1) ? "minecarts" : "minecart") + MessageColor.POSITIVE + ".");
        } else if (command.equalsIgnoreCase("xp")) {
            for (Entity e : entlist) {
                if (e instanceof ExperienceOrb) {
                    e.remove();
                    count++;
                }
            }
            cs.sendMessage(MessageColor.POSITIVE + "Removed " + MessageColor.NEUTRAL + count + " " + ((count != 1) ? "orbs" : "orb") + MessageColor.POSITIVE + ".");
        } else if (command.equalsIgnoreCase("paintings")) {
            for (Entity e : entlist) {
                if (e instanceof Painting) {
                    e.remove();
                    count++;
                }
            }
            cs.sendMessage(MessageColor.POSITIVE + "Removed " + MessageColor.NEUTRAL + count + " " + ((count != 1) ? "paintings" : "painting") + MessageColor.POSITIVE + ".");
        } else if (command.equalsIgnoreCase("drops")) {
            for (Entity e : entlist) {
                if (e instanceof Item) {
                    e.remove();
                    count++;
                }
            }
            cs.sendMessage(MessageColor.POSITIVE + "Removed " + MessageColor.NEUTRAL + count + " " + ((count != 1) ? "drops" : "drop") + MessageColor.POSITIVE + ".");
        } else {
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        return true;
    }
}
