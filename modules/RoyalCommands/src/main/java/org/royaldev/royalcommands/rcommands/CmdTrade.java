/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.configuration.PlayerConfiguration;
import org.royaldev.royalcommands.rcommands.trade.Trade;
import org.royaldev.royalcommands.wrappers.player.MemoryRPlayer;
import org.royaldev.royalcommands.wrappers.player.RPlayer;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

@ReflectCommand
public class CmdTrade extends TabCommand {

    public static final Map<UUID, UUID> tradedb = new HashMap<>();
    public static final Map<Map<UUID, UUID>, Inventory> trades = new HashMap<>();

    public CmdTrade(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.ONLINE_PLAYER.getShort()});
    }

    /**
     * Gets the trade inventory between two players. If no trade inventory has been made, this will return null.
     * <br>
     * Note that the order of the player arguments does not matter. It may be called as
     * <code>getTradeInv(playerA, playerB)</code> or <code>getTradeInv(playerB, playerA)</code>.
     *
     * @param p One player
     * @param t Other player
     * @return Inventory or null if no such inventory
     */
    public static Inventory getTradeInv(Player p, Player t) {
        synchronized (CmdTrade.trades) {
            for (final Map<UUID, UUID> set : CmdTrade.trades.keySet()) {
                if ((set.containsKey(t.getUniqueId()) && set.get(t.getUniqueId()).equals(p.getUniqueId())) || (set.containsKey(p.getUniqueId()) && set.get(p.getUniqueId()).equals(t.getUniqueId())))
                    return CmdTrade.trades.get(set);
            }
        }
        return null;
    }

    public static void sendTradeRequest(Player target, Player sender) {
        CmdTrade.tradedb.put(sender.getUniqueId(), target.getUniqueId());
        TextComponent tc = new TextComponent(sender.getName());
        tc.setColor(MessageColor.NEUTRAL.bc());
        tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, RUtils.getPlayerTooltip(sender)));

        TextComponent tc2 = new TextComponent(" has requested to trade with you.");
        tc2.setColor(MessageColor.POSITIVE.bc());
        tc.addExtra(tc2.duplicate());

        target.spigot().sendMessage(tc);

        tc2.setText("Type ");
        tc2.setColor(MessageColor.POSITIVE.bc());

        String tradeCmd = "/trade " + sender.getName();
        TextComponent tcCmd = new TextComponent(tradeCmd);
        tcCmd.setColor(MessageColor.NEUTRAL.bc());
        tcCmd.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, tradeCmd));
        tcCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to execute this command.")));
        tc2.addExtra(tcCmd);

        TextComponent tc3 = new TextComponent(" to accept.");
        tc3.setColor(MessageColor.POSITIVE.bc());
        tc2.addExtra(tc3);

        target.spigot().sendMessage(tc2);
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (!(cs instanceof Player)) {
            cs.sendMessage(MessageColor.NEGATIVE + "This command is only available to players!");
            return true;
        }
        if (args.length < 1) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        final Player p = (Player) cs;
        if (!Config.differentGamemodeTrade && p.getGameMode().equals(GameMode.CREATIVE)) {
            cs.sendMessage(MessageColor.NEGATIVE + "You can't trade in Creative mode!");
            return true;
        }
        final RPlayer otherRP = MemoryRPlayer.getRPlayer(args[0]);
        final PlayerConfiguration otherPC = otherRP.getPlayerConfiguration();
        if (!otherPC.exists()) {
            cs.sendMessage(MessageColor.NEGATIVE + "That player has never played before!");
            return true;
        }
        final RPlayer rp = MemoryRPlayer.getRPlayer(p);
        final UUID other = otherRP.getUUID();
        if (p.getUniqueId().equals(other)) {
            cs.sendMessage(MessageColor.NEGATIVE + "You can't trade with yourself!");
            return true;
        }
        Trade trade = Trade.getTradeFor(rp.getUUID(), other);
        if (trade == null) {
            trade = new Trade(rp.getUUID(), other);
        }
        trade.showInventoryGUI(rp.getUUID());
        return true;
    }
}
