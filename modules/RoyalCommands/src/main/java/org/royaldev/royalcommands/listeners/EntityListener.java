/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.listeners;

import org.bukkit.Location;
import org.bukkit.entity.EnderDragonPart;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.configuration.PlayerConfigurationManager;
import org.royaldev.royalcommands.rcommands.CmdBack;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

@SuppressWarnings("unused")
public class EntityListener implements Listener {

    public static RoyalCommands plugin;

    public EntityListener(RoyalCommands instance) {
        plugin = instance;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void buddhaMode(EntityDamageEvent e) {
        Entity ent = e.getEntity();
        if (!(ent instanceof Player)) return;
        Player p = (Player) ent;
        if (!PlayerConfigurationManager.getConfiguration(p).getBoolean("buddha")) return;
        e.setDamage(0);
        if (e.getDamage() >= p.getHealth()) p.setHealth(1);
        if (p.getHealth() == 1) e.setCancelled(true);
        if (p.getHealth() - e.getDamage() <= 1) {
            p.setHealth(1);
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void godMode(EntityDamageEvent e) {
        Entity ent = e.getEntity();
        if (!(ent instanceof Player)) return;
        Player p = (Player) ent;
        if (PlayerConfigurationManager.getConfiguration(p).getBoolean("godmode")) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityDeathEvent ent) {
        if (!(ent instanceof PlayerDeathEvent)) return;
        if (!Config.backDeath) return;
        PlayerDeathEvent e = (PlayerDeathEvent) ent;
        if (e.getEntity() == null) return;
        Player p = e.getEntity();
        Location pLoc = p.getLocation();
        CmdBack.addBackLocation(p, pLoc);
        if (Config.disabledBackWorlds.contains(pLoc.getWorld().getName())) return;
        if (plugin.ah.isAuthorized(p, "rcmds.back")) {
            TextComponent backC = new TextComponent("/back");
            backC.setColor(MessageColor.NEUTRAL.bc());
            Text tt = new Text(MessageColor.POSITIVE + "Click to teleport back");
            backC.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tt));
            backC.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/back"));
            BaseComponent[] bc = new ComponentBuilder("Type ")
                    .color(MessageColor.POSITIVE.bc())
                    .append(backC)
                    .append(" to go back to where you died.")
                    .color(MessageColor.POSITIVE.bc())
                    .event((HoverEvent) null)
                    .event((ClickEvent) null)
                    .create();
            p.spigot().sendMessage(bc);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityTarget(EntityTargetEvent event) {
        if (!(event.getTarget() instanceof Player)) return;
        Player p = (Player) event.getTarget();
        if (PlayerConfigurationManager.getConfiguration(p).getBoolean("mobignored")) event.setTarget(null);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player p = (Player) event.getEntity();
        if (!PlayerConfigurationManager.getConfiguration(p).getBoolean("godmode")) return;
        event.setFoodLevel(20);
        p.setSaturation(20F);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void oneHitKill(EntityDamageEvent event) {
        if (!(event instanceof EntityDamageByEntityEvent)) return;
        EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) event;
        Entity e = ev.getDamager();
        Entity ed = ev.getEntity();
        if (!(e instanceof Player)) return;
        Player p = (Player) e;
        if (!PlayerConfigurationManager.getConfiguration(p).getBoolean("ohk")) return;
        if (ed instanceof LivingEntity le) {
            le.damage(le.getHealth() * 1000);
            le.setLastDamageCause(new EntityDamageByEntityEvent(p, le, EntityDamageEvent.DamageCause.ENTITY_ATTACK, 1D));
        }
        if (ed instanceof EnderDragonPart ldp) {
            LivingEntity le = ldp.getParent();
            le.damage(le.getHealth() * 1000);
            le.setLastDamageCause(new EntityDamageByEntityEvent(p, le, EntityDamageEvent.DamageCause.ENTITY_ATTACK, 1D));
        }
    }

}
