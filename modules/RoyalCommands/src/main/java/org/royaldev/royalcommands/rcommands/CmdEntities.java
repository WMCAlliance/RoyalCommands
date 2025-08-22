/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;

@ReflectCommand
public class CmdEntities extends TabCommand {

    public CmdEntities(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{});
    }

    private Boolean sendNearbyEntities(Player p, double radius) {
        List<Entity> ents = p.getNearbyEntities(radius, radius, radius);

        ents.sort(Comparator.comparingDouble(entity -> p.getLocation().distanceSquared(entity.getLocation())));
        int amount = 0;
        p.sendMessage(MessageColor.POSITIVE + "Entities in a " + MessageColor.NEUTRAL + radius + MessageColor.POSITIVE + " block radius:");
        for (Entity e : ents) {
            if (e instanceof Player)
                continue;

            double dist = p.getLocation().distanceSquared(e.getLocation());

            DecimalFormat df = new DecimalFormat("#.#");
            BaseComponent bc = new TranslatableComponent(e.getType().getTranslationKey());
            bc.setColor(MessageColor.NEUTRAL.bc());
            bc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ENTITY, RUtils.getEntityTooltip(e)));
            TextComponent tc = new TextComponent(" ");
            tc.addExtra(bc);

            tc.addExtra(": ");
            tc.addExtra(TextComponent.fromLegacy(MessageColor.RESET + df.format(Math.sqrt(dist))));

            p.spigot().sendMessage(tc);
            amount++;
        }
        if (amount == 0) {
            p.sendMessage(MessageColor.NEGATIVE + " None nearby.");
            return true;
        }
        return true;
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (!(cs instanceof Player)) {
            cs.sendMessage(MessageColor.NEGATIVE + "This command is only available to players!");
            return true;
        }
        if (args.length < 1) {
            Player p = (Player) cs;
            double radius = Config.defaultNear;
            return this.sendNearbyEntities(p, radius);
        }
        if (args.length > 0) {
            Player p = (Player) cs;
            double radius;
            try {
                radius = Double.parseDouble(args[0]);
            } catch (Exception e) {
                cs.sendMessage(MessageColor.NEGATIVE + "That was not a valid number!");
                return true;
            }
            if (radius < 1) {
                cs.sendMessage(MessageColor.NEGATIVE + "That was not a valid number!");
                return true;
            }
            if (radius > Config.maxNear) {
                p.sendMessage(MessageColor.NEGATIVE + "That radius was too large!");
                return true;
            }
            return this.sendNearbyEntities(p, radius);
        }
        return true;
    }
}
