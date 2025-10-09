/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.configuration.PlayerConfiguration;
import org.royaldev.royalcommands.configuration.PlayerConfigurationManager;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

@ReflectCommand
public class CmdPlayerSearch extends TabCommand {

    public CmdPlayerSearch(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{});
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (args.length < 1) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        final String search = RoyalCommands.getFinalArg(args, 0);
        final OfflinePlayer[] ops = this.plugin.getServer().getOfflinePlayers();
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                int found = 0;
                cs.sendMessage(MessageColor.POSITIVE + "Search for " + MessageColor.NEUTRAL + search + MessageColor.POSITIVE + " started. This may take a while.");
                for (final OfflinePlayer op : ops) {
                    if (op == null || op.getName() == null) continue;
                    if (!op.getName().toLowerCase().contains(search.toLowerCase())) continue;
                    PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(op);
                    if (!pcm.exists()) continue;
                    long seen = pcm.getLong("seen");
                    if (seen < 1L) continue;
                    found++;
                    String opName = op.getName();
                    String lastseen = (op.isOnline()) ? " now"
                            : RUtils.formatDateDiff(seen) + MessageColor.POSITIVE + " ago";

                    TextComponent tc = new TextComponent();

                    TextComponent neutral = new TextComponent(String.valueOf(found));
                    neutral.setColor(MessageColor.NEUTRAL.bc());
                    tc.addExtra(neutral.duplicate());

                    TextComponent positive = new TextComponent(". ");
                    positive.setColor(MessageColor.POSITIVE.bc());
                    tc.addExtra(positive.duplicate());

                    neutral.setText(opName);
                    neutral.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, RUtils.getPlayerTooltip(op)));
                    neutral.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/whois " + opName));
                    tc.addExtra(neutral.duplicate());

                    positive.setText(" - Last seen ");
                    tc.addExtra(positive.duplicate());

                    tc.addExtra(TextComponent.fromLegacy(MessageColor.NEUTRAL + lastseen));

                    positive.setText(".");
                    tc.addExtra(positive.duplicate());

                    cs.spigot().sendMessage(tc);
                }
                BaseComponent[] bcR = new ComponentBuilder("Search completed. ")
                        .color(MessageColor.POSITIVE.bc())
                        .append(String.valueOf(found))
                        .color(MessageColor.NEUTRAL.bc())
                        .append(" results found.")
                        .color(MessageColor.POSITIVE.bc())
                        .create();
                cs.spigot().sendMessage(bcR);
            }
        };
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, r);
        return true;
    }
}
