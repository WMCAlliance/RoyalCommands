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
import org.royaldev.royalcommands.shaded.mkremins.fanciful.FancyMessage;

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
        if (plugin.ah.isAuthorized(p, "rcmds.back")) {
            FancyMessage fm = new FancyMessage("Type ")
                    .color(MessageColor.POSITIVE.cc())
                    .then("/back")
                    .color(MessageColor.NEUTRAL.cc())
                    .tooltip(MessageColor.POSITIVE + "Click to teleport back")
                    .command("/back")
                    .then(" to go back to where you died.")
                    .color(MessageColor.POSITIVE.cc());
            fm.send(p);
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
        if (ed instanceof LivingEntity) {
            LivingEntity le = (LivingEntity) ed;
            le.damage(le.getHealth() * 1000);
            le.setLastDamageCause(new EntityDamageByEntityEvent(p, le, EntityDamageEvent.DamageCause.ENTITY_ATTACK, 1D));
        }
        if (ed instanceof EnderDragonPart) {
            EnderDragonPart ldp = (EnderDragonPart) ed;
            LivingEntity le = ldp.getParent();
            le.damage(le.getHealth() * 1000);
            le.setLastDamageCause(new EntityDamageByEntityEvent(p, le, EntityDamageEvent.DamageCause.ENTITY_ATTACK, 1D));
        }
    }

}
