package org.royaldev.royalcommands.death;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import org.royaldev.royalcommands.RUtils;

public enum DeathType {

    ENTITY(EntityDamageByEntityEvent.class) {
        @Override
        String replaceVariables(final String message, final EntityDamageEvent damageEvent) {
            final EntityDamageByEntityEvent edbee = (EntityDamageByEntityEvent) damageEvent;
            return message.replaceAll("(?i)\\{(mob|entity)}", RUtils.formatVariable(edbee.getDamager().getType().name().toLowerCase().replace('_', ' ')));
        }
    },
    PLAYER(EntityDamageByEntityEvent.class) {
        @Override
        String replaceVariables(final String message, final EntityDamageEvent damageEvent) {
            final EntityDamageByEntityEvent edbee = (EntityDamageByEntityEvent) damageEvent;
            final Player p = (Player) edbee.getDamager();
            return message
                .replaceAll("(?i)\\{hand}", RUtils.formatVariable(RUtils.getItemStackName(p.getInventory().getItemInMainHand())))
                .replaceAll("(?i)\\{killer}", RUtils.formatVariable(p.getName()))
                .replaceAll("(?i)\\{dispkiller}", RUtils.formatVariable(p.getDisplayName()));
        }
    },
    BLOCK(EntityDamageByBlockEvent.class) {
        @Override
        String replaceVariables(final String message, final EntityDamageEvent damageEvent) {
            return message;
        }
    },
    GENERIC(EntityDamageEvent.class) {
        @Override
        String replaceVariables(final String message, final EntityDamageEvent damageEvent) {
            return message;
        }
    };

    private final Class<? extends EntityDamageEvent> damageClass;

    DeathType(final Class<? extends EntityDamageEvent> clazz) {
        this.damageClass = clazz;
    }

    public static DeathType getDeathTypeByEvent(final EntityDamageEvent ede) {
        if (ede == null) return null;
        if (ede.getClass() == BLOCK.getDamageClass()) return BLOCK;
        if (!(ede instanceof final EntityDamageByEntityEvent edbee)) return GENERIC;
        final Entity damager = edbee.getDamager();
        if (damager instanceof Player) return PLAYER;
        else return ENTITY;
    }

    abstract String replaceVariables(final String message, final EntityDamageEvent damageEvent);

    Class<? extends EntityDamageEvent> getDamageClass() {
        return this.damageClass;
    }
}
