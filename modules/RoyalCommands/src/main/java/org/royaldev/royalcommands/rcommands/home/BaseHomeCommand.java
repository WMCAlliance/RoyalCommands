/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands.home;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.royaldev.royalcommands.AuthorizationHandler.PermType;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.rcommands.TabCommand;
import org.royaldev.royalcommands.wrappers.player.MemoryRPlayer;
import org.royaldev.royalcommands.wrappers.player.RPlayer;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseHomeCommand extends TabCommand {

    private final boolean consoleAllowed;

    protected BaseHomeCommand(final RoyalCommands instance, final String name, final boolean checkPermissions, final Short[] cts, final boolean consoleAllowed) {
        super(instance, name, checkPermissions, cts);
        this.consoleAllowed = consoleAllowed;
    }

    protected abstract boolean runCommand(final CommandSender cs, final Player p, final Command cmd, final String label, final String[] eargs, final CommandArguments ca, final Home home);

    protected List<String> completeHomes(final RPlayer rp, final String name) {
        final String[] parts = name.split(":");
        final String playerName = parts.length > 1 ? parts[0] : null;
        if (playerName == null) return rp.getHomeNames();
        final List<Home> homes = MemoryRPlayer.getRPlayer(playerName).getHomes();
        final List<String> completeNames = new ArrayList<>();
        for (final Home h : homes) {
            completeNames.add(h.getName());
        }
        return completeNames;
    }

    @Override
    protected List<String> customList(final CommandSender cs, final Command cmd, final String label, final String[] args, final String arg) {
        if (!(cs instanceof Player)) return new ArrayList<>();
        final RPlayer rp = MemoryRPlayer.getRPlayer((Player) cs);
        return new ArrayList<>(this.completeHomes(rp, arg));
    }

    @Override
    protected boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] eargs, final CommandArguments ca) {
        final boolean isPlayer = cs instanceof Player;
        if (!isPlayer && !this.consoleAllowed) {
            cs.sendMessage(ChatColor.RED + "This command is only available to players.");
            return true;
        }
        final Player p = isPlayer ? (Player) cs : null;
        String homeName = eargs.length < 1 ? "home" : eargs[0];
        if (eargs.length > 1) {
            homeName = eargs[1] + ":" + eargs[0];
        }
        if (!isPlayer && !homeName.contains(":")) {
            cs.sendMessage(MessageColor.NEGATIVE + "You must specify a player.");
            return true;
        }
        final Home home = Home.fromNotation(p == null ? null : p.getUniqueId(), homeName);
        if (home == null) {
            cs.sendMessage(MessageColor.NEGATIVE + "The home " + MessageColor.NEUTRAL + homeName + MessageColor.NEGATIVE + " does not exist.");
            return true;
        }
        if (isPlayer && !home.getUUID().equals(p.getUniqueId()) && !this.ah.isAuthorized(cs, cmd, PermType.OTHERS)) {
            cs.sendMessage(MessageColor.NEGATIVE + "You are not allowed to use other players' homes.");
            return true;
        }
        return this.runCommand(cs, p, cmd, label, eargs, ca, home);
    }
}
