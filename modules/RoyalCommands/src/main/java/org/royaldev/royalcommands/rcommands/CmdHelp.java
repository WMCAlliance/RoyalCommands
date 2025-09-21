/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

@ReflectCommand
public class CmdHelp extends TabCommand {

    public CmdHelp(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.PLUGIN.getShort()});
    }

    private void displayCustomHelp(CommandSender cs, String page) {
        String sb = this.plugin.h.getCustomHelp();
        int pageNumber;
        try {
            pageNumber = Integer.parseInt(page);
        } catch (NumberFormatException e) {
            cs.sendMessage(MessageColor.NEGATIVE + "Page number was not numeric.");
            return;
        }
        int pages = StringUtils.countMatches(sb, "###");
        if (pageNumber > pages || pageNumber < 1) {
            cs.sendMessage(MessageColor.NEGATIVE + "Invalid page number!");
            return;
        }
        int beginIndex = StringUtils.ordinalIndexOf(sb, "###", pageNumber);
        int endIndex = StringUtils.ordinalIndexOf(sb, "###", pageNumber + 1);
        if (endIndex < 0 && beginIndex > -1) endIndex = sb.length() - 1; // eof
        if (beginIndex < 0) {
            cs.sendMessage(MessageColor.NEGATIVE + "Invalid help file!");
            return;
        }
        beginIndex += 4; // "###\n" = 4
        final String helpPage = sb.substring(beginIndex, endIndex);
        cs.sendMessage(MessageColor.POSITIVE + "Page " + MessageColor.NEUTRAL + pageNumber + MessageColor.POSITIVE + "/" + MessageColor.NEUTRAL + pages);
        for (String line : helpPage.split("\\n")) cs.sendMessage(RUtils.colorize(line));
    }

    private void displayPluginHelp(CommandSender cs, String pluginName, String page) {
        final Map<String, List<PluginCommand>> commands = this.plugin.h.getCommands();
        final Plugin p = getPlugin(pluginName);
        if (p == null) {
            cs.sendMessage(MessageColor.NEGATIVE + "No such plugin!");
            return;
        }
        final List<PluginCommand> pCommands = commands.get(pluginName.toLowerCase());
        if (pCommands == null) {
            cs.sendMessage(MessageColor.NEGATIVE + "No commands with help for that plugin!");
            return;
        }
        int pageNumber;
        try {
            pageNumber = Integer.parseInt(page);
        } catch (NumberFormatException e) {
            cs.sendMessage(MessageColor.NEGATIVE + "Page number was not numeric.");
            return;
        }
        int numPages = pCommands.size() / Config.helpAmount;
        if (pCommands.size() % Config.helpAmount != 0) numPages += 1;
        if (pageNumber > numPages || pageNumber < 1) {
            cs.sendMessage(MessageColor.NEGATIVE + "No such page!");
            return;
        }
        cs.sendMessage(MessageColor.POSITIVE + "Help for " + MessageColor.NEUTRAL + p.getName() + MessageColor.POSITIVE + " (Page " + MessageColor.NEUTRAL + pageNumber + MessageColor.POSITIVE + "/" + MessageColor.NEUTRAL + numPages + MessageColor.POSITIVE + ")");
        for (int i = (pageNumber - 1) * Config.helpAmount; i < (pageNumber * Config.helpAmount) + Config.helpAmount && i < pCommands.size(); i++) {
            final PluginCommand pc = pCommands.get(i);
            String cmdTxt = "/" + pc.getName();
            TextComponent tt = new TextComponent("  ");
            TextComponent cmd = new TextComponent(cmdTxt);
            cmd.setColor(MessageColor.NEUTRAL.bc());
            // cmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to ")));
            cmd.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, cmdTxt));
            tt.addExtra(cmd);
            TextComponent desc = new TextComponent(" - " + pc.getDescription());
            desc.setColor(MessageColor.POSITIVE.bc());
            // desc.setClickEvent(null);
            tt.addExtra(desc);
            cs.spigot().sendMessage(tt);
        }
    }

    private void displayPluginList(CommandSender cs) {
        final TreeMap<String, List<PluginCommand>> commands = this.plugin.h.getCommands();
        cs.sendMessage(MessageColor.POSITIVE + "There are " + MessageColor.NEUTRAL + commands.size() + MessageColor.POSITIVE + " plugins with help information:");
        for (Entry<String, ?> entry : commands.entrySet()) {
            final String pluginName = entry.getKey();
            final Plugin p = getPlugin(pluginName);
            if (p == null) continue;
            TextComponent tt = new TextComponent("  ");
            TextComponent pName = new TextComponent(p.getName());
            pName.setColor(MessageColor.NEUTRAL.bc());
            pName.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to display help")));
            pName.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/help " + p.getName()));
            tt.addExtra(pName);
            cs.spigot().sendMessage(tt);
        }
    }

    private Plugin getPlugin(String name) {
        for (Plugin lp : this.plugin.getServer().getPluginManager().getPlugins()) {
            if (!lp.getName().equalsIgnoreCase(name)) continue;
            return lp;
        }
        return null;
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] eargs, final CommandArguments ca) {
        if (Config.customHelp) displayCustomHelp(cs, (eargs.length > 0) ? eargs[0] : "1");
        else if (eargs.length < 1 && !Config.customHelp) this.displayPluginList(cs);
        else if (eargs.length > 0 && !Config.customHelp) {
            this.displayPluginHelp(cs, eargs[0], (eargs.length > 1) ? eargs[1] : "1");
        }
        return true;
    }
}
