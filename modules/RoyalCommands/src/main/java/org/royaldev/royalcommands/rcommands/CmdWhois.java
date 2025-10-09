/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang3.BooleanUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.configuration.PlayerConfiguration;
import org.royaldev.royalcommands.configuration.PlayerConfigurationManager;

@ReflectCommand
public class CmdWhois extends TabCommand {

    public CmdWhois(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{TabCommand.CompletionType.ONLINE_PLAYER.getShort()});
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (args.length < 1) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        OfflinePlayer t = null;
        if (args[0].matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}")) {
            t = Bukkit.getOfflinePlayer(UUID.fromString(args[0]));
        } else {
            t = RUtils.getOfflinePlayer(args[0]);
        }
        try {
            if (t == null || !t.isOnline() && !t.hasPlayedBefore()) {
                cs.sendMessage(MessageColor.NEGATIVE + "That player has never played before!");
                return true;
            }
        } catch (Exception e) {
            cs.sendMessage(MessageColor.NEGATIVE + "That player's data is unreadable!");
            return true;
        }
        PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(t);
        DecimalFormat df = new DecimalFormat("#.##");
        String ip = pcm.getString("ip");
        String name = pcm.getString("name");
        String dispname = pcm.getString("dispname");
        cs.sendMessage(MessageColor.POSITIVE + "=====================");
        cs.sendMessage(MessageColor.POSITIVE + " " + ((t.isOnline()) ? "Whois" : "Whowas") + " for " + MessageColor.NEUTRAL + name);
        cs.sendMessage(MessageColor.POSITIVE + " Nickname: " + MessageColor.NEUTRAL + dispname);
        cs.sendMessage(MessageColor.POSITIVE + " UUID: " + MessageColor.NEUTRAL + t.getUniqueId());
        cs.sendMessage(MessageColor.POSITIVE + " IP: " + MessageColor.NEUTRAL + ip);
        cs.sendMessage(MessageColor.POSITIVE + " VIP: " + MessageColor.NEUTRAL + BooleanUtils.toStringYesNo(pcm.getBoolean("vip")));
        cs.sendMessage(MessageColor.POSITIVE + " Muted/Frozen/Jailed: " + MessageColor.NEUTRAL + BooleanUtils.toStringYesNo(pcm.getBoolean("muted")) + MessageColor.POSITIVE + " / " + MessageColor.NEUTRAL + BooleanUtils.toStringYesNo(pcm.getBoolean("frozen")) + MessageColor.POSITIVE + " / " + MessageColor.NEUTRAL + BooleanUtils.toStringYesNo(pcm.getBoolean("jailed")));
        long timestamp = RUtils.getTimeStamp(t, "seen");
        String lastseen = (timestamp < 0) ? "unknown" : RUtils.formatDateDiff(timestamp);
        cs.sendMessage(MessageColor.POSITIVE + " Last seen: " + MessageColor.NEUTRAL + ((t.isOnline()) ? "now" : lastseen));
        cs.sendMessage(MessageColor.POSITIVE + " First played: " + MessageColor.NEUTRAL + RUtils.formatDateDiff(t.getFirstPlayed()) + "ago");
        if (t.isOnline()) {
            final Player p = (Player) t;
            cs.sendMessage(MessageColor.POSITIVE + " Gamemode: " + MessageColor.NEUTRAL + p.getGameMode().name().toLowerCase());
            cs.sendMessage(MessageColor.POSITIVE + " Can fly: " + MessageColor.NEUTRAL + BooleanUtils.toStringYesNo(p.getAllowFlight()));
            cs.sendMessage(MessageColor.POSITIVE + " Buddha/God/Mob Ignored/One Hit Kill: " + MessageColor.NEUTRAL + BooleanUtils.toStringYesNo(pcm.getBoolean("buddha")) + MessageColor.POSITIVE + " / " + MessageColor.NEUTRAL + BooleanUtils.toStringYesNo(pcm.getBoolean("godmode")) + MessageColor.POSITIVE + " / " + MessageColor.NEUTRAL + BooleanUtils.toStringYesNo(pcm.getBoolean("mobignored")) + MessageColor.POSITIVE + " / " + MessageColor.NEUTRAL + BooleanUtils.toStringYesNo(pcm.getBoolean("ohk")));
            cs.sendMessage(MessageColor.POSITIVE + " Health/Hunger/Saturation: " + MessageColor.NEUTRAL + df.format(p.getHealth() / 2) + MessageColor.POSITIVE + " / " + MessageColor.NEUTRAL + p.getFoodLevel() / 2 + MessageColor.POSITIVE + " / " + MessageColor.NEUTRAL + p.getSaturation() / 2);
            cs.sendMessage(MessageColor.POSITIVE + " Total Exp/Exp %/Level: " + MessageColor.NEUTRAL + p.getTotalExperience() + MessageColor.POSITIVE + " / " + MessageColor.NEUTRAL + df.format(p.getExp() * 100) + "%" + MessageColor.POSITIVE + " / " + MessageColor.NEUTRAL + p.getLevel());
            PlayerInventory inv = p.getInventory();
            String mainHand = RUtils.getItemName(inv.getItemInMainHand());
            ItemStack offH = inv.getItemInOffHand();
            String offHand = offH != null && !offH.getType().equals(Material.AIR) ? RUtils.getItemName(offH) : "";
            cs.sendMessage(MessageColor.POSITIVE + " Item in hand: " + MessageColor.NEUTRAL + mainHand + (!offHand.isEmpty() ? MessageColor.POSITIVE + " / " + MessageColor.NEUTRAL + offHand : ""));
            cs.sendMessage(MessageColor.POSITIVE + " Alive for: " + MessageColor.NEUTRAL + RUtils.formatDateDiff(new Date().getTime() - p.getTicksLived() * 50));
            Location l = p.getLocation();
            cs.sendMessage(MessageColor.POSITIVE + " Last position: " + "(" + MessageColor.NEUTRAL + df.format(l.getX()) + MessageColor.POSITIVE + ", " + MessageColor.NEUTRAL + df.format(l.getY()) + MessageColor.POSITIVE + ", " + MessageColor.NEUTRAL + df.format(l.getZ()) + MessageColor.POSITIVE + ") in " + MessageColor.NEUTRAL + RUtils.getMVWorldName(l.getWorld()));
        } else {
            String lP = "lastposition.";
            World w = (pcm.isSet(lP + "world")) ? this.plugin.getServer().getWorld(pcm.getString(lP + "world")) : null;
            if (w != null) {
                Location l = new Location(w, pcm.getDouble(lP + "x"), pcm.getDouble(lP + "y"), pcm.getDouble(lP + "z"));
                cs.sendMessage(MessageColor.POSITIVE + " Last position: " + "(" + MessageColor.NEUTRAL + df.format(l.getX()) + MessageColor.POSITIVE + ", " + MessageColor.NEUTRAL + df.format(l.getY()) + MessageColor.POSITIVE + ", " + MessageColor.NEUTRAL + df.format(l.getZ()) + MessageColor.POSITIVE + ") in " + MessageColor.NEUTRAL + RUtils.getMVWorldName(l.getWorld()));
            }
        }
        cs.sendMessage(MessageColor.POSITIVE + "=====================");
        return true;
    }
}
