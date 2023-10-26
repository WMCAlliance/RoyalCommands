/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.rcommands.CmdGive;
import org.royaldev.royalcommands.rcommands.CmdTime;
import org.royaldev.royalcommands.rcommands.CmdWarp;
import org.royaldev.royalcommands.rcommands.CmdWeather;
import org.royaldev.royalcommands.rcommands.CmdWeather.WeatherType;
import org.royaldev.royalcommands.wrappers.player.MemoryRPlayer;
import org.royaldev.royalcommands.wrappers.player.RPlayer;

// This code is so ancient... It *really* needs a rewrite.
// TODO: Rewrite

public class SignListener implements Listener {

    private final RoyalCommands plugin;

    public SignListener(RoyalCommands instance) {
        this.plugin = instance;
    }

    private Double getCharge(String line) {
        if (!line.isEmpty()) {
            double amount;
            try {
                amount = Double.parseDouble(line);
            } catch (Exception ex) {
                return null;
            }
            if (amount < 0) return null;
            return amount;
        }
        return -1D;
    }

    @EventHandler
    public void colorSigns(SignChangeEvent e) {
        Player p = e.getPlayer();
        if (!this.plugin.ah.isAuthorized(p, "rcmds.signedit.color")) return;
        for (int i = 0; i < 4; i++) e.setLine(i, RUtils.colorize(e.getLine(i)));
    }

    @EventHandler
    public void onInt(PlayerInteractEvent e) {
        if (e.isCancelled()) return;
        if (e.getClickedBlock() == null) return;
        if (!(e.getClickedBlock().getState() instanceof Sign)) return;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getPlayer() == null) return;
        Sign s = (Sign) e.getClickedBlock().getState();
        Player p = e.getPlayer();
        String line1 = ChatColor.stripColor(s.getLine(0)).trim();
        String line2 = ChatColor.stripColor(s.getLine(1)).trim();
        String line3 = ChatColor.stripColor(s.getLine(2)).trim();
        String line4 = ChatColor.stripColor(s.getLine(3)).trim();

        //Warp signs
        if (line1.equalsIgnoreCase("[warp]")) {
            if (!this.plugin.ah.isAuthorized(p, "rcmds.sign.use.warp")) {
                RUtils.dispNoPerms(p);
                return;
            }
            if (line2.isEmpty()) {
                s.setLine(0, MessageColor.NEGATIVE + line1);
                p.sendMessage(MessageColor.NEGATIVE + "No warp specified.");
                return;
            }

            Double charge = getCharge(line3);
            if (charge == null) {
                p.sendMessage(MessageColor.NEGATIVE + "The cost is invalid!");
                s.setLine(0, MessageColor.NEGATIVE + line1);
                return;
            } else if (charge >= 0) {
                s.setLine(2, ChatColor.DARK_GREEN + line3);
                boolean did = RUtils.chargePlayer(p, charge);
                if (!did) return;
            }

            Location warpLoc = CmdWarp.pWarp(p, line2.toLowerCase());
            if (warpLoc == null) {
                return;
            }
            final RPlayer rp = MemoryRPlayer.getRPlayer(p);
            rp.getTeleporter().teleport(warpLoc);
        }

        //Time signs
        if (line1.equalsIgnoreCase(ChatColor.stripColor("[time]"))) {
            if (!this.plugin.ah.isAuthorized(p, "rcmds.sign.use.time")) {
                RUtils.dispNoPerms(p);
                return;
            }
            if (line2.isEmpty() || (CmdTime.getValidTime(line2) == null)) {
                s.setLine(0, MessageColor.NEGATIVE + line1);
                p.sendMessage(MessageColor.NEGATIVE + "Invalid time specified!");
                return;
            }

            Double charge = getCharge(line3);
            if (charge == null) {
                p.sendMessage(MessageColor.NEGATIVE + "The cost is invalid!");
                s.setLine(0, MessageColor.NEGATIVE + line1);
                return;
            } else if (charge >= 0) {
                s.setLine(2, ChatColor.DARK_GREEN + line3);
                boolean did = RUtils.chargePlayer(p, charge);
                if (!did) return;
            }

            long time = CmdTime.getValidTime(line2);
            p.getWorld().setTime(time);
        }

        //Free signs
        if (line1.equalsIgnoreCase("[free]")) {
            if (!this.plugin.ah.isAuthorized(p, "rcmds.sign.use.free")) {
                RUtils.dispNoPerms(p);
                return;
            }

            Double charge = getCharge(line3);
            if (charge == null) {
                p.sendMessage(MessageColor.NEGATIVE + "The cost is invalid!");
                s.setLine(0, MessageColor.NEGATIVE + line1);
                return;
            } else if (charge >= 0) {
                s.setLine(2, ChatColor.DARK_GREEN + line3);
                if (!RUtils.chargePlayer(p, charge)) return;
            }
            line2 = line2.replace(" ", "_");
            ItemStack stack = RUtils.getItem(line2, null);
            if (stack == null) {
                s.setLine(1, MessageColor.NEGATIVE + line2);
                p.sendMessage(MessageColor.NEGATIVE + "That material is invalid!");
                return;
            } else s.setLine(1, line2.toLowerCase().replace("_", " "));
            RUtils.showFilledChest(p, line2);
        }

        //Disposal signs
        if (line1.equalsIgnoreCase("[disposal]")) {
            if (!this.plugin.ah.isAuthorized(p, "rcmds.sign.use.disposal")) {
                RUtils.dispNoPerms(p);
                return;
            }

            Double charge = getCharge(line2);
            if (charge == null) {
                p.sendMessage(MessageColor.NEGATIVE + "The cost is invalid!");
                s.setLine(0, MessageColor.NEGATIVE + line1);
                return;
            } else if (charge >= 0) {
                s.setLine(1, ChatColor.DARK_GREEN + line2);
                boolean did = RUtils.chargePlayer(p, charge);
                if (!did) return;
            }

            RUtils.showEmptyChest(p, "Disposal");
        }

        //Heal signs
        if (line1.equalsIgnoreCase("[heal]")) {
            if (!this.plugin.ah.isAuthorized(p, "rcmds.sign.use.heal")) {
                RUtils.dispNoPerms(p);
                return;
            }

            Double charge = getCharge(line2);
            if (charge == null) {
                p.sendMessage(MessageColor.NEGATIVE + "The cost is invalid!");
                s.setLine(0, MessageColor.NEGATIVE + line1);
                return;
            } else if (charge >= 0) {
                s.setLine(1, ChatColor.DARK_GREEN + line2);
                boolean did = RUtils.chargePlayer(p, charge);
                if (!did) return;
            }

            p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        }

        //Weather signs
        if (line1.equalsIgnoreCase("[weather]")) {
            if (!this.plugin.ah.isAuthorized(p, "rcmds.sign.use.weather")) {
                RUtils.dispNoPerms(p);
                return;
            }
            Double charge = getCharge(line3);
            if (charge == null) {
                p.sendMessage(MessageColor.NEGATIVE + "The cost is invalid!");
                s.setLine(0, MessageColor.NEGATIVE + line1);
                return;
            } else if (charge >= 0) {
                s.setLine(2, ChatColor.DARK_GREEN + line3);
                boolean did = RUtils.chargePlayer(p, charge);
                if (!did) return;
            }
            CmdWeather.changeWeather(p, WeatherType.getWeatherType(line2.trim()));
        }

        //Give signs
        if (line1.equalsIgnoreCase("[give]")) {
            if (!this.plugin.ah.isAuthorized(p, "rcmds.sign.use.give")) {
                RUtils.dispNoPerms(p);
                return;
            }
            int amount;
            try {
                amount = Integer.parseInt(line3);
            } catch (Exception ex) {
                p.sendMessage(MessageColor.NEGATIVE + "The amount was not a valid number!");
                return;
            }
            Double charge = getCharge(line4);
            if (charge == null) {
                p.sendMessage(MessageColor.NEGATIVE + "The cost is invalid!");
                s.setLine(0, MessageColor.NEGATIVE + line1);
                return;
            } else if (charge >= 0) {
                s.setLine(3, ChatColor.DARK_GREEN + line4);
                boolean did = RUtils.chargePlayer(p, charge);
                if (!did) return;
            }
            CmdGive.giveItemStandalone(null, p, line2, amount);
        }

        //Command signs
        if (line1.equalsIgnoreCase("[command]")) {
            if (!this.plugin.ah.isAuthorized(p, "rcmds.sign.use.command")) {
                RUtils.dispNoPerms(p);
                return;
            }

            Double charge = getCharge(line3);
            if (charge == null) {
                p.sendMessage(MessageColor.NEGATIVE + "The cost is invalid!");
                s.setLine(0, MessageColor.NEGATIVE + line1);
                return;
            } else if (charge >= 0) {
                s.setLine(2, ChatColor.DARK_GREEN + line3);
                boolean did = RUtils.chargePlayer(p, charge);
                if (!did) return;
            }
            if (line2.isEmpty()) {
                s.setLine(0, MessageColor.NEGATIVE + line1);
                p.sendMessage(MessageColor.NEGATIVE + "No command specified!");
                return;
            }
            p.performCommand(line2.trim());
        }
    }

    @EventHandler()
    public void onSignChange(SignChangeEvent e) {
        if (e.isCancelled()) return;
        if (e.getPlayer() == null) return;
        Player p = e.getPlayer();
        String line1 = ChatColor.stripColor(e.getLine(0));
        String line2 = ChatColor.stripColor(e.getLine(1));
        String line3 = ChatColor.stripColor(e.getLine(2));
        String line4 = ChatColor.stripColor(e.getLine(3));

        //Warp signs
        if (line1.equalsIgnoreCase("[warp]")) {
            if (!this.plugin.ah.isAuthorized(p, "rcmds.sign.warp")) {
                RUtils.dispNoPerms(p);
                return;
            }
            if (line2.isEmpty()) {
                e.setLine(0, MessageColor.NEGATIVE + line1);
                p.sendMessage(MessageColor.NEGATIVE + "No warp specified.");
                return;
            }

            Double charge = getCharge(line3);
            if (charge == null) {
                p.sendMessage(MessageColor.NEGATIVE + "The cost is invalid!");
                e.setLine(0, MessageColor.NEGATIVE + line1);
                return;
            } else if (charge >= 0) {
                e.setLine(2, ChatColor.DARK_GREEN + line3);
            }

            Location warpLoc = CmdWarp.pWarp(p, line2.toLowerCase());
            if (warpLoc == null) {
                e.setLine(0, MessageColor.NEGATIVE + line1);
                return;
            }
            e.setLine(0, MessageColor.POSITIVE + line1);
            p.sendMessage(MessageColor.POSITIVE + "Warp sign created successfully.");
        }

        //Time signs
        if (line1.equalsIgnoreCase("[time]")) {
            if (!this.plugin.ah.isAuthorized(p, "rcmds.sign.time")) {
                RUtils.dispNoPerms(p);
                return;
            }
            if (line2.isEmpty() || (CmdTime.getValidTime(line2)) == null) {
                e.setLine(0, MessageColor.NEGATIVE + line1);
                return;
            }

            Double charge = getCharge(line3);
            if (charge == null) {
                p.sendMessage(MessageColor.NEGATIVE + "The cost is invalid!");
                e.setLine(0, MessageColor.NEGATIVE + line1);
                return;
            } else if (charge >= 0) {
                e.setLine(2, ChatColor.DARK_GREEN + line3);
            }

            e.setLine(0, MessageColor.POSITIVE + line1);
            p.sendMessage(MessageColor.POSITIVE + "Time sign created successfully!");
        }

        //Free signs
        if (line1.equalsIgnoreCase("[free]")) {
            if (!this.plugin.ah.isAuthorized(p, "rcmds.sign.free")) {
                RUtils.dispNoPerms(p);
                e.setLine(0, "");
                return;
            }

            Double charge = getCharge(line3);
            if (charge == null) {
                p.sendMessage(MessageColor.NEGATIVE + "The cost is invalid!");
                e.setLine(0, MessageColor.NEGATIVE + line1);
                return;
            } else if (charge >= 0) {
                e.setLine(1, ChatColor.DARK_GREEN + line3);
                if (!RUtils.chargePlayer(p, charge)) return;
            }
            ItemStack stack = RUtils.getItem(line2, null);
            if (stack == null) {
                e.setLine(1, MessageColor.NEGATIVE + line2);
                p.sendMessage(MessageColor.NEGATIVE + "That material is invalid!");
                return;
            } else e.setLine(1, line2.toLowerCase().replace("_", " "));
            e.setLine(0, MessageColor.POSITIVE + line1);
            p.sendMessage(MessageColor.POSITIVE + "Free sign created successfully!");
        }

        //Disposal signs
        if (line1.equalsIgnoreCase("[disposal]")) {
            if (!this.plugin.ah.isAuthorized(p, "rcmds.sign.disposal")) {
                RUtils.dispNoPerms(p);
                return;
            }

            Double charge = getCharge(line2);
            if (charge == null) {
                p.sendMessage(MessageColor.NEGATIVE + "The cost is invalid!");
                e.setLine(0, MessageColor.NEGATIVE + line1);
                return;
            } else if (charge >= 0) {
                e.setLine(1, ChatColor.DARK_GREEN + line2);
            }

            e.setLine(0, MessageColor.POSITIVE + line1);
            p.sendMessage(MessageColor.POSITIVE + "Disposal sign created successfully!");
        }

        //Heal signs
        if (line1.equalsIgnoreCase("[heal]")) {
            if (!this.plugin.ah.isAuthorized(p, "rcmds.sign.heal")) {
                RUtils.dispNoPerms(p);
                return;
            }

            Double charge = getCharge(line2);
            if (charge == null) {
                p.sendMessage(MessageColor.NEGATIVE + "The cost is invalid!");
                e.setLine(0, MessageColor.NEGATIVE + line1);
                return;
            } else if (charge >= 0) {
                e.setLine(1, ChatColor.DARK_GREEN + line2);
            }

            e.setLine(0, MessageColor.POSITIVE + line1);
            p.sendMessage(MessageColor.POSITIVE + "Heal sign created successfully!");
        }

        //Weather signs
        if (line1.equalsIgnoreCase("[weather]")) {
            if (!this.plugin.ah.isAuthorized(p, "rcmds.sign.weather")) {
                RUtils.dispNoPerms(p);
                return;
            }

            Double charge = getCharge(line3);
            if (charge == null) {
                p.sendMessage(MessageColor.NEGATIVE + "The cost is invalid!");
                e.setLine(0, MessageColor.NEGATIVE + line1);
                return;
            } else if (charge >= 0) {
                e.setLine(2, ChatColor.DARK_GREEN + line3);
            }

            boolean valid = CmdWeather.validWeather(line2.trim());
            if (!valid) {
                p.sendMessage(MessageColor.NEGATIVE + "Invalid weather condition!");
                e.setLine(0, MessageColor.NEGATIVE + line1);
                return;
            }
            e.setLine(0, MessageColor.POSITIVE + line1);
            p.sendMessage(MessageColor.POSITIVE + "Weather sign created successfully!");
        }

        //Give signs
        if (line1.equalsIgnoreCase("[give]")) {
            if (!this.plugin.ah.isAuthorized(p, "rcmds.sign.give")) {
                RUtils.dispNoPerms(p);
                return;
            }

            Double charge = getCharge(line4);
            if (charge == null) {
                p.sendMessage(MessageColor.NEGATIVE + "The cost is invalid!");
                e.setLine(0, MessageColor.NEGATIVE + line1);
                return;
            } else if (charge >= 0) {
                e.setLine(3, ChatColor.DARK_GREEN + line4);
            }

            boolean valid = CmdGive.validItem(line2.trim());
            if (!valid) {
                p.sendMessage(MessageColor.NEGATIVE + "That item is invalid!");
                e.setLine(0, MessageColor.NEGATIVE + line1);
                return;
            }
            try {
                Integer.parseInt(line3);
            } catch (Exception ex) {
                p.sendMessage(MessageColor.NEGATIVE + "The amount was not a valid number!");
                return;
            }
            e.setLine(0, MessageColor.POSITIVE + line1);
            p.sendMessage(MessageColor.POSITIVE + "Give sign created successfully!");
        }

        //Command signs
        if (line1.equalsIgnoreCase("[command]")) {
            if (!this.plugin.ah.isAuthorized(p, "rcmds.sign.command")) {
                RUtils.dispNoPerms(p);
                return;
            }

            Double charge = getCharge(line3);
            if (charge == null) {
                p.sendMessage(MessageColor.NEGATIVE + "The cost is invalid!");
                e.setLine(0, MessageColor.NEGATIVE + line1);
                return;
            } else if (charge >= 0) e.setLine(2, ChatColor.DARK_GREEN + line3);
            if (line2.isEmpty()) {
                e.setLine(0, MessageColor.NEGATIVE + line1);
                p.sendMessage(MessageColor.NEGATIVE + "No command specified!");
                return;
            }
            p.sendMessage(MessageColor.POSITIVE + "Command sign created successfully.");
            e.setLine(0, MessageColor.POSITIVE + line1);
        }
    }

}
