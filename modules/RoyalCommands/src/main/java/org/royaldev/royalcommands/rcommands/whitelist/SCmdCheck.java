/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands.whitelist;

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

public class SCmdCheck extends SubCommand<CmdWhitelist> {

    public SCmdCheck(final RoyalCommands instance, final CmdWhitelist parent) {
        super(instance, parent, "check", true, "Checks if a player is in the whitelist.", "<command> (player)", new String[0], new Short[]{CompletionType.ONLINE_PLAYER.getShort()});
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
        Boolean inWhitelist = Config.whitelist.contains(uuid);
        String inWhitelistStr = inWhitelist ? "" : "not ";
        MessageColor inWhitelistColor = inWhitelist ? MessageColor.POSITIVE : MessageColor.NEGATIVE;

        TextComponent tc = new TextComponent("");
        BaseComponent bc = new TextComponent(rp.getPlayer() != null ? rp.getPlayer().getDisplayName() : rp.getName());
        bc.setColor(MessageColor.NEUTRAL.bc());
        if (rp.getPlayer() != null) {
            bc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, RUtils.getPlayerTooltip(rp.getPlayer())));
        } else {
            bc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(rp.getUUID().toString())));
        }
        tc.addExtra(bc);

        tc.addExtra(TextComponent.fromLegacy(" " + inWhitelistColor + "is " + inWhitelistStr + "in the whitelist."));

        cs.spigot().sendMessage(tc);
        return true;
    }
}
