/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands.teleport;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.wrappers.player.MemoryRPlayer;
import org.royaldev.royalcommands.wrappers.player.RPlayer;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeleportRequest {

    // NOTE: Not using UUIDs, because this is so temporary that it should NOT matter. It will just add lag time.
    private static final Map<String, List<TeleportRequest>> teleportRequests = new HashMap<>();
    private final String requester;
    private final String target;
    private final TeleportType teleportType;
    private final long date;

    /**
     * Constructs a teleport request. This does not register it in the list of pending requests.
     *
     * @param requester    Requester's name
     * @param target       Target's name
     * @param teleportType Type of teleport
     * @param date         Time teleport was requested
     */
    public TeleportRequest(final String requester, final String target, final TeleportType teleportType, final long date) {
        this.requester = requester;
        this.target = target;
        this.teleportType = teleportType;
        this.date = date;
    }

    /**
     * Gets the first registered TeleportRequest with the given requester and target names.
     *
     * @param requester Requester's name
     * @param target    Target's name
     * @return First matching TeleportRequest or null
     */
    public static TeleportRequest getFirstRequest(final String requester, final String target) {
        final List<TeleportRequest> trs = TeleportRequest.getRequests().get(target);
        if (trs == null) return null;
        for (TeleportRequest tr : trs) {
            if (tr == null || !tr.getRequester().equalsIgnoreCase(requester) || !tr.getTarget().equalsIgnoreCase(target))
                continue;
            return tr;
        }
        return null;
    }

    /**
     * Gets the latest request for the given target name. If there are no requests, this will return null.
     *
     * @param target Target's name
     * @return Latest TeleportRequest or null
     */
    public static TeleportRequest getLatestRequest(final String target) {
        final List<TeleportRequest> trs = TeleportRequest.getRequests().get(target);
        if (trs == null) return null;
        long highest = -1L;
        TeleportRequest latest = null;
        for (TeleportRequest tr : trs) {
            if (tr == null || !tr.getTarget().equalsIgnoreCase(target)) continue;
            if (highest == -1L || tr.getDate() > highest) {
                highest = tr.getDate();
                latest = tr;
            }
        }
        return latest;
    }

    /**
     * Gets all teleport requests currently pending.
     *
     * @return Map&lt;Target Name, List&lt;TeleportRequest&gt;&gt;
     */
    public static Map<String, List<TeleportRequest>> getRequests() {
        return TeleportRequest.teleportRequests;
    }

    /**
     * Checks to see if a request with the given requester and target names exists, regardless of the request type.
     *
     * @param requester Requester's name
     * @param target    Target's name
     * @return true if requests exist, false if not
     */
    private static boolean hasPendingRequest(final String requester, final String target) {
        final List<TeleportRequest> trs = TeleportRequest.getRequests().get(target);
        if (trs == null) return false;
        for (TeleportRequest tr : trs) {
            if (tr == null || !tr.getRequester().equalsIgnoreCase(requester) || !tr.getTarget().equalsIgnoreCase(target))
                continue;
            return true;
        }
        return false;
    }

    /**
     * Sends a teleport request to the target from the requester.
     *
     * @param requester    Requester of the teleport
     * @param target       Target of the teleport
     * @param teleportType Type of teleport
     * @param confirmation If a confirmation of the request should be sent to the requester
     */
    public static void send(final Player requester, final Player target, final TeleportType teleportType, final boolean confirmation) {
        if (requester.getName().equalsIgnoreCase(target.getName())) {
            requester.sendMessage(MessageColor.NEGATIVE + "You cannot teleport to yourself.");
            return;
        }
        final TeleportRequest tr = new TeleportRequest(requester.getName(), target.getName(), teleportType, System.currentTimeMillis());
        List<TeleportRequest> trs;
        synchronized (TeleportRequest.teleportRequests) {
            trs = TeleportRequest.teleportRequests.get(target.getName());
        }
        if (trs == null) trs = new ArrayList<>();
        if (TeleportRequest.hasPendingRequest(requester.getName(), target.getName())) {
            requester.sendMessage(MessageColor.NEGATIVE + "You already have a request pending with " + MessageColor.NEUTRAL + target.getName() + MessageColor.NEGATIVE + ".");
            return;
        }
        trs.add(tr);
        synchronized (TeleportRequest.teleportRequests) {
            TeleportRequest.teleportRequests.put(target.getName(), trs);
        }
        target.sendMessage(teleportType.getMessage(requester));
        TextComponent tc = new TextComponent("To accept, use ");
        tc.setColor(MessageColor.POSITIVE.bc());

        TextComponent tcmd = new TextComponent("/tpaccept");
        tcmd.setColor(MessageColor.NEUTRAL.bc());
        tcmd.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept"));
        tcmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click here to execute this command.")));
        tc.addExtra(tcmd.duplicate());

        TextComponent tc2 = new TextComponent(". To decline, use ");
        tc2.setColor(MessageColor.POSITIVE.bc());
        tc.addExtra(tc2.duplicate());

        tcmd.setText("/tpdeny");
        tcmd.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny"));
        tc.addExtra(tcmd.duplicate());

        tc2.setText(".");
        tc.addExtra(tc2.duplicate());

        target.spigot().sendMessage(tc);

        if (confirmation)
            requester.sendMessage(MessageColor.POSITIVE + "Request sent to " + MessageColor.NEUTRAL + target.getName() + MessageColor.POSITIVE + ".");
    }

    /**
     * Sends a teleport request to the target from the requester.
     *
     * @param requester    Requester of the teleport
     * @param target       Target of the teleport
     * @param teleportType Type of teleport
     */
    public static void send(final Player requester, final Player target, final TeleportType teleportType) {
        TeleportRequest.send(requester, target, teleportType, true);
    }

    /**
     * Accepts this TeleportRequest and attempts the teleport. Also sends confirmation messages to both parties.
     */
    public void accept() {
        this.expire();
        final Player requester = Bukkit.getPlayerExact(this.getRequester());
        final Player target = Bukkit.getPlayerExact(this.getTarget());
        if (requester == null || target == null) return;
        final RPlayer reqRP = MemoryRPlayer.getRPlayer(requester);
        final RPlayer tarRP = MemoryRPlayer.getRPlayer(target);
        String error;
        switch (this.getType()) {
            case TO:
                error = reqRP.getTeleporter().teleport(target.getLocation());
                break;
            case HERE:
                error = tarRP.getTeleporter().teleport(requester.getLocation());
                break;
            default:
                return;
        }
        final boolean success = error.isEmpty();
        final String message = MessageColor.POSITIVE + "The request to teleport " + MessageColor.NEUTRAL + ((this.getType() == TeleportType.TO) ? requester.getDisplayName() : target.getDisplayName()) + MessageColor.POSITIVE + " to " + MessageColor.NEUTRAL + ((this.getType() == TeleportType.TO) ? target.getDisplayName() : requester.getDisplayName()) + MessageColor.POSITIVE + " was accepted.";
        if (success) {
            requester.sendMessage(message);
            target.sendMessage(message);
        } else {
            final String errorMessage = message.substring(0, message.length() - 1).replace(MessageColor.POSITIVE.toString(), MessageColor.NEGATIVE.toString()) + ", but there was an error.";
            requester.sendMessage(errorMessage);
            requester.sendMessage(MessageColor.NEGATIVE + error);
            target.sendMessage(errorMessage);
            target.sendMessage(MessageColor.NEGATIVE + error);
        }
    }

    /**
     * Denies the teleport request and {@link #expire()}s it. Also sends confirmation messages to both parties.
     */
    public void deny() {
        this.expire();
        final Player requester = Bukkit.getPlayerExact(this.getRequester());
        final Player target = Bukkit.getPlayerExact(this.getTarget());
        if (requester == null || target == null) return;
        final String message = MessageColor.POSITIVE + "The request to teleport " + MessageColor.NEUTRAL + ((this.getType() == TeleportType.TO) ? requester.getDisplayName() : target.getDisplayName()) + MessageColor.POSITIVE + " to " + MessageColor.NEUTRAL + ((this.getType() == TeleportType.TO) ? target.getDisplayName() : requester.getDisplayName()) + MessageColor.POSITIVE + " was denied.";
        requester.sendMessage(message.replace(MessageColor.POSITIVE.toString(), MessageColor.NEGATIVE.toString()));
        target.sendMessage(message);
    }

    /**
     * Causes this TeleportRequest to expire, removing it from the registered list of requests.
     */
    public void expire() {
        final List<TeleportRequest> trs = TeleportRequest.getRequests().get(this.getTarget());
        if (trs == null) return; // not registered, then
        trs.remove(this);
        TeleportRequest.getRequests().put(this.getRequester(), trs);
    }

    public long getDate() {
        return this.date;
    }

    public String getRequester() {
        return this.requester;
    }

    public String getTarget() {
        return this.target;
    }

    public TeleportType getType() {
        return this.teleportType;
    }

    public enum TeleportType {
        /**
         * Teleporting the requester to the target.
         */
        TO(MessageColor.NEUTRAL + "%s" + MessageColor.POSITIVE + " would like to teleport to you."),
        /**
         * Teleporting the target to the requester.
         */
        HERE(MessageColor.NEUTRAL + "%s" + MessageColor.POSITIVE + " would like you to teleport to them.");

        private final String requestMessage;

        TeleportType(final String requestMessage) {
            this.requestMessage = requestMessage;
        }

        /**
         * Formats the output of {@link #getRequestMessage()} with the given name.
         *
         * @param name Name to insert into the message
         * @return Formatted message
         */
        public String getMessage(final String name) {
            return String.format(this.requestMessage, name);
        }

        /**
         * Formats the output of {@link #getRequestMessage()} with the name of the given Player.
         *
         * @param p Player to get name from to insert into the message
         * @return Formatted message
         */
        public String getMessage(final Player p) {
            return this.getMessage(p.getDisplayName());
        }

        /**
         * Gets the message to send to the target. Must be formatted using {@link java.lang.String#format} to insert the
         * requester's name.
         *
         * @return Unformatted message
         */
        public String getRequestMessage() {
            return this.requestMessage;
        }
    }
}
