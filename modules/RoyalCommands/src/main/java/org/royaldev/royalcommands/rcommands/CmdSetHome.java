/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.rcommands.home.Home;
import org.royaldev.royalcommands.wrappers.player.MemoryRPlayer;
import org.royaldev.royalcommands.wrappers.player.RPlayer;

@ReflectCommand
public class CmdSetHome extends TabCommand {

    private final Map<UUID, Map<String, Long>> overwrites = new HashMap<>();

    public CmdSetHome(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.NONE.getShort(), CompletionType.ONLINE_PLAYER.getShort()});
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (args.length > 0 && !this.ah.isAuthorized(cs, "rcmds.sethome.multi")) {
            RUtils.dispNoPerms(cs, MessageColor.NEGATIVE + "You don't have permission for multiple homes!");
            return true;
        }
        if (!(cs instanceof Player)) {
            cs.sendMessage(MessageColor.NEGATIVE + "This command is only available to players!");
            return true;
        }
        final Player p = (Player) cs;
        final RPlayer rp = MemoryRPlayer.getRPlayer(p);
        final Location l = p.getLocation();
        String name = "";
        if (args.length > 0) name = args[0];
        if (name.isEmpty()) name = "home";
        if (args.length > 1) name = args[1] + ":" + args[0];
        Home h = Home.fromNotation(p.getUniqueId(), name);
        if (h != null) {
            boolean overwrite = false;
            Map<String, Long> hto = this.overwrites.get(p.getUniqueId());
            if (this.overwrites.containsKey(p.getUniqueId())) {
                if (hto.containsKey(h.getName()) && hto.get(h.getName()) != null) {
                    final long expiresAt = hto.get(h.getName()) + 10000;
                    overwrite = System.currentTimeMillis() < expiresAt;
                }
            }
            if (!overwrite) {
                cs.sendMessage(MessageColor.NEGATIVE + "Warning! " + MessageColor.POSITIVE + "The home " + MessageColor.NEUTRAL + h.getName() + MessageColor.POSITIVE + " is already set. To re-set it, do this command again in the next ten seconds.");
                if (hto == null) hto = new HashMap<>();
                hto.put(h.getName(), System.currentTimeMillis());
                this.overwrites.put(p.getUniqueId(), hto);
                return true;
            } else {
                hto.remove(h.getName());
                this.overwrites.put(p.getUniqueId(), hto);
            }
        } else {
            h = Home.createEmptyFromNotation(rp.getUUID(), name);
            if (!h.getUUID().equals(p.getUniqueId()) && !this.ah.isAuthorized(cs, "rcmds.others.sethome")) {
                RUtils.dispNoPerms(cs, new String[]{"rcmds.others.sethome"});
                return true;
            }
        }
        final RPlayer homeRPlayer = h.getRPlayer();
        final int limit = homeRPlayer.getHomeLimit();
        final int curHomes = homeRPlayer.getHomes().size();
        if (limit == 0) {
            RUtils.dispNoPerms(cs, MessageColor.NEGATIVE + "The home limit for " + MessageColor.NEUTRAL + homeRPlayer.getName() + MessageColor.POSITIVE + " is set to " + MessageColor.NEUTRAL + "0" + MessageColor.NEGATIVE + "!");
            cs.sendMessage(MessageColor.NEGATIVE + "Homes cannot be set!");
            return true;
        } else if (curHomes >= limit && limit > -1) {
            RUtils.dispNoPerms(cs, MessageColor.NEUTRAL + homeRPlayer.getName() + MessageColor.NEGATIVE + " has reached their maximum number of homes! (" + MessageColor.NEUTRAL + limit + MessageColor.NEGATIVE + ")");
            return true;
        }
        h.setLocation(l);
        h.save();
        if (args.length > 0) {
            p.sendMessage(MessageColor.POSITIVE + "Home " + MessageColor.NEUTRAL + h.getName() + MessageColor.POSITIVE + " set for " + MessageColor.NEUTRAL + h.getRPlayer().getName() + MessageColor.POSITIVE + ".");
        } else p.sendMessage(MessageColor.POSITIVE + "Home set.");
        return true;
    }
}
