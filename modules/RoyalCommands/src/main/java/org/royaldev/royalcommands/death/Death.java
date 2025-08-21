package org.royaldev.royalcommands.death;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RoyalCommands;

import java.util.List;
import java.util.Random;

import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.configuration.Configuration;

public class Death {

    private final RoyalCommands plugin;
    private final Player killed;
    private final EntityDamageEvent lastEntityDamageEvent;
    private final DamageCause lastDamageCause;
    private final DeathType deathType;
    private final Random random = new Random();

    public Death(final RoyalCommands instance, final PlayerDeathEvent ped) {
        this(instance, ped.getEntity(), ped.getEntity().getLastDamageCause(), Death.getLastDamageCause(ped.getEntity()));
    }

    public Death(final RoyalCommands instance, final Player killed, final EntityDamageEvent event, final DamageCause lastDamageCause) {
        this.plugin = instance;
        this.killed = killed;
        this.lastEntityDamageEvent = event;
        this.lastDamageCause = lastDamageCause;
        this.deathType = DeathType.getDeathTypeByEvent(this.getLastEntityDamageEvent());
    }

    private static DamageCause getLastDamageCause(final Player p) {
        final EntityDamageEvent ede = p.getLastDamageCause();
        return ede == null ? null : ede.getCause();
    }

    private String getMessageType() {
        final String pullFrom = switch (this.getLastDamageCause()) {
            case BLOCK_EXPLOSION -> "blo";
            case CAMPFIRE -> "cam";
            case CONTACT -> "con";
            case CUSTOM, DRAGON_BREATH -> "dra";
            case DROWNING -> "dro";
            case ENTITY_ATTACK, ENTITY_SWEEP_ATTACK -> "mob";
            case ENTITY_EXPLOSION -> "cre";
            case FALL -> "fal";
            case FALLING_BLOCK -> "fab";
            case FIRE, FIRE_TICK -> "fir";
            case FLY_INTO_WALL -> "fiw";
            case FREEZE -> "fro";
            case HOT_FLOOR -> "hot";
            case LAVA -> "lav";
            case LIGHTNING -> "lig";
            case MAGIC -> "mag";
            case MELTING, POISON -> "poi";
            case PROJECTILE -> (this.getDeathType() == DeathType.PLAYER) ? "pvp" : "mob";
            case SONIC_BOOM -> "son";
            case STARVATION -> "sta";
            case SUFFOCATION -> "suf";
            case SUICIDE -> "sui";
            case THORNS -> "tho";
            case WITHER -> "wit";
            case VOID -> "voi";
            default -> "oth";
        };
        return pullFrom;
    }

    private RoyalCommands getPlugin() {
        return this.plugin;
    }

    private Random getRandom() {
        return this.random;
    }

    private String getRandomMessage() {
        final List<String> messages;
        Configuration cm = Configuration.getConfiguration("deathmessages.yml");
        if (!cm.exists()) return "";
        messages = cm.getStringList(this.getMessageType());
        if (messages == null || messages.isEmpty()) return "";
        return messages.get(this.getRandom().nextInt(messages.size()));
    }

    private String replacePlayerVariables(final String message) {
        if (this.getKilled() == null) return message;
        final Player p = this.getKilled();
            return message
                .replaceAll("(?i)\\{player}", MessageColor.NEGATIVE + p.getName() + MessageColor.NEUTRAL)
                .replaceAll("(?i)\\{dispname}", MessageColor.NEGATIVE + p.getDisplayName() + MessageColor.NEUTRAL)
                .replaceAll("(?i)\\{world}", MessageColor.NEGATIVE + RUtils.getMVWorldName(p.getWorld()) + MessageColor.NEUTRAL);

    }

    private String replaceVariables(String message) {
        final DeathType dt = DeathType.getDeathTypeByEvent(this.getLastEntityDamageEvent());
        if (dt != null) {
            message = dt.replaceVariables(message, this.getLastEntityDamageEvent());
        }
        return MessageColor.NEUTRAL + this.replacePlayerVariables(message) ;
    }

    public DeathType getDeathType() {
        return this.deathType;
    }

    public Player getKilled() {
        return this.killed;
    }

    public DamageCause getLastDamageCause() {
        return this.lastDamageCause;
    }

    public EntityDamageEvent getLastEntityDamageEvent() {
        return this.lastEntityDamageEvent;
    }

    public String getNewDeathMessage() {
        return this.replaceVariables(this.getRandomMessage());
    }
}
