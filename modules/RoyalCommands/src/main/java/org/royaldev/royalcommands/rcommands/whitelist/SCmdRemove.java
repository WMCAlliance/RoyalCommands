/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands.whitelist;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.rcommands.CmdWhitelist;
import org.royaldev.royalcommands.rcommands.SubCommand;
import org.royaldev.royalcommands.wrappers.player.RPlayer;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class SCmdRemove extends SubCommand<CmdWhitelist> {

    public SCmdRemove(final RoyalCommands instance, final CmdWhitelist parent) {
        super(instance, parent, "remove", true, "Removes a player from the whitelist.", "<command> (player)", new String[0], new Short[]{CompletionType.LIST.getShort()});
    }

    @Override
    protected List<String> customList(final CommandSender cs, final Command cmd, final String label, final String[] args, final String arg) {
        List<String> players = new ArrayList<>();
        for (String p : Config.whitelist) {
            final RPlayer rp = this.getParent().getRPlayer(p);
            players.add(rp.getName() != null ? rp.getName() : rp.getUUID().toString());
        }
        return players;
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] eargs, final CommandArguments ca) {
        if (this.plugin.whl == null) {
            cs.sendMessage(MessageColor.NEGATIVE + "The whitelist.yml file was invalid! Cannot use whitelist.");
            return true;
        }
        if (eargs.length < 1){
            return false;
        }
        final RPlayer rp = this.getParent().getRPlayer(RoyalCommands.getFinalArg(eargs, 0));
        final String uuid = rp.getUUID().toString();
        if (!Config.whitelist.contains(uuid)) {
            cs.sendMessage(MessageColor.NEGATIVE + "That player is not whitelisted!");
            return true;
        }
        Config.whitelist.remove(uuid);
        this.plugin.whl.set("whitelist", Config.whitelist);
        this.getParent().reloadWhitelist();

        TextComponent tc = new TextComponent("Removed ");
        tc.setColor(MessageColor.POSITIVE.bc());
        String name = rp.getName() != null ? rp.getName() : uuid;
        BaseComponent bc = new TextComponent(rp.getPlayer() != null ? rp.getPlayer().getDisplayName() : name);
        bc.setColor(MessageColor.NEUTRAL.bc());
        if (rp.getPlayer() != null) {
            bc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, RUtils.getPlayerTooltip(rp.getPlayer())));
        } else {
            bc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(rp.getUUID().toString())));
        }
        tc.addExtra(bc);

        tc.addExtra(TextComponent.fromLegacy(MessageColor.POSITIVE + " from the whitelist."));

        cs.spigot().sendMessage(tc);

        return true;
    }
}
