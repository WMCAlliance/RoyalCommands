package org.royaldev.royalcommands.death;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
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
        final String pullFrom;
        switch (this.getLastDamageCause()) {
            case BLOCK_EXPLOSION:
                pullFrom = "blo";
                break;
            case CONTACT:
                pullFrom = "con";
                break;
            case ENTITY_ATTACK:
            case PROJECTILE:
                pullFrom = (this.getDeathType() == DeathType.PLAYER) ? "pvp" : "mob";
                break;
            case SUFFOCATION:
                pullFrom = "suf";
                break;
            case FALL:
                pullFrom = "fal";
                break;
            case FALLING_BLOCK:
                pullFrom = "fab";
                break;
            case FIRE:
                pullFrom = "fir";
                break;
            case FIRE_TICK:
                pullFrom = "fir";
                break;
            case LAVA:
                pullFrom = "lav";
                break;
            case DROWNING:
                pullFrom = "dro";
                break;
            case ENTITY_EXPLOSION:
                pullFrom = "cre";
                break;
            case VOID:
                pullFrom = "voi";
                break;
            case LIGHTNING:
                pullFrom = "lig";
                break;
            case SUICIDE:
                pullFrom = "sui";
                break;
            case STARVATION:
                pullFrom = "sta";
                break;
            case POISON:
                pullFrom = "poi";
                break;
            case MAGIC:
                pullFrom = "mag";
                break;
            case WITHER:
                pullFrom = "wit";
                break;
            case THORNS:
                pullFrom = "tho";
                break;
            case DRAGON_BREATH:
                pullFrom = "dra";
                break;
            case FREEZE:
                pullFrom = "fro";
                break;
            case MELTING:
            case CUSTOM:
            default:
                pullFrom = "oth";
                break;
        }
        return pullFrom;
    }

    private RoyalCommands getPlugin() {
        return this.plugin;
    }

    private Random getRandom() {
        return this.random;
    }

    private String getRandomMessage() {
        //final List<String> messages = this.getPlugin().getConfig().getStringList(this.getMessageType());
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
                .replaceAll("(?i)\\{player}", RUtils.formatVariable(p.getName()))
                .replaceAll("(?i)\\{dispplayer}", RUtils.formatVariable(p.getDisplayName()))
                .replaceAll("(?i)\\{world}", RUtils.formatVariable(RUtils.getMVWorldName(p.getWorld())));

    }

    private String replaceVariables(String message) {
        final DeathType dt = DeathType.getDeathTypeByEvent(this.getLastEntityDamageEvent());
        if (dt != null) {
            message = dt.replaceVariables(message, this.getLastEntityDamageEvent());
        }
        return RUtils.getStringColor() +
            this.replacePlayerVariables(
                message
            );
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
