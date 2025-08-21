/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.royaldev.royalcommands.AFKUtils;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;

@ReflectCommand
public class CmdList extends TabCommand {

    private static RoyalCommands pluginInstance;

	public enum ListMode {
		PERMISSION_GROUP,
		WORLD
	};

    public CmdList(final RoyalCommands pluginInstance, final String name) {
        super(pluginInstance, name, true, new Short[]{});
        CmdList.pluginInstance = pluginInstance;
    }

    public static String formatPrepend(Player p) {
        String format = Config.whoFormat;
        try {
            if (!CmdList.pluginInstance.vh.usingVault()) throw new Exception();
            format = format.replaceAll("(?i)\\{prefix}", CmdList.pluginInstance.vh.getChat().getPlayerPrefix(p));
        } catch (Exception e) {
            String prefix = RUtils.getRChatPrefix(p);
            if (prefix != null) format = format.replace("{prefix}", prefix);
            else format = format.replace("{prefix}", "");
        }
        try {
            if (!CmdList.pluginInstance.vh.usingVault()) throw new Exception();
            format = format.replaceAll("(?i)\\{group}", CmdList.pluginInstance.vh.getPermission().getPrimaryGroup(p));
        } catch (Exception e) {
            format = format.replaceAll("(?i)\\{group}", "");
        }
        try {
            if (CmdList.pluginInstance.vh.usingVault())
                format = format.replaceAll("(?i)\\{suffix}", CmdList.pluginInstance.vh.getChat().getPlayerSuffix(p));
        } catch (Exception e) {
            String suffix = RUtils.getRChatSuffix(p);
            if (suffix != null) format = format.replace("{suffix}", suffix);
            else format = format.replace("{suffix}", "");
        }
        format = format.replaceAll("(?i)\\{name}", p.getName());
        format = format.replaceAll("(?i)\\{dispname}", p.getDisplayName());
        format = RUtils.colorize(format);
        return format;
    }

    public static String[] getGroupList(ListMode mode, CommandSender cs) {
        Map<String, List<String>> groups = new HashMap<>();
        final StringBuilder sb = new StringBuilder();
        for (final Player p : CmdList.pluginInstance.getServer().getOnlinePlayers()) {
            String group = "";
			if (mode == ListMode.PERMISSION_GROUP) {
				try {
					if (!CmdList.pluginInstance.vh.usingVault()) throw new Exception();
					group = CmdList.pluginInstance.vh.getPermission().getPrimaryGroup(p);
				} catch (Exception e) {
					group = "No Group";
				}
				if (group == null) group = "No Group";
			} else if (mode == ListMode.WORLD) {
				group = RUtils.getMVWorldName(p.getWorld());
			}
            List<String> inGroup = !group.isEmpty() && (groups.containsKey(group)) ? groups.get(group) : new ArrayList<String>();
            if (CmdList.pluginInstance.isVanished(p) && CmdList.pluginInstance.ah.isAuthorized(cs, "rcmds.seehidden"))
                inGroup.add(MessageColor.NEUTRAL + "[HIDDEN]" + MessageColor.RESET + formatPrepend(p));
            else if (!CmdList.pluginInstance.isVanished(p)) {
                if (AFKUtils.isAfk(p))
                    inGroup.add(Config.afkOnPlaceholder + MessageColor.RESET + formatPrepend(p));
                else inGroup.add(formatPrepend(p));
            }
            groups.put(group, inGroup);
        }
        List<String> toRet = new ArrayList<>();
        for (Entry<String, List<String>> entry : groups.entrySet()) {
            List<String> inGroup = entry.getValue();
            if (inGroup.size() < 1) continue;
			if (mode == ListMode.PERMISSION_GROUP) {
				sb.append(groupPrepend(entry.getKey()));
			} else {
				sb.append(entry.getKey());
			}
            sb.append(MessageColor.RESET);
            sb.append(": ");
            for (String name : inGroup) {
                sb.append(name);
                sb.append(MessageColor.RESET);
                sb.append(", ");
            }
            if (sb.length() < 2) {
                sb.setLength(0);
                continue;
            }
            toRet.add(sb.toString().substring(0, sb.length() - 2));
            sb.setLength(0);
        }
        for (String s : toRet) if (s == null) toRet.remove(null);
        return toRet.toArray(new String[toRet.size()]);
    }

    public static String getSimpleList(CommandSender cs) {
        StringBuilder sb = new StringBuilder();
        for (final Player p : CmdList.pluginInstance.getServer().getOnlinePlayers()) {
            if (CmdList.pluginInstance.isVanished(p) && CmdList.pluginInstance.ah.isAuthorized(cs, "rcmds.seehidden")) {
                sb.append(MessageColor.NEUTRAL);
                sb.append("[HIDDEN]");
                sb.append(MessageColor.RESET);
                sb.append(formatPrepend(p));
                sb.append(MessageColor.RESET);
                sb.append(", ");
            } else if (!CmdList.pluginInstance.isVanished(p)) {
                if (AFKUtils.isAfk(p)) sb.append(MessageColor.NEUTRAL).append("[AFK]");
                sb.append(formatPrepend(p));
                sb.append(MessageColor.RESET).append(", ");
            }
        }
        if (sb.length() < 2) return "";
        return MessageColor.NEUTRAL + "Online Players: " + MessageColor.RESET + sb.toString().substring(0, sb.length() - 2);
    }

    public static String groupPrepend(String group) {
        String format = Config.whoGroupFormat;
        try {
            if (!CmdList.pluginInstance.vh.usingVault()) throw new Exception();
            format = format.replaceAll("(?i)\\{prefix}", CmdList.pluginInstance.vh.getChat().getGroupPrefix(CmdList.pluginInstance.getServer().getWorlds().get(0), group));
        } catch (Exception e) {
            String prefix = RUtils.getRChatGroupPrefix(group);
            if (prefix != null) format = format.replace("{prefix}", prefix);
            else format = format.replace("{prefix}", "");
        }
        try {
            if (!CmdList.pluginInstance.vh.usingVault()) throw new Exception();
            format = format.replaceAll("(?i)\\{suffix}", CmdList.pluginInstance.vh.getChat().getGroupSuffix(CmdList.pluginInstance.getServer().getWorlds().get(0), group));
        } catch (Exception e) {
            String suffix = RUtils.getRChatGroupSuffix(group);
            if (suffix != null) format = format.replace("{suffix}", suffix);
            else format = format.replace("{suffix}", "");
        }
        format = format.replace("{group}", group);
        format = RUtils.colorize(format);
        return format;
    }

    public String getNumOnline(CommandSender cs) {
        int hid = this.plugin.getNumberVanished();
        int all = this.plugin.getServer().getOnlinePlayers().size();
        boolean canSeeVanished = this.ah.isAuthorized(cs, "rcmds.seehidden");
        String numPlayers;
        if (canSeeVanished && hid > 0) numPlayers = (all - hid) + "/" + hid;
        else numPlayers = String.valueOf(all - hid);
        return MessageColor.POSITIVE + "There are currently " + MessageColor.NEUTRAL + numPlayers + MessageColor.POSITIVE + " out of " + MessageColor.NEUTRAL + CmdList.pluginInstance.getServer().getMaxPlayers() + MessageColor.POSITIVE + " players online.";
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        cs.sendMessage(getNumOnline(cs));
		switch(Config.whoGroupMode) {
			case "simple" -> {
				String pList = getSimpleList(cs);
				if (pList.equals("")) return true;
				cs.sendMessage(pList);
			}
			case "group" -> cs.sendMessage(getGroupList(ListMode.PERMISSION_GROUP, cs));
			case "world" -> cs.sendMessage(getGroupList(ListMode.WORLD, cs));
		}
        return true;
    }
}
