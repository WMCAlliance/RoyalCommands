/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.listeners;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.royaldev.royalcommands.AFKUtils;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;

import static org.royaldev.royalcommands.RUtils.nearEqual;

import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.configuration.Configuration;
import org.royaldev.royalcommands.configuration.PlayerConfiguration;
import org.royaldev.royalcommands.configuration.PlayerConfigurationManager;
import org.royaldev.royalcommands.rcommands.CmdMessageOfTheDay;
import org.royaldev.royalcommands.rcommands.CmdNameEntity;
import org.royaldev.royalcommands.rcommands.CmdSpawn;
import org.royaldev.royalcommands.rcommands.CmdTime;
import org.royaldev.royalcommands.shaded.mkremins.fanciful.FancyMessage;
import org.royaldev.royalcommands.wrappers.player.MemoryRPlayer;
import org.royaldev.royalcommands.wrappers.player.RPlayer;

public class PlayerListener implements Listener {

    private final RoyalCommands plugin;
    private final Logger log = Logger.getLogger("Minecraft");

    public PlayerListener(final RoyalCommands instance) {
        this.plugin = instance;
    }

    @EventHandler
    public void afk(PlayerMoveEvent e) {
        AFKUtils.setLastMove(e.getPlayer(), System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void afkWatch(PlayerMoveEvent event) {
        if (event.isCancelled()) return;
        if (!AFKUtils.isAfk(event.getPlayer())) return;
        Location to = event.getTo();
        Location from = event.getFrom();
        if (nearEqual(to.getX(), from.getX()) && nearEqual(to.getY(), from.getY()) && nearEqual(to.getZ(), from.getZ()))
            return;
        AFKUtils.unsetAfk(event.getPlayer());
        plugin.getServer().broadcastMessage(RUtils.colorize(RUtils.replaceVars(Config.returnFormat, event.getPlayer())));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void assign(PlayerInteractEvent event) {
        //if (event.isCancelled()) return;
        if (PlayerConfigurationManager.getConfiguration(event.getPlayer()).getBoolean("jailed")) {
            event.setCancelled(true);
        }
        Action act = event.getAction();
        if (act.equals(Action.PHYSICAL)) return;
        ItemStack id = event.getItem();
        if (id == null) return;
        final List<String> cmds = new ArrayList<>();
        final List<String> personalAssigns = RUtils.getAssignment(id, PlayerConfigurationManager.getConfiguration(event.getPlayer()));
        final List<String> publicAssigns = RUtils.getAssignment(id, Configuration.getConfiguration("publicassignments.yml"));
        if (personalAssigns != null) cmds.addAll(personalAssigns);
        if (publicAssigns != null) cmds.addAll(publicAssigns);
        if (cmds.isEmpty()) return;
        for (String s : cmds) {
            if (s.toLowerCase().trim().startsWith("c:")) event.getPlayer().chat(s.trim().substring(2));
            else {
                event.getPlayer().performCommand(s.trim());
                if (Config.showcommands) {
                    String[] parts = s.split(" ");
                    if (parts.length > 0) {
                        String command = parts[0].toLowerCase();
                        if (!Config.logBlacklist.contains(command.substring(1)))
                            log.info("[PLAYER_COMMAND] " + event.getPlayer().getName() + ": /" + s);
                    }
                }
            }
        }
    }

    @EventHandler
    public void commandCooldown(PlayerCommandPreprocessEvent e) {
        if (e.isCancelled()) return;
        String command = e.getMessage().split(" ")[0].toLowerCase().substring(1);
        if (plugin.getCommand(command) != null) command = plugin.getCommand(command).getName();
        Player p = e.getPlayer();
        if (plugin.ah.isAuthorized(p, "rcmds.exempt.cooldown.commands")) return;
        long currentcd = PlayerConfigurationManager.getConfiguration(p).getLong("command_cooldowns." + command, -1L);
        if (currentcd > 0L) {
            if (currentcd <= System.currentTimeMillis()) {
                setCooldown(command, p);
                return;
            }
            p.sendMessage(MessageColor.NEGATIVE + "You can't use that command for" + MessageColor.NEUTRAL + RUtils.formatDateDiff(currentcd) + MessageColor.NEGATIVE + ".");
            e.setCancelled(true);
            return;
        }
        setCooldown(command, p);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void commandSpy(PlayerCommandPreprocessEvent e) {
        if (plugin.ah.isAuthorized(e.getPlayer(), "rcmds.exempt.commandspy")) return;
        final PluginCommand pc = plugin.getServer().getPluginCommand(e.getMessage().split(" ")[0].substring(1));
        if (pc != null) {
            if (Config.commandSpyBlacklist.contains(pc.getName().toLowerCase())) return;
            for (String alias : pc.getAliases()) if (Config.commandSpyBlacklist.contains(alias.toLowerCase())) return;
        }
        for (Player p : e.getPlayer().getServer().getOnlinePlayers()) {
            if (!PlayerConfigurationManager.getConfiguration(p).getBoolean("commandspy", false)) continue;
            if (p.getName().equalsIgnoreCase(e.getPlayer().getName())) continue; // don't send to self
            new FancyMessage(e.getPlayer().getName()).color(MessageColor.NEUTRAL.cc()).formattedTooltip(RUtils.getPlayerTooltip(e.getPlayer())).then(": ").color(MessageColor.POSITIVE.cc()).then(e.getMessage()).color(MessageColor.NEUTRAL.cc()).command(e.getMessage()).tooltip("Click here to execute this command.").send(p);
        }
    }

    @EventHandler
    public void deafenMessages(AsyncPlayerChatEvent e) {
        if (e.isCancelled()) return;
        List<Player> toRemove = new ArrayList<>();
        for (Player t : e.getRecipients()) {
            PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(t);
            boolean isDeaf = pcm.getBoolean("deaf", false);
            if (!isDeaf) continue;
            if (e.getPlayer().getName().equals(t.getName())) continue; // don't remove own messages
            toRemove.add(t);
        }
        e.getRecipients().removeAll(toRemove); // remove deaf players
    }

    @EventHandler
    public void nicknames(final PlayerJoinEvent e) {
        final RPlayer rp = MemoryRPlayer.getRPlayer(e.getPlayer());
        rp.getNick().update();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void freezeWatch(PlayerMoveEvent e) {
        if (e.isCancelled()) return;
        if (PlayerConfigurationManager.getConfiguration(e.getPlayer()).getBoolean("frozen")) e.setTo(e.getFrom());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void heMan(AsyncPlayerChatEvent e) {
        if (e.isCancelled()) return;
        if (!e.getMessage().matches("(?i)by the power of gr[ae]yskull!?")) return;
        final Player p = e.getPlayer();
        if (!plugin.ah.isAuthorized(p, "rcmds.heman")) return;
        final ItemStack is = p.getInventory().getItemInMainHand();
        if (is.getType() != Material.DIAMOND_SWORD) return;
        if (is.getEnchantments().isEmpty()) return;
        e.setCancelled(true);
        p.getWorld().strikeLightningEffect(p.getLocation());
        final Matcher m = Pattern.compile("(?i)by the power of gr[ae]yskull!?").matcher(e.getMessage());
        final StringBuilder sb = new StringBuilder();
        int last = 0;
        while (m.find()) {
            sb.append(e.getMessage().substring(last, m.start()));
            sb.append(m.group(0).toUpperCase());
            last = m.end();
        }
        sb.append(e.getMessage().substring(last));
        this.plugin.getServer().broadcastMessage(e.getFormat().replaceAll("(?i)by the power of gr[ae]yskull!?", sb.toString()));
        e.setFormat("");
        final List<PotionEffect> effects = new ArrayList<>();
        effects.add(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 1200, 2));
        effects.add(new PotionEffect(PotionEffectType.REGENERATION, 1200, 2));
        effects.add(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 1200, 2));
        effects.add(new PotionEffect(PotionEffectType.SPEED, 1200, 2));
        p.addPotionEffects(effects);
    }

    @EventHandler(priority = EventPriority.HIGH) // run after others
    public void ipBans(PlayerLoginEvent e) {
        final String ip = e.getAddress().toString().replace("/", "");
        if (!RUtils.isIPBanned(ip)) return;
        final String message = Config.ipBanFormat.replace("{ip}", ip);
        e.disallow(Result.KICK_BANNED, RUtils.colorize(message));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void kickLogging(PlayerKickEvent e) {
        final Player p = e.getPlayer();
        final PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(p);
        final ConfigurationSection prevKicks = (pcm.isSet("kick_history")) ? pcm.getConfigurationSection("kick_history") : pcm.createSection("kick_history");
        final String section = String.valueOf(prevKicks.getKeys(false).size()) + ".";
        prevKicks.set(section + "kicker", pcm.getString("last_kick.kicker", "Unknown"));
        prevKicks.set(section + "reason", pcm.getString("last_kick.reason", e.getReason().replaceAll("\\r?\\n", " ")));
        prevKicks.set(section + "timestamp", pcm.isSet("last_kick.timestamp") ? pcm.getLong("last_kick.timestamp") : null);
        if (pcm.isSet("last_kick")) pcm.set("last_kick", null); // clear last_kick
    }

    @EventHandler
    public void lastPosition(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(p);
        Location l = p.getLocation();
        String lastPos = "lastposition.";
        pcm.set(lastPos + "x", l.getX());
        pcm.set(lastPos + "y", l.getY());
        pcm.set(lastPos + "z", l.getZ());
        pcm.set(lastPos + "pitch", l.getPitch());
        pcm.set(lastPos + "yaw", l.getYaw());
        pcm.set(lastPos + "world", l.getWorld().getName());
    }

    @EventHandler
    public void leRespawn(PlayerRespawnEvent e) {
        if (!Config.overrideRespawn) return;
        e.setRespawnLocation(CmdSpawn.getWorldSpawn(e.getPlayer().getWorld()));
    }

    @EventHandler
    public void onAssignHitPlayer(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        Player p = (Player) e.getDamager();
        onAssignInteractPlayer(new PlayerInteractEntityEvent(p, e.getEntity()));
    }

    @EventHandler
    public void onAssignInteractPlayer(PlayerInteractEntityEvent e) {
        //if (e.isCancelled()) return;
        if (PlayerConfigurationManager.getConfiguration(e.getPlayer()).getBoolean("jailed")) e.setCancelled(true);
        ItemStack id = e.getPlayer().getInventory().getItemInMainHand();
        if (id == null) return;
        final List<String> cmds = new ArrayList<>();
        final List<String> personalAssigns = RUtils.getAssignment(id, PlayerConfigurationManager.getConfiguration(e.getPlayer()));
        final List<String> publicAssigns = RUtils.getAssignment(id, Configuration.getConfiguration("publicassignments.yml"));
        if (personalAssigns != null) cmds.addAll(personalAssigns);
        if (publicAssigns != null) cmds.addAll(publicAssigns);
        if (cmds.isEmpty()) return;
        Player clicked = null;
        if (e.getRightClicked() instanceof Player) clicked = (Player) e.getRightClicked();
        for (String s : cmds) {
            if (clicked != null) s = s.replace("{player}", clicked.getName());
            if (s.toLowerCase().trim().startsWith("c:")) e.getPlayer().chat(s.trim().substring(2));
            else {
                e.getPlayer().performCommand(s.trim());
                if (Config.showcommands) {
                    String[] parts = s.split(" ");
                    if (parts.length > 0) {
                        String command = parts[0].toLowerCase();
                        if (!Config.logBlacklist.contains(command.substring(1)))
                            log.info("[PLAYER_COMMAND] " + e.getPlayer().getName() + ": /" + s);
                    }
                }
            }
        }
    }

    @EventHandler()
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(p);
        if (pcm.get("ignoredby") == null) return;
        Set<Player> recpts = e.getRecipients();
        ArrayList<String> ignores = (ArrayList<String>) pcm.getStringList("ignoredby");
        if (ignores == null) return;
        Set<Player> ignore = new HashSet<>();
        for (Player pl : recpts)
            for (String ignoree : ignores)
                if (pl.getName().equalsIgnoreCase(ignoree.toLowerCase())) ignore.add(pl);
        e.getRecipients().removeAll(ignore);
    }

    @EventHandler
    public void onKick(PlayerKickEvent e) {
        PlayerConfigurationManager.getConfiguration(e.getPlayer()).set("seen", System.currentTimeMillis());
        if (AFKUtils.isAfk(e.getPlayer())) AFKUtils.unsetAfk(e.getPlayer());
        if (AFKUtils.moveTimesContains(e.getPlayer())) AFKUtils.removeLastMove(e.getPlayer());
    }

    @EventHandler
    public void onPInt(PlayerInteractEvent event) {
        if (PlayerConfigurationManager.getConfiguration(event.getPlayer()).getBoolean("frozen")) {
            event.setCancelled(true);
        }
        if (Config.buildPerm && !plugin.ah.isAuthorized(event.getPlayer(), "rcmds.build")) event.setCancelled(true);
    }

    @EventHandler()
    public void onPJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (plugin.newVersion == null) return;
        if (!plugin.newVersion.contains(plugin.version) && !plugin.version.contains("-SNAPSHOT") && plugin.ah.isAuthorized(p, "rcmds.updates")) {
            String newV = plugin.newVersion.split("RoyalCommands")[1].trim().substring(1);
            p.sendMessage(MessageColor.POSITIVE + "RoyalCommands " + MessageColor.NEUTRAL + "v" + newV + MessageColor.POSITIVE + " is out! You are running " + MessageColor.NEUTRAL + "v" + plugin.version + MessageColor.POSITIVE + ".");
            p.sendMessage(MessageColor.POSITIVE + "Get the new version at " + ChatColor.DARK_AQUA + "http://dev.bukkit.org/server-mods/royalcommands" + MessageColor.POSITIVE + ".");
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;
        Player p = event.getPlayer();
        AFKUtils.setLastMove(p, System.currentTimeMillis());
        if (AFKUtils.isAfk(p)) {
            AFKUtils.unsetAfk(p);
            plugin.getServer().broadcastMessage(RUtils.colorize(RUtils.replaceVars(Config.returnFormat, p)));
        }
        PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(p);
        if (pcm.getBoolean("muted", false)) {
            if (pcm.get("mutetime") != null && !RUtils.isTimeStampValidAddTime(p, "mutetime", "mutedat")) {
                pcm.set("muted", false);
                return;
            }
            String howLong = "";
            if (pcm.get("mutetime") != null && pcm.get("mutedat") != null) {
                long mutedAt = pcm.getLong("mutedat");
                long muteTime = pcm.getLong("mutetime") * 1000L;
                howLong = " for " + MessageColor.NEUTRAL + RUtils.formatDateDiff(muteTime + mutedAt).substring(1) + MessageColor.NEGATIVE;
            }
            p.sendMessage(MessageColor.NEGATIVE + "You are muted and cannot speak" + howLong + ".");
            plugin.getLogger().info(p.getName() + " is muted but tried to say \"" + event.getMessage() + "\"");
            event.setFormat("");
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) return;
        Player p = event.getPlayer();
        PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(p);
        if (Config.showcommands) {
            String command = event.getMessage().split(" ")[0].toLowerCase();
            if (!Config.logBlacklist.contains(command.substring(1)))
                log.info("[PLAYER_COMMAND] " + p.getName() + ": " + event.getMessage());
        }
        if (pcm.getBoolean("muted")) {
            if (pcm.get("mutetime") != null && !RUtils.isTimeStampValidAddTime(p, "mutetime", "mutedat"))
                pcm.set("muted", false);
            for (String command : Config.muteCmds) {
                if (!(event.getMessage().toLowerCase().startsWith(command.toLowerCase() + " ") || event.getMessage().equalsIgnoreCase(command.toLowerCase())))
                    continue;
                p.sendMessage(MessageColor.NEGATIVE + "You are muted.");
                log.info("[RoyalCommands] " + p.getName() + " tried to use that command, but is muted.");
                event.setCancelled(true);
            }
        }

        if (pcm.getBoolean("jailed")) {
            p.sendMessage(MessageColor.NEGATIVE + "You are jailed.");
            log.info("[RoyalCommands] " + p.getName() + " tried to use that command, but is jailed.");
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer() == null) return;
        final PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(event.getPlayer());
        if (pcm.isFirstJoin()) {
            String dispname = event.getPlayer().getDisplayName();
            if (dispname == null || dispname.trim().equals("")) dispname = event.getPlayer().getName();
            pcm.set("name", event.getPlayer().getName());
            pcm.set("dispname", dispname);
            pcm.set("ip", event.getPlayer().getAddress().getAddress().toString().replace("/", ""));
            pcm.set("banreason", "");
            pcm.set("allow_tp", true);
            if (Config.stsNew)
                RUtils.silentTeleport(event.getPlayer(), CmdSpawn.getWorldSpawn(event.getPlayer().getWorld()));
            pcm.setFirstJoin(false);
        } else {
            log.info("[RoyalCommands] Updating the IP for " + event.getPlayer().getName() + ".");
            String playerip = event.getPlayer().getAddress().getAddress().toString();
            playerip = playerip.replace("/", "");
            pcm.set("ip", playerip);
        }
        if (Config.sendToSpawn) {
			final RPlayer rp = MemoryRPlayer.getRPlayer(event.getPlayer());
            if (Config.stsBack) {
				rp.getTeleporter().teleport(CmdSpawn.getWorldSpawn(event.getPlayer().getWorld()));
			} else {
				RUtils.silentTeleport(event.getPlayer(), CmdSpawn.getWorldSpawn(event.getPlayer().getWorld()));
			}
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (RUtils.isIPBanned(event.getAddress().toString().replace("/", ""))) return;
        // Define the player
        Player p = event.getPlayer();
        // If player is null, stop
        if (p == null) return;
        // Create new config manager for player
        PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(p);
        final boolean wasBanned = p.isBanned();
        // Check if player is banned
        final boolean isBanned = RUtils.isBanned(p);
        if (wasBanned && !isBanned) event.setResult(Result.ALLOWED); // end of tempban, most likely
        if (!isBanned) return;
        // Get the banreason from the player's userdata file
        String reason = pcm.getString("banreason"); // Returns string or null
        // Check if there was none, and if there wasn't, set it to default ban message.
        if (reason == null) reason = Config.banMessage;
        String kicker = pcm.getString("banner");
        if (kicker == null) kicker = "Unknown";
        String kickMessage;
        if (pcm.get("bantime") != null) { // if tempban
            kickMessage = RUtils.getMessage(Config.tempbanFormat, reason, kicker);
            long banTime = pcm.getLong("bantime");
            kickMessage = kickMessage.replace("{length}", RUtils.formatDateDiff(banTime).substring(1));
        } else kickMessage = RUtils.getMessage(Config.banFormat, reason, kicker);
        // Set the kick message to the ban reason
        event.setKickMessage(kickMessage);
        // Disallow the event
        event.disallow(Result.KICK_BANNED, kickMessage);
    }

    @EventHandler
    public void onPlayerBedEnterEvent(PlayerBedEnterEvent event) {
        if (event.isCancelled() || !Config.sleepNotifications) {
            return;
        }

		String myName = event.getPlayer().getDisplayName();
        List<String> sleepers = new ArrayList<>(List.of(myName));

        FancyMessage fm = new FancyMessage("Players asleep: ")
            .color(MessageColor.POSITIVE.cc());
        for (Player p : event.getPlayer().getWorld().getPlayers()) {
            if (p.isSleeping()) {
                sleepers.add(p.getDisplayName());
                if (event.getPlayer() != p) {
                    p.sendMessage(MessageColor.NEUTRAL + myName + MessageColor.POSITIVE + " is now asleep.");
                }
            }
            fm
                .then(String.valueOf(sleepers.size()))
                .color(MessageColor.NEUTRAL.cc())
                .tooltip(MessageColor.NEUTRAL + String.join("\n", sleepers))
                .then(" of ")
                .color(MessageColor.POSITIVE.cc())
                .then(String.valueOf(event.getPlayer().getWorld().getPlayers().size()))
                .color(MessageColor.NEUTRAL.cc())
                .then(".")
                .color(MessageColor.POSITIVE.cc());
            fm.send(p);
        }
        if (Config.sleepMajority) {
            double sleep_percent = Config.sleepMajorityPercent / 100D;
            double current_asleep = (double) sleepers.size() / event.getPlayer().getWorld().getPlayers().size();
            if (current_asleep >= sleep_percent && current_asleep != 1.0) {
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                log.info("[RoyalCommands] It's a new day as sleep majority was met");
                                World w = event.getPlayer().getWorld();
                                if (Config.smoothTime) CmdTime.smoothTimeChange(0L, w);
                                else w.setTime(0L);
                            }
                        },
                        5500
                );
            }
        }

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        PlayerConfigurationManager.getConfiguration(e.getPlayer()).set("seen", System.currentTimeMillis());
        if (AFKUtils.isAfk(e.getPlayer())) AFKUtils.unsetAfk(e.getPlayer());
        if (AFKUtils.moveTimesContains(e.getPlayer())) AFKUtils.removeLastMove(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onTeleport(PlayerTeleportEvent e) {
        if (e.isCancelled()) return;
        if (PlayerConfigurationManager.getConfiguration(e.getPlayer()).getBoolean("jailed")) {
            e.getPlayer().sendMessage(MessageColor.NEGATIVE + "You are jailed and may not teleport.");
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void renameEntity(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        if (!CmdNameEntity.isNaming(p)) return;
        Entity rightClicked = e.getRightClicked();
        if (rightClicked instanceof Player) return;
        if (!(rightClicked instanceof LivingEntity)) return;
        LivingEntity le = (LivingEntity) rightClicked;
        String newName = CmdNameEntity.getNamingName(p);
        if (newName == null) return;
        le.setCustomName(newName);
        le.setCustomNameVisible(!newName.isEmpty());
        CmdNameEntity.cancelNaming(p);
        if (newName.isEmpty())
            p.sendMessage(MessageColor.POSITIVE + "Successfully removed the name from that " + MessageColor.NEUTRAL + le.getType().name().toLowerCase().replace("_", " ") + MessageColor.POSITIVE + ".");
        else
            p.sendMessage(MessageColor.POSITIVE + "Successfully renamed that " + MessageColor.NEUTRAL + le.getType().name().toLowerCase().replace("_", " ") + MessageColor.POSITIVE + " to " + MessageColor.NEUTRAL + newName + MessageColor.POSITIVE + ".");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void saveData(PlayerQuitEvent e) {
        PlayerConfigurationManager.getConfiguration(e.getPlayer()).forceSave();
    }

    public void setCooldown(String command, OfflinePlayer p) {
        if (Config.commandCooldowns == null) return;
        boolean contains = Config.commandCooldowns.getKeys(false).contains(command);
        if (Config.cooldownAliases && plugin.getCommand(command) != null) {
            for (String alias : plugin.getCommand(command).getAliases()) {
                if (Config.commandCooldowns.getKeys(false).contains(alias)) {
                    contains = true;
                    break;
                }
            }
        }
        if (contains) {
            long cooldown = Config.commandCooldowns.getLong(command);
            PlayerConfigurationManager.getConfiguration(p).set("command_cooldowns." + command, System.currentTimeMillis() + (cooldown * 1000));
        }
    }

    public void setTeleCooldown(OfflinePlayer p) {
        double seconds = Config.globalTeleportCooldown;
        if (seconds <= 0) return;
        PlayerConfigurationManager.getConfiguration(p).set("teleport_cooldown", (seconds * 1000) + System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void showMotd(PlayerJoinEvent e) {
        if (!Config.motdLogin) return;
        CmdMessageOfTheDay.showMotd(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void silentKicks(PlayerKickEvent e) {
        if (e.isCancelled()) return;
        String reason = e.getReason();
        if (!reason.endsWith("\00-silent")) return;
        e.setLeaveMessage(null);
        e.setReason(reason.replace("\00-silent", ""));
    }

    @EventHandler(priority = EventPriority.LOW)
    public void teleCooldown(PlayerTeleportEvent e) {
        if (e.isCancelled()) return;
        Player p = e.getPlayer();
        if (plugin.ah.isAuthorized(p, "rcmds.exempt.cooldown.teleports")) return;
        long currentcd = PlayerConfigurationManager.getConfiguration(p).getLong("teleport_cooldown", -1L);
        if (currentcd > 0L) {
            if (currentcd <= System.currentTimeMillis()) {
                setTeleCooldown(p);
                return;
            }
            p.sendMessage(MessageColor.NEGATIVE + "You can't teleport for" + MessageColor.NEUTRAL + RUtils.formatDateDiff(currentcd) + MessageColor.NEGATIVE + ".");
            e.setCancelled(true);
            return;
        }
        setTeleCooldown(p);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void teleWarmup(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        Location to = e.getTo();
        Location from = e.getFrom();
        if (nearEqual(to.getX(), from.getX()) && nearEqual(to.getY(), from.getY()) && nearEqual(to.getZ(), from.getZ()))
            return;
        PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(p);
        long l = pcm.getLong("teleport_warmup", -1L);
        int toAdd = Config.teleportWarmup * 1000;
        l = l + toAdd;
        long c = System.currentTimeMillis();
        if (l > c) {
            p.sendMessage(MessageColor.NEGATIVE + "You moved! Teleport cancelled!");
            pcm.set("teleport_warmup", -1L);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void vipLogin(PlayerLoginEvent e) {
        if (e.getResult() != Result.KICK_FULL) return;
        Player p = e.getPlayer();
        PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(p);
        if (p.isBanned()) return;
        if (pcm.getBoolean("vip", false)) e.allow();
    }

    @EventHandler
    public void welcomeNewPlayers(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (Config.useWelcome && !p.hasPlayedBefore()) {
            String welcomemessage = Config.welcomeMessage;
            welcomemessage = welcomemessage.replace("{name}", p.getName());
            welcomemessage = welcomemessage.replace("{dispname}", p.getDisplayName());
            welcomemessage = welcomemessage.replace("{world}", p.getWorld().getName());
            plugin.getServer().broadcastMessage(welcomemessage);
        }
    }

    @EventHandler
    public void whitelist(PlayerLoginEvent e) {
        if (!Config.useWhitelist) return;
        if (!Config.whitelist.contains(e.getPlayer().getUniqueId().toString())) {
            e.disallow(Result.KICK_WHITELIST, Config.whitelistMessage);
        }
    }

    @EventHandler
    public void whitelistMessage(PlayerLoginEvent e) {
        if (!plugin.getServer().hasWhitelist()) return;
        if (e.getResult() != Result.KICK_WHITELIST) return;
        if (!e.getPlayer().isWhitelisted()) e.disallow(Result.KICK_WHITELIST, Config.whitelistMessage);
    }

    @EventHandler
    public void worldPerms(PlayerTeleportEvent e) {
        if (e.isCancelled()) return;
        if (!Config.worldAccessPerm) return;
        World from = e.getFrom().getWorld();
        World to = e.getTo().getWorld();
        if (from.equals(to)) return;
        Player p = e.getPlayer();
        if (plugin.ah.isAuthorized(p, "rcmds.worldaccess." + to.getName())) return;
        e.setTo(e.getFrom());
        p.sendMessage(MessageColor.NEGATIVE + "You do not have permission to access the world \"" + RUtils.getMVWorldName(to) + ".\"");
    }

}
