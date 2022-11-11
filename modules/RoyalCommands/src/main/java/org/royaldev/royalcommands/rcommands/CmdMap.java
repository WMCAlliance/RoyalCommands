/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.map.MapView.Scale;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ReflectCommand
public class CmdMap extends TabCommand {

    public CmdMap(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.LIST.getShort(), CompletionType.CUSTOM.getShort()});
    }

    @Override
    protected List<String> customList(final CommandSender cs, final Command cmd, final String label, final String[] args, final String arg) {
        return new ArrayList<>(Arrays.asList(
                "setlock", "locked", "lock",
                "scale", "scaling", "setscale", "setscaling",
                "track", "tracking",
                "unlimited", "unlimit",
                "reposition", "position", "pos", "repos", "setposition", "setpos", "coords", "coordinates", "setcoords", "setcoordinates",
                "world", "setworld",
                "info",
                "help", "?"
        ));
    }

    @Override
    protected Enum[] customEnum(final CommandSender cs, final Command cmd, final String label, final String[] args, final String arg) {
        return Scale.values();
    }

    @Override
    protected List<String> getCustomCompletions(final CommandSender cs, final Command cmd, final String label, final String[] args, final String arg) {
        if (cs instanceof Player) {
            Player p = (Player) cs;
            /**
             * TODO: Depending on the first parameter, either show numbers, the user's current coordinates, or a list of worlds
             */
            switch (args[0]) {
                case "scale":
                case "scaling":
                case "setscale":
                case "setscaling":
                    return getCompletionsFor(cs, cmd, label, args, CompletionType.ENUM);
                case "reposition":
                case "position":
                case "pos":
                case "repos":
                case "setposition":
                case "setpos":
                case "coords":
                case "coordinates":
                case "setcoords":
                case "setcoordinates":
                    return new ArrayList<>(Arrays.asList(
                            p.getLocation().getBlockX() + " " + p.getLocation().getBlockZ(),
                            p.getWorld().getSpawnLocation().getBlockX() + " " + p.getWorld().getSpawnLocation().getBlockZ()
                    ));
                case "world":
                case "setworld":
                    return getCompletionsFor(cs, cmd, label, args, CompletionType.WORLD);
            }
            return new ArrayList<>();
        }
        return new ArrayList<>();
    }

    private String combineEnums(Enum[] es) {
        StringBuilder sb = new StringBuilder();
        for (Enum e : es) {
            sb.append(MessageColor.NEUTRAL);
            sb.append(e.name());
            sb.append(MessageColor.RESET);
            sb.append(", ");
        }
        return sb.substring(0, sb.length() - 4);
    }

    private boolean subcommandMatches(String subcommand, String... matchAgainst) {
        for (String aMatchAgainst : matchAgainst) if (subcommand.equalsIgnoreCase(aMatchAgainst)) return true;
        return false;
    }

    private void updateMap(Player p, MapView mv) {
        p.sendMap(mv);
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (!(cs instanceof Player)) {
            cs.sendMessage(MessageColor.NEGATIVE + "This command is only available to players!");
            return true;
        }
        Player p = (Player) cs;
        if (args.length < 1) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        String subcommand = args[0].toLowerCase();
        if (subcommandMatches(subcommand, "help", "?")) {
            cs.sendMessage(MessageColor.NEUTRAL + "/" + label + MessageColor.POSITIVE + " help:");
            cs.sendMessage("  " + MessageColor.POSITIVE + "/" + label + MessageColor.NEUTRAL + " scale [scaletype]" + MessageColor.POSITIVE + " - " + MessageColor.NEUTRAL + "Sets the scale of the map in hand.");
            cs.sendMessage("  " + MessageColor.POSITIVE + "/" + label + MessageColor.NEUTRAL + " position [x] [z]" + MessageColor.POSITIVE + " - " + MessageColor.NEUTRAL + "Sets the center position of the map in hand.");
            cs.sendMessage("  " + MessageColor.POSITIVE + "/" + label + MessageColor.NEUTRAL + " lock" + MessageColor.POSITIVE + " - " + MessageColor.NEUTRAL + "Sets the lock status of the map in hand.");
            cs.sendMessage("  " + MessageColor.POSITIVE + "/" + label + MessageColor.NEUTRAL + " tracking" + MessageColor.POSITIVE + " - " + MessageColor.NEUTRAL + "Sets if tracking should be used on the map in hand.");
            cs.sendMessage("  " + MessageColor.POSITIVE + "/" + label + MessageColor.NEUTRAL + " unlimited" + MessageColor.POSITIVE + " - " + MessageColor.NEUTRAL + "Sets if unlimited tracking should be used on the map in hand.");
            cs.sendMessage("  " + MessageColor.POSITIVE + "/" + label + MessageColor.NEUTRAL + " world [world]" + MessageColor.POSITIVE + " - " + MessageColor.NEUTRAL + "Changes the world displayed by the map in hand.");
            cs.sendMessage("  " + MessageColor.POSITIVE + "/" + label + MessageColor.NEUTRAL + " info" + MessageColor.POSITIVE + " - " + MessageColor.NEUTRAL + "Displays information about the map in hand.");
            cs.sendMessage("  " + MessageColor.POSITIVE + "/" + label + MessageColor.NEUTRAL + " help" + MessageColor.POSITIVE + " - " + MessageColor.NEUTRAL + "Displays this help.");
            //cs.sendMessage("  " + MessageColor.POSITIVE + "/" + label + MessageColor.NEUTRAL + " subcommand" + MessageColor.POSITIVE + " - " + MessageColor.NEUTRAL + "Description");
            return true;
        }
        ItemStack hand = p.getInventory().getItemInMainHand();
        MapMeta mapMeta = (MapMeta) hand.getItemMeta();
        if (hand == null || hand.getType() != Material.FILLED_MAP) {
            cs.sendMessage(MessageColor.NEGATIVE + "You must be holding a map to use this subcommand!");
            return true;
        }
        if (!mapMeta.hasMapView()) {
            cs.sendMessage(MessageColor.NEGATIVE + "You must be holding a map to use this subcommand!");
            return true;
        }
        MapView mv = mapMeta.getMapView();
        if (subcommandMatches(subcommand, "scale", "scaling", "setscale", "setscaling")) {
            if (args.length < 2) {
                cs.sendMessage(combineEnums(Scale.values()));
                cs.sendMessage(MessageColor.NEGATIVE + "Please specify a scale.");
                return true;
            }
            String sscale = args[1].toUpperCase();
            Scale mvs;
            try {
                mvs = Scale.valueOf(sscale);
            } catch (IllegalArgumentException e) {
                cs.sendMessage(combineEnums(Scale.values()));
                cs.sendMessage(MessageColor.NEGATIVE + "Invalid scale type.");
                return true;
            }
            mv.setScale(mvs);
            updateMap(p, mv);
            p.getInventory().setItemInMainHand(hand);
            cs.sendMessage(MessageColor.POSITIVE + "Set the scale of map " + MessageColor.NEUTRAL + mv.getId() + MessageColor.POSITIVE + " to " + MessageColor.NEUTRAL + mvs.name().toLowerCase().replace("_", " ") + MessageColor.POSITIVE + ".");
            return true;
        } else if (subcommandMatches(subcommand, "reposition", "position", "pos", "repos", "setposition", "setpos", "coords", "coordinates", "setcoords", "setcoordinates")) {
            if (args.length < 3) {
                cs.sendMessage(MessageColor.NEGATIVE + "Please specify the new X and Z coordinates for the center of the map.");
                return true;
            }
            int x, z;
            try {
                x = Integer.parseInt(args[1]);
                z = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                cs.sendMessage(MessageColor.NEGATIVE + "Those coordinates were invalid!");
                return true;
            }
            mv.setCenterX(x);
            mv.setCenterZ(z);
            updateMap(p, mv);
            p.getInventory().setItemInMainHand(hand);
            cs.sendMessage(MessageColor.POSITIVE + "Set the center of map " + MessageColor.NEUTRAL + mv.getId() + MessageColor.POSITIVE + " to " + MessageColor.NEUTRAL + x + MessageColor.POSITIVE + ", " + MessageColor.NEUTRAL + z + MessageColor.POSITIVE + ".");
            return true;
        } else if (subcommandMatches(subcommand, "world", "setworld")) {
            if (args.length < 2) {
                cs.sendMessage(MessageColor.NEGATIVE + "Specify the world to set this map to.");
                return true;
            }
            String sworld = args[1];
            World w = this.plugin.getServer().getWorld(sworld);
            if (w == null) {
                cs.sendMessage(MessageColor.NEGATIVE + "No such world!");
                return true;
            }
            mv.setWorld(w);
            updateMap(p, mv);
            p.getInventory().setItemInMainHand(hand);
            cs.sendMessage(MessageColor.POSITIVE + "Set the world of map " + MessageColor.NEUTRAL + mv.getId() + MessageColor.POSITIVE + " to " + MessageColor.NEUTRAL + w.getName() + MessageColor.POSITIVE + ".");
            return true;
        } else if (subcommandMatches(subcommand, "info", "getinfo", "information", "getinformation")) {
            cs.sendMessage(MessageColor.POSITIVE + "Information about map " + MessageColor.NEUTRAL + mv.getId() + MessageColor.POSITIVE + ":");
            cs.sendMessage("  " + MessageColor.POSITIVE + "Center coordinates: " + MessageColor.NEUTRAL + mv.getCenterX() + MessageColor.POSITIVE + ", " + MessageColor.NEUTRAL + mv.getCenterZ());
            cs.sendMessage("  " + MessageColor.POSITIVE + "World: " + MessageColor.NEUTRAL + mv.getWorld().getName());
            cs.sendMessage("  " + MessageColor.POSITIVE + "Scale: " + MessageColor.NEUTRAL + RUtils.getFriendlyEnumName(mv.getScale()));
            cs.sendMessage("  " + MessageColor.POSITIVE + "Lock: " + MessageColor.NEUTRAL + mv.isLocked());
            cs.sendMessage("  " + MessageColor.POSITIVE + "Tracking: " + MessageColor.NEUTRAL + mv.isTrackingPosition());
            cs.sendMessage("  " + MessageColor.POSITIVE + "Unlimited Tracking: " + MessageColor.NEUTRAL + mv.isUnlimitedTracking());
            //cs.sendMessage("  " + MessageColor.POSITIVE + "Char: " + MessageColor.NEUTRAL + "stuff");
            return true;
        } else if (subcommandMatches(subcommand, "render", "fullrender")) {
            return true;
        } else if (subcommandMatches(subcommand, "lock", "locked", "setlock")) {
            if (!mv.isLocked()) {
                mv.setLocked(true);
                cs.sendMessage(MessageColor.POSITIVE + "Map " + MessageColor.NEUTRAL + mv.getId() + MessageColor.POSITIVE + " set to locked.");
            } else {
                mv.setLocked(false);
                cs.sendMessage(MessageColor.POSITIVE + "Map " + MessageColor.NEUTRAL + mv.getId() + MessageColor.POSITIVE + " set to unlocked.");
            }
            updateMap(p, mv);
            p.getInventory().setItemInMainHand(hand);
            return true;
        } else if (subcommandMatches(subcommand, "track", "tracking")) {
            if (!mv.isTrackingPosition()) {
                mv.setTrackingPosition(true);
                cs.sendMessage(MessageColor.POSITIVE + "Tracking for map " + MessageColor.NEUTRAL + mv.getId() + MessageColor.POSITIVE + " enabled.");
            } else {
                mv.setTrackingPosition(false);
                cs.sendMessage(MessageColor.POSITIVE + "Tracking for map " + MessageColor.NEUTRAL + mv.getId() + MessageColor.POSITIVE + " disabled");
            }
            updateMap(p, mv);
            p.getInventory().setItemInMainHand(hand);
            return true;
        } else if (subcommandMatches(subcommand, "unlimit", "unlimited")) {
            if (!mv.isUnlimitedTracking()) {
                mv.setUnlimitedTracking(true);
                cs.sendMessage(MessageColor.POSITIVE + "Unlimited tracking for map " + MessageColor.NEUTRAL + mv.getId() + MessageColor.POSITIVE + " enabled.");
            } else {
                mv.setUnlimitedTracking(false);
                cs.sendMessage(MessageColor.POSITIVE + "Unlimited tracking for map " + MessageColor.NEUTRAL + mv.getId() + MessageColor.POSITIVE + " disabled.");
            }
            updateMap(p, mv);
            p.getInventory().setItemInMainHand(hand);
            return true;
        } else {
            cs.sendMessage(MessageColor.NEGATIVE + "Unknown subcommand. Try " + MessageColor.NEUTRAL + "/" + label + " help" + MessageColor.NEGATIVE + ".");
            return true;
        }
    }
}
