/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands;

import com.google.common.base.Charsets;
import com.griefcraft.lwc.LWCPlugin;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.onarandombox.MultiverseCore.MultiverseCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.Contract;
import org.kitteh.vanish.VanishPlugin;
import org.royaldev.royalcommands.api.RApiMain;
import org.royaldev.royalcommands.configuration.Configuration;
import org.royaldev.royalcommands.configuration.PlayerConfiguration;
import org.royaldev.royalcommands.configuration.PlayerConfigurationManager;
import org.royaldev.royalcommands.gui.inventory.listeners.ClickListener;
import org.royaldev.royalcommands.gui.inventory.listeners.InventoryGUIEventListener;
import org.royaldev.royalcommands.listeners.BackpackListener;
import org.royaldev.royalcommands.listeners.BlockListener;
import org.royaldev.royalcommands.listeners.EntityListener;
import org.royaldev.royalcommands.listeners.MonitorListener;
import org.royaldev.royalcommands.listeners.PlayerListener;
import org.royaldev.royalcommands.listeners.ServerListener;
import org.royaldev.royalcommands.listeners.SignListener;
import org.royaldev.royalcommands.nms.api.NMSFace;
import org.royaldev.royalcommands.protocol.ProtocolListener;
import org.royaldev.royalcommands.rcommands.BaseCommand;
import org.royaldev.royalcommands.rcommands.ReflectCommand;
import org.royaldev.royalcommands.rcommands.trade.TradeListener;
import org.royaldev.royalcommands.runners.AFKWatcher;
import org.royaldev.royalcommands.runners.FreezeWatcher;
import org.royaldev.royalcommands.runners.MailRunner;
import org.royaldev.royalcommands.runners.UserdataRunner;
import org.royaldev.royalcommands.runners.WarnWatcher;
import org.royaldev.royalcommands.shaded.com.sk89q.util.config.FancyConfiguration;
import org.royaldev.royalcommands.spawninfo.ItemListener;
import org.royaldev.royalcommands.tools.UUIDFetcher;

// import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

// TODO: Add banning for no-UUID players? Wait for Bukkit to fix? Investigate.
// TODO: Rewrite /gm
// TODO: Add config option for dangerous async (some people like living on the edge)
// TODO: Add config for getting info about player for tooltip (use vault for group, etc) "{{group}}\n{{name}}" etc

public class RoyalCommands extends JavaPlugin {

    public static ConfigurationSection commands = null;
    public static File dataFolder;
    public static ItemNameManager inm;
    public static WorldManager wm = null;
    public static MultiverseCore mvc = null;
    private static RoyalCommands instance;
    public final AuthorizationHandler ah = new AuthorizationHandler(this);
    public final VaultHandler vh = new VaultHandler(this);
    private final int minVersion = 1180; // 1.18.0

    private final Pattern versionPattern = Pattern.compile("((\\d+\\.?){3})(\\-SNAPSHOT)?(\\-local\\-(\\d{8}\\.\\d{6})|\\-(\\d+))?");
    private final long startTime = System.currentTimeMillis();
    public Configuration whl;
    public String version = null;
    public String newVersion = null;
    public Metrics m = null;
    public Config c;
    public Help h;
    private NMSFace nmsFace;
    private CommandMap cm = null;
    private YamlConfiguration pluginYml = null;
    private RApiMain api;
    private VanishPlugin vp = null;
    // private WorldGuardPlugin wg = null;
    private LWCPlugin lwc = null;
    private ProtocolListener pl = null;

    /**
     * Joins an array of strings with spaces
     *
     * @param array    Array to join
     * @param position Position to start joining from
     * @return Joined string
     */
    @Contract("null, _ -> null; !null, _ -> !null")
    public static String getFinalArg(final String[] array, final int position) {
        if (array == null) return null;
        final StringBuilder sb = new StringBuilder();
        for (int i = position; i < array.length; i++) sb.append(array[i]).append(" ");
        return sb.substring(0, sb.length() - 1);
    }

    public static RoyalCommands getInstance() {
        return RoyalCommands.instance;
    }

    private List<String> getAliases(String command) {
        final List<String> aliasesList = this.getCommandInfo(command).getStringList("aliases");
        if (aliasesList == null) return new ArrayList<>();
        return aliasesList;
    }

    private ConfigurationSection getCommandInfo(String command) {
        final ConfigurationSection ci = this.getCommands().getConfigurationSection(command);
        if (ci == null) throw new IllegalArgumentException("No such command registered!");
        return ci;
    }

    private CommandMap getCommandMap() {
        if (this.cm != null) return this.cm;
        Field map;
        try {
            map = this.getServer().getPluginManager().getClass().getDeclaredField("commandMap");
            map.setAccessible(true);
            this.cm = (CommandMap) map.get(this.getServer().getPluginManager());
            return this.cm;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private ConfigurationSection getCommands() {
        return this.pluginYml.getConfigurationSection("reflectcommands");
    }

    private String getDescription(String command) {
        return this.getCommandInfo(command).getString("description", command);
    }

    private VUUpdater.VUUpdateInfo getNewestVersions() throws Exception {
        return VUUpdater.getUpdateInfo("34507");
    }

    private String getUsage(String command) {
        return this.getCommandInfo(command).getString("usage", "/<command>");
    }

    private void initializeConfManagers() {
        final String[] cms = new String[]{"whitelist.yml", "warps.yml", "publicassignments.yml"};
        for (final String name : cms) {
            final Configuration cm = Configuration.getConfiguration(name);
            if (!cm.exists()) cm.createFile();
            cm.forceSave();
        }
    }

    private void initializeMetrics() {
        try {
            this.m = new Metrics(this, 15550);
            if (YamlConfiguration.loadConfiguration(new File(new File(this.getDataFolder().getParentFile(), "bStats"), "config.yml")).getBoolean("enabled", true)) {
                this.getLogger().info("Metrics enabled. Thank you!");
            } else {
                this.getLogger().info("You have Metrics off, no hard feelings :(");
            }
        } catch (Exception ignore) {
            this.getLogger().warning("Could not start Metrics!");
        }
    }


    private void initializeNMS() {
        // Get full package string of CraftServer.
        // org.bukkit.craftbukkit.versionstring (or for pre-refactor, just org.bukkit.craftbukkit
        final String packageName = getServer().getClass().getPackage().getName();
        // Get the last element of the package
        // If the last element of the package was "craftbukkit" we are now pre-refactor
        String versionNMS = packageName.substring(packageName.lastIndexOf('.') + 1);
        if ("craftbukkit".equals(versionNMS)) versionNMS = "NoSupport";
        try {
            // Check if we have a NMSHandler class at that location.
            final Class<?> clazz = Class.forName("org.royaldev.royalcommands.nms." + versionNMS + ".NMSHandler");
            // Make sure it actually implements NMS and set our handler
            if (NMSFace.class.isAssignableFrom(clazz)) this.nmsFace = (NMSFace) clazz.getConstructor().newInstance();
        } catch (final Exception e) {
            this.getLogger().log(Level.WARNING, "Could not find support for this CraftBukkit version ({0}).", versionNMS);
            this.getLogger().info("The BukkitDev page has links to the newest development builds to fix this.");
            this.getLogger().info("For now, NMS/CB internal support will be disabled.");
            this.nmsFace = new org.royaldev.royalcommands.nms.NoSupport.NMSHandler();
        }
        if (this.nmsFace.hasSupport()) getLogger().info("Loaded support for " + this.nmsFace.getVersion() + ".");
    }

    private void initializeTasks() {
        final BukkitScheduler bs = this.getServer().getScheduler();
        bs.runTaskTimerAsynchronously(this, new Runnable() {
            @Override
            public void run() {
                if (!Config.updateCheck) return;
                try {
                    Matcher m = versionPattern.matcher(version);
                    if (!m.matches()) return;
                    final StringBuilder useVersion = new StringBuilder();
                    if (m.group(1) != null) useVersion.append(m.group(1)); // add base version #
                    if (m.group(3) != null) useVersion.append(m.group(3)); // add SNAPSHOT status
                    /*
                    This does not need to compare build numbers. Everyone would be out of date all the time if it did.
                    This method will compare root versions.
                     */
                    final VUUpdater.VUUpdateInfo vuui = RoyalCommands.this.getNewestVersions();
                    final String stable = vuui.getStable();
                    final String dev = vuui.getDevelopment() + "-SNAPSHOT";
                    String currentVersion = useVersion.toString();
                    if (!dev.equalsIgnoreCase(currentVersion) && currentVersion.contains("-SNAPSHOT")) {
                        RoyalCommands.this.getLogger().warning("A newer version of RoyalCommands is available!");
                        RoyalCommands.this.getLogger().warning("Currently installed: v" + currentVersion + ", newest: v" + dev);
                        RoyalCommands.this.getLogger().warning("Development builds are available at https://jenkins.blny.me/");
                    } else if (!stable.equalsIgnoreCase(currentVersion) && !currentVersion.equalsIgnoreCase(dev)) {
                        RoyalCommands.this.getLogger().warning("A newer version of RoyalCommands is available!");
                        RoyalCommands.this.getLogger().warning("Currently installed: v" + currentVersion + ", newest: v" + stable);
                        RoyalCommands.this.getLogger().warning("Stable builds are available at https://www.spigotmc.org/resources/royalcommands.4113/");
                    } else if (!stable.equalsIgnoreCase(currentVersion) && currentVersion.replace("-SNAPSHOT", "").equalsIgnoreCase(stable)) {
                        RoyalCommands.this.getLogger().warning("A newer version of RoyalCommands is available!");
                        RoyalCommands.this.getLogger().warning("Currently installed: v" + currentVersion + ", newest: v" + stable);
                        RoyalCommands.this.getLogger().warning("Stable builds are available at https://www.spigotmc.org/resources/royalcommands.4113/");
                    }
                } catch (Exception ignored) {}
            }
        }, 0L, 36000L);
        bs.scheduleSyncDelayedTask(this, new Runnable() { // load after server starts up
            @Override
            public void run() {
                h.reloadHelp();
                RoyalCommands.this.getLogger().info("Help loaded for all plugins.");
            }
        });
        bs.runTaskTimerAsynchronously(this, new AFKWatcher(this), 0L, 200L);
        bs.runTaskTimerAsynchronously(this, new WarnWatcher(this), 20L, 12000L);
        bs.scheduleSyncRepeatingTask(this, new FreezeWatcher(this), 20L, 100L);
        long mail = RUtils.timeFormatToSeconds(Config.mailCheckTime);
        if (mail > 0L) bs.scheduleSyncRepeatingTask(this, new MailRunner(this), 20L, mail * 20L);

        long every = RUtils.timeFormatToSeconds(Config.saveInterval);
        if (every < 1L) every = 600L; // 600s = 10m
        bs.runTaskTimerAsynchronously(this, new UserdataRunner(this), 20L, every * 20L); // tick = 1/20s
    }

    private <T> List<List<T>> partitionList(List<T> original, int maxSize) {
        final List<List<T>> partitions = new LinkedList<>();
        for (int i = 0; i < original.size(); i += maxSize)
            partitions.add(original.subList(i, i + Math.min(maxSize, original.size() - i)));
        return partitions;
    }

    /**
     * Registers a command in the server's CommandMap.
     *
     * @param ce      CommandExecutor to be registered
     * @param command Command name as specified in plugin.yml
     */
    private void registerCommand(CommandExecutor ce, String command) {
        if (Config.disabledCommands.contains(command.toLowerCase())) return;
        try {
            final Constructor c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            c.setAccessible(true);
            final PluginCommand pc = (PluginCommand) c.newInstance(command, this);
            pc.setExecutor(ce);
            pc.setAliases(this.getAliases(command));
            pc.setDescription(this.getDescription(command));
            pc.setUsage(this.getUsage(command));
            this.getCommandMap().register(this.getDescription().getName(), pc);
        } catch (Exception e) {
            this.getLogger().warning("Could not register command \"" + command + "\" - an error occurred: " + e.getMessage() + ".");
        }
    }

    private void update() {
        final File userdataFolder = new File(dataFolder, "userdata");
        if (!userdataFolder.exists() || !userdataFolder.isDirectory()) return;
        final List<String> playersToConvert = new ArrayList<>();
        final List<String> playersConverted = new ArrayList<>();
        for (String fileName : userdataFolder.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".yml");
            }
        })) {
            String playerName = fileName.substring(0, fileName.length() - 4); // ".yml" = 4
            try {
                //noinspection ResultOfMethodCallIgnored
                UUID.fromString(playerName);
                continue;
            } catch (IllegalArgumentException ignored) {}
            playersToConvert.add(playerName);
        }
        final List<List<String>> partitions = this.partitionList(playersToConvert, 100);
        this.getLogger().info("Converting " + playersToConvert.size() + " players in " + partitions.size() + " request" + (partitions.size() == 1 ? "" : "s") + ".");
        for (List<String> lookup : partitions) {
            this.getLogger().info("Converting next " + lookup.size() + " players.");
            final Map<String, UUID> uuids;
            try {
                uuids = new UUIDFetcher(lookup).call();
            } catch (Exception ex) {
                ex.printStackTrace();
                continue;
            }
            for (Map.Entry<String, UUID> e : uuids.entrySet()) {
                File userFile = new File(userdataFolder, e.getKey().toLowerCase() + ".yml");
                if (!userFile.exists()) continue;
                try {
                    Files.move(userFile.toPath(), new File(userdataFolder, e.getValue() + ".yml").toPath());
                    this.getLogger().info("Converted " + e.getKey().toLowerCase() + ".yml to " + e.getValue() + ".yml");
                    playersConverted.add(e.getKey().toLowerCase());
                } catch (IOException ex) {
                    this.getLogger().warning("Could not convert " + e.getKey() + ".yml: " + ex.getClass().getSimpleName() + " (" + ex.getMessage() + ")");
                }
            }
        }
        playersToConvert.removeAll(playersConverted); // left over should be offline-mode players
        if (playersToConvert.size() > 0) this.getLogger().info("Converting offline-mode players.");
        for (final String name : playersToConvert) {
            if (name.trim().isEmpty()) continue;
            //noinspection deprecation
            final UUID uuid = this.getServer().getOfflinePlayer(name).getUniqueId();
            try {
                Files.move(new File(userdataFolder, name + ".yml").toPath(), new File(userdataFolder, uuid + ".yml").toPath());
                this.getLogger().info("Converted offline-mode player " + name + ".yml to " + uuid + ".yml");
            } catch (IOException ex) {
                this.getLogger().warning("Could not convert " + name + ".yml: " + ex.getClass().getSimpleName() + " (" + ex.getMessage() + ")");
            }
        }
    }

    /*private boolean unregisterCommand(String command) {
        final Command c = getCommandMap().getCommand(command);
        return c != null && c.unregister(getCommandMap());
    } save for overriding commands in the config*/

    private boolean versionCheck() {
        // If someone happens to be looking through this and knows a better way, let me know.
        if (!Config.checkVersion) return true;
        Pattern p = Pattern.compile("(\\d+.\\d+.\\d+).+");
        Matcher m = p.matcher(getServer().getBukkitVersion());
        if (!m.matches() || m.groupCount() < 1) {
            this.getLogger().warning("Could not get CraftBukkit version! No version checking will take place.");
            return true;
        }
        Integer currentVersion = RUtils.getInt(m.group(1).replace(".", ""));
        return currentVersion == null || currentVersion >= minVersion;
    }

    public boolean canAccessChest(Player p, Block b) {
        return this.lwc == null || this.lwc.getLWC().canAccessProtection(p, b);
    }

    @SuppressWarnings("unused")
    public boolean canBuild(Player p, Location l) {
        // return this.wg == null || this.wg.canBuild(p, l);
        return true;
    }

    public boolean canBuild(Player p, Block b) {
        // return this.wg == null || this.wg.canBuild(p, b);
        return true;
    }

    @SuppressWarnings("unused")
    public RApiMain getAPI() {
        return this.api;
    }

    public FancyConfiguration getFancyConfig() {
        final FancyConfiguration fc = new FancyConfiguration(new File(this.getDataFolder(), "config.yml"));
        fc.load();
        return fc;
    }

    public NMSFace getNMSFace() {
        return this.nmsFace;
    }

    public int getNumberVanished() {
        int hid = 0;
        for (Player p : getServer().getOnlinePlayers()) if (this.isVanished(p)) hid++;
        return hid;
    }

    public YamlConfiguration getPluginYml() {
        return this.pluginYml;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public boolean isVanished(Player p) {
        if (!Config.useVNP) return false;
        if (this.vp == null) {
            this.vp = (VanishPlugin) this.getServer().getPluginManager().getPlugin("VanishNoPacket");
            return false;
        }
        return this.vp.getManager().isVanished(p);
    }

    public boolean isVanished(Player p, CommandSender cs) {
        if (!Config.useVNP) return false;
        if (this.vp == null) {
            this.vp = (VanishPlugin) this.getServer().getPluginManager().getPlugin("VanishNoPacket");
            return false;
        }
        return !this.ah.isAuthorized(cs, "rcmds.seehidden") && this.vp.getManager().isVanished(p);
    }

    public void loadConfiguration() {
        if (!new File(getDataFolder(), "config.yml").exists()) saveDefaultConfig();
        if (!new File(getDataFolder(), "items.csv").exists()) saveResource("items.csv", false);
        if (!new File(getDataFolder(), "rules.txt").exists()) saveResource("rules.txt", false);
        if (!new File(getDataFolder(), "help.txt").exists()) saveResource("help.txt", false);
        if (!new File(getDataFolder(), "warps.yml").exists()) saveResource("warps.yml", false);
        final File file = new File(getDataFolder(), "userdata");
        if (!file.exists()) {
            try {
                boolean success = file.mkdir();
                if (success) this.getLogger().info("Created userdata directory.");
            } catch (Exception e) {
                this.getLogger().severe("Failed to make userdata directory!");
                this.getLogger().severe(e.getMessage());
            }
        }
    }

    @Override
    public void onDisable() {

        //-- Cancel scheduled tasks --//

        this.getServer().getScheduler().cancelTasks(this);

        //-- Save inventories --//

        WorldManager.il.saveAllInventories();

        //-- Save all userdata --//

        this.getLogger().info("Saving userdata and configurations (" + (PlayerConfigurationManager.configurationsCreated() + Configuration.configurationsCreated()) + " files in all)...");
        PlayerConfigurationManager.saveAllConfigurations();
        Configuration.saveAllConfigurations();
        this.getLogger().info("Userdata saved.");

        //-- ProtocolLib --//

        if (this.pl != null) this.pl.uninitialize();

        //-- We're done! --//

        this.getLogger().info("RoyalCommands v" + this.version + " disabled.");
    }

    @Override
    public void onEnable() {

        //-- Set fields --//

        RoyalCommands.instance = this;
        this.pluginYml = YamlConfiguration.loadConfiguration(new InputStreamReader(this.getResource("plugin.yml"), Charsets.UTF_8));
        RoyalCommands.dataFolder = getDataFolder();
        this.whl = Configuration.getConfiguration("whitelist.yml");
        RoyalCommands.commands = pluginYml.getConfigurationSection("reflectcommands");
        this.version = getDescription().getVersion();

        //-- Initialize ConfManagers if not made --//

        this.initializeConfManagers();

        //-- Work out NMS magic using mbaxter's glorious methods --//

        this.initializeNMS();

        //-- Hidendra's Metrics --//

        this.initializeMetrics();

        //-- Get help --//

        this.h = new Help(this);

        //-- Get configs --//

        this.loadConfiguration();
        this.c = new Config(this);
        this.c.reloadConfiguration();

        //-- Check CB version --//

        if (!versionCheck()) {
            this.getLogger().severe("This version of CraftBukkit is too old to run RoyalCommands!");
            this.getLogger().severe("This version of RoyalCommands needs at least CraftBukkit " + this.minVersion + ".");
            this.getLogger().severe("Disabling plugin. You can turn this check off in the config.");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        //-- Set up Vault --//

        this.vh.setUpVault();

        //-- Update old userdata --//
        if (Config.updateOldUserdata) this.update();

        //-- Schedule tasks --//

        this.initializeTasks();

        //-- Get dependencies --//

        this.vp = (VanishPlugin) this.getServer().getPluginManager().getPlugin("VanishNoPacket");
        // this.wg = (WorldGuardPlugin) this.getServer().getPluginManager().getPlugin("WorldGuard");
        this.lwc = (LWCPlugin) this.getServer().getPluginManager().getPlugin("LWC");
        RoyalCommands.mvc = (MultiverseCore) this.getServer().getPluginManager().getPlugin("Multiverse-Core");

        //-- Register events --//

        final PluginManager pm = this.getServer().getPluginManager();

        pm.registerEvents(new PlayerListener(this), this);
        pm.registerEvents(new EntityListener(this), this);
        pm.registerEvents(new BlockListener(this), this);
        pm.registerEvents(new SignListener(this), this);
        pm.registerEvents(new MonitorListener(this), this);
        pm.registerEvents(new ServerListener(this), this);
        pm.registerEvents(new ItemListener(this), this);
        pm.registerEvents(new BackpackListener(), this);
        pm.registerEvents(new ClickListener(), this);
        pm.registerEvents(new TradeListener(), this);
        pm.registerEvents(new InventoryGUIEventListener(), this);

        //-- ProtocolLib things --//

        final Plugin plPlugin = this.getServer().getPluginManager().getPlugin("ProtocolLib");
        if (Config.useProtocolLib && plPlugin != null && plPlugin.isEnabled()) {
            this.pl = new ProtocolListener(this);
            this.pl.initialize();
        }

        //-- Register commands --//

        for (final String command : this.getCommands().getValues(false).keySet()) {
            final ConfigurationSection ci = this.getCommandInfo(command);
            if (ci == null) continue;
            final String className = ci.getString("class");
            if (className == null) continue;
            try {
                final Class<?> clazz = Class.forName("org.royaldev.royalcommands.rcommands." + className);
                if (!clazz.isAnnotationPresent(ReflectCommand.class)) continue;
                final Constructor c = clazz.getConstructor(RoyalCommands.class, String.class);
                final Object o = c.newInstance(this, command);
                if (!(o instanceof BaseCommand)) continue;
                this.registerCommand((CommandExecutor) o, command);
            } catch (Exception e) {
                this.getLogger().warning("Could not register command \"" + command + "\" - an error occurred (" + e.getClass().getSimpleName() + "): " + e.getMessage() + ".");
            }
        }

        //-- Make the API --//

        this.api = new RApiMain();

        //-- Convert old backpacks --//

        pm.registerEvents(new BackpackConverter(), this);

        //-- We're done! --//

        this.getLogger().info("RoyalCommands v" + this.version + " initiated.");
    }

    private class BackpackConverter implements Listener {

        @EventHandler
        public void onJoin(PlayerJoinEvent e) {
            final Player p = e.getPlayer();
            final PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(p);
            if (!pcm.isSet("backpack.item")) return;
            if (!pcm.exists()) pcm.createFile();
            int invSize = pcm.getInt("backpack.size", -1);
            if (invSize < 9) invSize = 36;
            if (invSize % 9 != 0) invSize = 36;
            final Inventory i = Bukkit.createInventory(p, invSize, "Backpack");
            for (int slot = 0; slot < invSize; slot++) {
                final ItemStack is = pcm.getItemStack("backpack.item." + slot);
                if (is == null) continue;
                i.setItem(slot, is);
            }
            RUtils.saveBackpack(p, i);
            pcm.set("backpack.item", null);
            pcm.set("backpack.size", null);
        }
    }

}
