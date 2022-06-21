/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands;

import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.royaldev.royalchat.RoyalChat;
import org.royaldev.royalcommands.configuration.GeneralConfiguration;
import org.royaldev.royalcommands.configuration.PlayerConfiguration;
import org.royaldev.royalcommands.configuration.PlayerConfigurationManager;
import org.royaldev.royalcommands.exceptions.InvalidItemNameException;
import org.royaldev.royalcommands.listeners.BackpackListener;
import org.royaldev.royalcommands.rcommands.CmdBack;
import org.royaldev.royalcommands.shaded.mkremins.fanciful.FancyMessage;
import org.royaldev.royalcommands.spawninfo.SpawnInfo;
import org.royaldev.royalcommands.tools.NameFetcher;
import org.royaldev.royalcommands.tools.UUIDFetcher;

@SuppressWarnings("unused")
public final class RUtils {

    public static final Set<Material> AIR_MATERIALS = new HashSet<>();

    static {
        for (final Material m : Material.values()) {
            if (m.isSolid()) continue;
            AIR_MATERIALS.add(m);
        }
    }

    private static final Map<String, Integer> teleRunners = new HashMap<>();
    private static final List<String> teleAllowed = new ArrayList<>();

    public static FancyMessage addCommandTo(FancyMessage fm, String command) {
        return RUtils.addDataTo(fm, new String[]{"clickActionName", "clickActionData"}, "run_command", command);
    }

    public static FancyMessage addDataTo(FancyMessage fm, String[] fields, Object... values) {
        final Iterator i = fm.iterator();
        for (final Object o : fm) {
            try {
                RUtils.setFields(o, fields, values);
            } catch (ReflectiveOperationException ex) {
                ex.printStackTrace();
                return fm;
            }
        }
        return fm;
    }

    /**
     * Adds lore to an ItemStack.
     *
     * @param is      ItemStack to add lore to
     * @param newLore Lore to add
     * @return ItemStack with added lore
     */
    public static ItemStack addLore(ItemStack is, String newLore) {
        if (is == null) throw new IllegalArgumentException("ItemStack cannot be null!");
        if (newLore == null) return is;
        ItemMeta im = is.getItemMeta();
        List<String> lores = im.getLore();
        if (lores == null) lores = new ArrayList<>();
        lores.add(newLore);
        im.setLore(lores);
        is.setItemMeta(im);
        return is;
    }

    public static ItemStack applySpawnLore(ItemStack stack) {
        final ItemMeta im = stack.getItemMeta();
        final List<String> lore = (im.hasLore()) ? im.getLore() : new ArrayList<String>();
        for (String s : Config.itemSpawnTagLore) lore.add(RUtils.colorize(s));
        im.setLore(lore);
        stack.setItemMeta(im);
        return stack;
    }

    public static void banIP(String ip, CommandSender cs, String reason) {
        final BanList bl = Bukkit.getBanList(BanList.Type.IP);
        bl.addBan(ip, reason, null, cs.getName());
    }

    /**
     * Bans a player. Message is not sent to banned player or person who banned.
     * Message is broadcasted to those with rcmds.see.ban
     * Kicks banned player if they're online.
     * <br>
     * This is only used for permabans.
     *
     * @param t      Player to ban
     * @param cs     CommandSender who issued the ban
     * @param reason Reason for the ban
     */
    public static void banPlayer(OfflinePlayer t, CommandSender cs, String reason) {
        reason = colorize(reason);
        final BanList bl = Bukkit.getBanList(BanList.Type.NAME);
        bl.addBan(t.getName(), reason, null, cs.getName());
        writeBanHistory(t);
        String inGameFormat = Config.igBanFormat;
        String outFormat = Config.banFormat;
        executeBanActions(t, cs, reason);
        Bukkit.getServer().broadcast(getInGameMessage(inGameFormat, reason, t, cs), "rcmds.see.ban");
        if (t.isOnline()) ((Player) t).kickPlayer(getMessage(outFormat, reason, cs));
    }

    public static void cancelTeleportRunner(final Player p) {
        synchronized (teleRunners) {
            if (teleRunners.containsKey(p.getName())) {
                Bukkit.getScheduler().cancelTask(teleRunners.get(p.getName()));
                teleRunners.remove(p.getName());
            }
        }
    }

    /**
     * Charges a CommandSender an amount of money
     *
     * @param cs     CommandSender to charge
     * @param amount Amount to charge cs
     * @return true if transaction was successful, false if otherwise
     */
    public static boolean chargePlayer(CommandSender cs, double amount) {
        if (!(cs instanceof OfflinePlayer)) return false;
        final OfflinePlayer op = (OfflinePlayer) cs;
        if (!RoyalCommands.getInstance().vh.usingVault() || RoyalCommands.getInstance().vh.getEconomy() == null) {
            cs.sendMessage(MessageColor.NEGATIVE + "No economy! Continuing without charging.");
            return true;
        }
        if (!RoyalCommands.getInstance().vh.getEconomy().hasAccount(op)) {
            cs.sendMessage(MessageColor.NEGATIVE + "You don't have a bank account!");
            return false;
        }
        if (RoyalCommands.getInstance().vh.getEconomy().getBalance(op) < amount) {
            cs.sendMessage(MessageColor.NEGATIVE + "You don't have enough money!");
            return false;
        }
        RoyalCommands.getInstance().vh.getEconomy().withdrawPlayer(op, amount);
        cs.sendMessage(MessageColor.POSITIVE + "You have had " + MessageColor.NEUTRAL + RoyalCommands.getInstance().vh.getEconomy().format(amount) + MessageColor.POSITIVE + " removed from your account.");
        return true;
    }

    public static void checkMail(Player p) {
        PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(p);
        if (!pcm.getStringList("mail").isEmpty()) {
            int count = pcm.getStringList("mail").size();
            String poss = (count != 1) ? "s" : "";
            p.sendMessage(MessageColor.POSITIVE + "Your mailbox contains " + MessageColor.NEUTRAL + count + MessageColor.POSITIVE + " message" + poss + ".");
            if (RoyalCommands.getInstance().ah.isAuthorized(p, "rcmds.mail"))
                p.sendMessage(MessageColor.POSITIVE + "View mail with " + MessageColor.NEUTRAL + "/mail read" + MessageColor.POSITIVE + ".");
        }
    }

    /**
     * Clears lore on an ItemStack.
     *
     * @param is ItemStack to clear lore from
     * @return ItemStack with no lore
     */
    public static ItemStack clearLore(ItemStack is) {
        if (is == null) throw new IllegalArgumentException("ItemStack cannot be null!");
        ItemMeta im = is.getItemMeta();
        im.setLore(null);
        is.setItemMeta(im);
        return is;
    }

    /**
     * Replaces raw color codes with processed color codes
     *
     * @param text String with codes to be converted
     * @return Processed string
     */
    public static String colorize(final String text) {
        if (text == null) return null;
        return ChatColor.translateAlternateColorCodes('&', text);
        // return text.replaceAll("(?i)&([a-f0-9k-or])", ChatColor.COLOR_CHAR + "$1");
    }

    /**
     * Returns an empty inventory for use.
     *
     * @param handler May be null - owner of inventory
     * @param size    Size of inventory - MUST be divisible by 9
     * @param name    May be null (default to Chest) - name of inventory to open
     * @return Inventory or null if size not divisible by 9
     */
    public static Inventory createInv(InventoryHolder handler, Integer size, String name) {
        if (size == null) size = 27;
        //if (size % 9 != 0) return null;
        final Inventory i = Bukkit.getServer().createInventory(handler, size, name);
        i.clear();
        return i;
    }

    private static int dateDiff(int type, Calendar fromDate, Calendar toDate, boolean future) {
        int diff = 0;
        long savedDate = fromDate.getTimeInMillis();
        while ((future && !fromDate.after(toDate)) || (!future && !fromDate.before(toDate))) {
            savedDate = fromDate.getTimeInMillis();
            fromDate.add(type, future ? 1 : -1);
            diff++;
        }
        diff--;
        fromDate.setTimeInMillis(savedDate);
        return diff;
    }

    /**
     * Removes color codes that have not been processed yet (&amp;char)
     * <br>
     * This fixes a common exploit where color codes can be embedded into other codes:
     * &amp;&amp;aa (replaces &amp;a, and the other letters combine to make &amp;a again)
     *
     * @param message String with raw color codes
     * @return String without raw color codes
     */
    public static String decolorize(String message) {
        final Pattern p = Pattern.compile("(?i)&[a-f0-9k-or]");
        boolean contains = p.matcher(message).find();
        while (contains) {
            message = message.replaceAll("(?i)&[a-f0-9k-or]", "");
            contains = p.matcher(message).find();
        }
        return message;
    }

    /**
     * Recursively deletes a directory.
     *
     * @param f Directory to delete
     * @return If all files were deleted, true, else false
     */
    public static boolean deleteDirectory(File f) {
        boolean success = true;
        if (!f.isDirectory()) return false;
        File[] files = f.listFiles();
        if (files == null) return false;
        for (File delete : files) {
            if (delete.isDirectory()) {
                boolean recur = deleteDirectory(delete);
                if (success) success = recur; // if all has been okay, set to new value.
                continue;
            }
            if (!delete.delete()) {
                RoyalCommands.getInstance().getLogger().warning("Could not delete " + delete.getAbsolutePath());
                success = false;
            }
        }
        if (success) success = f.delete(); // don't delete directory if files still remain
        return success;
    }

    /**
     * Sends the standard message of no permission to console and command sender
     *
     * @param cs CommandSender to send message to
     */
    public static void dispNoPerms(CommandSender cs) {
        cs.sendMessage(MessageColor.NEGATIVE + "You don't have permission for that!");
        RoyalCommands.getInstance().getLogger().warning(cs.getName() + " was denied access to that!");
    }

    /**
     * Displays a no permissions message to the command sender and console/
     *
     * @param cs      CommandSender to send message to
     * @param message Custom message to send
     */
    public static void dispNoPerms(CommandSender cs, String message) {
        cs.sendMessage(message);
        RoyalCommands.getInstance().getLogger().warning(cs.getName() + " was denied access to that!");
    }

    public static void dispNoPerms(CommandSender cs, String... permissionsNeeded) {
        final List<FancyMessage> tooltip = new ArrayList<>();
        tooltip.add(new FancyMessage("Missing permissions").color(MessageColor.NEGATIVE.cc()).style(ChatColor.BOLD, ChatColor.UNDERLINE));
        for (final String missingPermission : permissionsNeeded)
            tooltip.add(new FancyMessage(missingPermission).color(MessageColor.NEUTRAL.cc()));
        // @formatter:off
        new FancyMessage("You don't have permission for that!")
                .color(MessageColor.NEGATIVE.cc())
                .formattedTooltip(tooltip)
            .send(cs);
        // @formatter:on
        RoyalCommands.getInstance().getLogger().warning(cs.getName() + " was denied access to that!");
    }

    private static void executeBanActions(OfflinePlayer banned, CommandSender banner, String reason) {
        if (!RoyalCommands.getInstance().getConfig().getKeys(false).contains("on_ban"))
            return; // default values are not welcome here
        final List<String> banActions = Config.onBanActions;
        if (banActions == null || banActions.isEmpty()) return;
        for (String command : banActions) {
            if (command.trim().isEmpty()) continue;
            boolean fromConsole = command.startsWith("@");
            if (fromConsole) command = command.substring(1);

            command = command.replace("{name}", banned.getName());
            command = command.replace("{dispname}", (banned.isOnline()) ? ((Player) banned).getDisplayName() : banned.getName());
            command = command.replace("{banner}", banner.getName());
            command = command.replace("{bannerdispname}", (banner instanceof Player) ? ((Player) banner).getDisplayName() : banner.getName());
            command = command.replace("{reason}", reason);

            CommandSender sendFrom = (fromConsole) ? Bukkit.getConsoleSender() : banner;
            Bukkit.dispatchCommand(sendFrom, command);
        }
    }

    public static String forceGetName(UUID u) {
        String name;
        try {
            name = RUtils.getName(u);
        } catch (Exception ex) {
            name = u.toString();
        }
        return name;
    }

    public static String formatDateDiff(long date) {
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(date);
        Calendar now = new GregorianCalendar();
        return formatDateDiff(now, c);
    }

    public static String formatDateDiff(Calendar fromDate, Calendar toDate) {
        boolean future = false;
        if (toDate.equals(fromDate)) return " now";
        if (toDate.after(fromDate)) future = true;
        StringBuilder sb = new StringBuilder();
        int[] types = new int[]{Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE};
        String[] names = new String[]{"year", "years", "month", "months", "day", "days", "hour", "hours", "minute", "minutes"};
        for (int i = 0; i < types.length; i++) {
            int diff = dateDiff(types[i], fromDate, toDate, future);
            if (diff > 0) sb.append(" ").append(diff).append(" ").append(names[i * 2 + (diff > 1 ? 1 : 0)]);
        }
        if (sb.length() == 0) return " now";
        return sb.toString();
    }

    public static List<String> getAssignment(ItemStack is, GeneralConfiguration gcf) {
        return gcf.getStringList(getAssignmentPath(is));
    }

    public static String getAssignmentPath(ItemStack is) {
        return getAssignmentPath(is, Config.assignUseDisplayNames, Config.assignUseDurability);
    }

    public static String getAssignmentPath(ItemStack is, boolean customNames, boolean durability) {
        StringBuilder path = new StringBuilder("assign.");
        path.append(is.getType().name());
        if (customNames) {
            ItemMeta im = is.getItemMeta();
            if (im != null) {
                String displayName = im.getDisplayName();
                if (displayName != null) path.append(".").append(displayName.replace('.', ',')).append(".");
                List<String> lore = im.getLore();
                if (lore != null && !lore.isEmpty()) {
                    for (String l : lore) {
                        path.append(l.replace('.', ','));
                        path.append(".");
                    }
                }
            }
        }
        if (durability) path.append(is.getDurability()).append(".");
        path.append("commands");
        return path.toString();
    }

    /**
     * Gets a player backpack
     *
     * @param u UUID of player to get backpack for
     * @return Backpack - never null
     * @deprecated Use {@link org.royaldev.royalcommands.wrappers.player.MemoryRPlayer#getBackpack}.
     */
    @Deprecated
    public static Inventory getBackpack(UUID u, World w) {
        PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(u);
        String worldGroup = WorldManager.il.getWorldGroup(w);
        if (worldGroup == null) worldGroup = "w-" + w.getName();
        if (!pcm.exists()) pcm.createFile();
        int invSize = pcm.getInt("backpack." + worldGroup + ".size", -1);
        if (invSize < 9) invSize = 36;
        if (invSize % 9 != 0) invSize = 36;
        final Inventory i = Bukkit.createInventory(new BackpackListener.BackpackHolder(u, w), invSize, "Backpack");
        if (!pcm.isSet("backpack." + worldGroup + ".item")) return i;
        for (int slot = 0; slot < invSize; slot++) {
            ItemStack is = pcm.getItemStack("backpack." + worldGroup + ".item." + slot);
            if (is == null) continue;
            i.setItem(slot, is);
        }
        return i;
    }

    /**
     * Gets a player backpack
     *
     * @param p Player to get backpack for
     * @return Backpack - never null
     * @deprecated Use {@link org.royaldev.royalcommands.wrappers.player.MemoryRPlayer#getBackpack}.
     */
    @Deprecated
    public static Inventory getBackpack(Player p) {
        return getBackpack(p.getUniqueId(), p.getWorld());
    }

    public static Command getCommand(String name) {
        try {
            final Field map = RoyalCommands.getInstance().getServer().getPluginManager().getClass().getDeclaredField("commandMap");
            map.setAccessible(true);
            final CommandMap cm = (CommandMap) map.get(RoyalCommands.getInstance().getServer().getPluginManager());
            return cm.getCommand(name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @deprecated Use {@link org.royaldev.royalcommands.wrappers.player.MemoryRPlayer#getHomes}.size()
     */
    @Deprecated
    public static int getCurrentHomes(Player p) {
        return getCurrentHomes(p.getUniqueId());
    }

    /**
     * @deprecated Use {@link org.royaldev.royalcommands.wrappers.player.MemoryRPlayer#getHomes}.size()
     */
    @Deprecated
    public static int getCurrentHomes(UUID u) {
        ConfigurationSection pconf = PlayerConfigurationManager.getConfiguration(u).getConfigurationSection("home");
        if (pconf == null) return 0;
        return pconf.getValues(false).size();
    }

    /**
     * Returns the Double from a String
     *
     * @param number String to get double from
     * @return Double or null if string was not a valid double
     */
    public static Double getDouble(String number) {
        try {
            return Double.valueOf(number);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gets an empty inventory with a backpack configuration.
     *
     * @return Backpack
     */
    public static Inventory getEmptyBackpack() {
        return Bukkit.createInventory(null, 36, "Backpack");
    }

    /**
     * Gets enchantments from form "name:level,..." (e.g. "damage_all:2,durability:1")
     *
     * @param enchant String of enchantment
     * @return Map of Enchantments and their levels or null if invalid
     */
    public static Map<Enchantment, Integer> getEnchantments(String enchant) {
        final Map<Enchantment, Integer> enchants = new HashMap<>();
        for (String enc : enchant.split(",")) {
            enc = enc.replace(" ", "");
            String[] data = enc.split(":");
            if (data.length < 2) return null;
            String name = data[0];
            int lvl;
            try {
                lvl = Integer.parseInt(data[1]);
            } catch (NumberFormatException e) {
                continue;
            }
            Enchantment e = Enchantment.getByName(name.toUpperCase());
            if (e == null) continue;
            enchants.put(e, lvl);
        }
        return enchants;
    }

    public static String getFriendlyEnumName(Enum e) {
        return e.name().toLowerCase().replace("_", " ");
    }

    /**
     * @deprecated Use {@link org.royaldev.royalcommands.wrappers.player.MemoryRPlayer#getHomeLimit}
     */
    @Deprecated
    public static int getHomeLimit(Player p) {
        String name = p.getName();
        String group;
        if (RoyalCommands.getInstance().vh.usingVault()) {
            try {
                group = RoyalCommands.getInstance().vh.getPermission().getPrimaryGroup(p);
            } catch (Exception e) {
                group = "";
            }
        } else group = "";
        if (group == null) group = "";
        int limit;
        final FileConfiguration c = RoyalCommands.getInstance().getConfig();
        if (c.isSet("homes.limits.players." + name)) limit = c.getInt("homes.limits.players." + name, -1);
        else limit = c.getInt("homes.limits.groups." + group, -1);
        return limit;
    }

    /**
     * Gets the message shown to other players on the server when someone is disconnected (kicked).
     *
     * @param format Format of the message
     * @param reason Reason for disconnect
     * @param kicked Person disconnected
     * @param kicker Person who caused disconnect
     * @return Formatted string
     */
    public static String getInGameMessage(final String format, final String reason, final OfflinePlayer kicked, final CommandSender kicker) {
        if (reason == null || kicked == null || kicker == null) return null;
        String message = format;
        message = colorize(message);
        if (kicked.isOnline()) message = message.replace("{kdispname}", ((Player) kicked).getDisplayName());
        else message = message.replace("{kdispname}", kicked.getName());
        message = message.replace("{kname}", kicked.getName());
        message = message.replace("{name}", kicker.getName());
        if (kicker instanceof Player) message = message.replace("{dispname}", ((Player) kicker).getDisplayName());
        else message = message.replace("{dispname}", kicker.getName());
        message = message.replace("{reason}", reason);
        return message;
    }

    /**
     * Returns the Integer from a String
     *
     * @param number String to get int from
     * @return Integer or null if string was not a valid integer
     */
    public static Integer getInt(String number) {
        try {
            return Integer.valueOf(number);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns the ItemStack for any material name and amount.
     * If amount is null, will be default stack size.
     * <br>
     * name can contain a ":" to specify data
     *
     * @param name   Name of the material
     * @param amount Amount of items or null for default
     * @return ItemStack or null if no such material
     */
    public static ItemStack getItem(String name, Integer amount) {
        if (name == null) return null;
        Short data;
        String datas = null;
        name = name.trim().toUpperCase();
        if (name.contains(":")) {
            if (name.split(":").length < 2) {
                datas = null;
                name = name.split(":")[0];
            } else {
                datas = name.split(":")[1];
                name = name.split(":")[0];
            }
        }
        try {
            data = Short.valueOf(datas);
        } catch (Exception e) {
            if (datas != null) return null;
            else data = null;
        }
        Material mat = Material.getMaterial(name);
        if (mat == null) return null;
        if (amount == null) amount = Config.defaultStack;
        ItemStack stack = new ItemStack(mat, amount);
        if (data != null) stack.setDurability(data);
        return stack;
    }

    /**
     * Gets an ItemStack from an alias and an amount.
     *
     * @param alias  Alias of the item name
     * @param amount Amount of the item to be in the stack
     * @return ItemStack
     * @throws InvalidItemNameException If item alias is not valid
     * @throws NullPointerException     If ItemNameManager is not loaded
     */
    public static ItemStack getItemFromAlias(String alias, int amount) throws InvalidItemNameException, NullPointerException {
        ItemStack is;
        if (RoyalCommands.inm == null) throw new NullPointerException("ItemNameManager is not loaded!");
        is = RoyalCommands.inm.getItemStackFromAlias(alias);
        if (is == null) throw new InvalidItemNameException(alias + " is not a valid alias!");
        is.setAmount(amount);
        return is;
    }

    /**
     * Returns formatted name of an ItemStack.
     *
     * @param is ItemStack to get name for
     * @return Name of item (formatted)
     */
    public static String getItemName(ItemStack is) {
        return getItemName(is.getType());
    }

    /**
     * Returns formatted name of a Material
     *
     * @param m Material to get name for
     * @return Name of item (formatted)
     */
    public static String getItemName(Material m) {
        return m.name().toLowerCase().replace("_", " ");
    }

    public static String getMVWorldName(World w) {
        if (w == null) throw new NullPointerException("w can't be null!");
        if (!Config.multiverseNames || RoyalCommands.mvc == null)
            return RoyalCommands.wm.getConfig().getString("worlds." + w.getName() + ".displayname", w.getName());
        return RoyalCommands.mvc.getMVWorldManager().getMVWorld(w).getColoredWorldString();
    }

    /**
     * Gets the message shown to a player on disconnect.
     *
     * @param message Format of the message
     * @param reason  Reason for disconnect.
     * @param kicker  Person who caused disconnect.
     * @return Formatted string
     */
    public static String getMessage(final String message, final String reason, final CommandSender kicker) {
        String format = message;
        format = colorize(format);
        if (kicker instanceof Player) format = format.replace("{dispname}", ((Player) kicker).getDisplayName());
        else format = format.replace("{dispname}", kicker.getName());
        format = format.replace("{name}", kicker.getName());
        format = format.replace("{reason}", reason);
        return format;
    }

    public static String getMessage(final String message, final String reason, final String kicker) {
        String format = message;
        format = colorize(format);
        format = format.replace("{dispname}", kicker);
        format = format.replace("{name}", kicker);
        format = format.replace("{reason}", reason);
        return format;
    }

    public static String getName(UUID u) throws Exception {
        return new NameFetcher(Arrays.asList(u)).call().get(u);
    }

    /**
     * Gets an OfflinePlayer with support for name completion. If no Player is on that matches the beginning of the
     * name, the string provided will be used to get an OfflinePlayer to return. If there is a Player, it will be cast
     * to OfflinePlayer and returned.
     *
     * @param name Name of player
     * @return OfflinePlayer
     */
    @SuppressWarnings("deprecation")
    public static OfflinePlayer getOfflinePlayer(String name) {
        OfflinePlayer op = RoyalCommands.getInstance().getServer().getPlayerExact(name);
        if (op == null) op = RoyalCommands.getInstance().getServer().getPlayer(name);
        if (op == null) op = RoyalCommands.getInstance().getServer().getOfflinePlayer(name);
        return op;
    }

    public static List<FancyMessage> getPlayerTooltip(final Object o) {
        final List<FancyMessage> tooltip = new ArrayList<>();
        final VaultHandler vh = RoyalCommands.getInstance().vh;
        if (o instanceof OfflinePlayer) {
            final OfflinePlayer op = (OfflinePlayer) o;
            if (tooltip.size() < 1)
                tooltip.add(new FancyMessage("Offline Player").color(MessageColor.NEUTRAL.cc()).style(ChatColor.BOLD, ChatColor.UNDERLINE));
            tooltip.add(new FancyMessage("Name: ").color(MessageColor.POSITIVE.cc()).style(ChatColor.BOLD).then(op.getName()).color(MessageColor.NEUTRAL.cc()));
            tooltip.add(new FancyMessage("Operator: ").color(MessageColor.POSITIVE.cc()).style(ChatColor.BOLD).then(op.isOp() ? "Yes" : "No").color(MessageColor.NEUTRAL.cc()));
            if (vh.usingVault() && vh.getPermission().hasGroupSupport()) {
                final String group = vh.getPermission().getPrimaryGroup(null, op);
                final Chat c = vh.getChat();
                String prefix = c == null ? null : c.getGroupPrefix((String) null, group);
                if (prefix == null || prefix.isEmpty()) prefix = c == null ? null : c.getPlayerPrefix(null, op);
                if (prefix == null) prefix = "";
                String suffix = c == null ? null : c.getGroupSuffix((String) null, group);
                if (suffix == null || suffix.isEmpty()) suffix = c == null ? null : c.getPlayerSuffix(null, op);
                if (suffix == null) suffix = "";
                // TODO: Config format
                if (!group.isEmpty()) {
                    tooltip.add(new FancyMessage("Group: ").color(MessageColor.POSITIVE.cc()).style(ChatColor.BOLD).then(ChatColor.translateAlternateColorCodes('&', prefix)).color(MessageColor.NEUTRAL.cc()).then(group).then(ChatColor.translateAlternateColorCodes('&', suffix)));
                }
            }
        }
        if (o instanceof Player) {
            final Player p = (Player) o;
            if (tooltip.size() > 0) { // Replace Offline Player with Player
                tooltip.remove(0);
                tooltip.add(0, new FancyMessage("Player").color(MessageColor.NEUTRAL.cc()).style(ChatColor.BOLD, ChatColor.UNDERLINE));
            }
        }
        if (o instanceof ConsoleCommandSender) {
            if (tooltip.size() < 1) {
                tooltip.add(new FancyMessage("Console").color(MessageColor.NEUTRAL.cc()).style(ChatColor.BOLD, ChatColor.UNDERLINE));
            }
        }
        return tooltip.isEmpty() ? null : tooltip;
    }

    public static List<FancyMessage> getItemTooltip(final Object o) {
        final List<FancyMessage> tooltip = new ArrayList<>();
        final VaultHandler vh = RoyalCommands.getInstance().vh;
        if (o instanceof Material) {
            final Material item = (Material) o;
            tooltip.add(new FancyMessage("Item").color(MessageColor.NEUTRAL.cc()).style(ChatColor.BOLD, ChatColor.UNDERLINE));
            tooltip.add(new FancyMessage("Name: ").color(MessageColor.POSITIVE.cc()).style(ChatColor.BOLD).then(getItemName(item)).color(MessageColor.NEUTRAL.cc()));
            if(item.getMaxDurability() > 0) {
                tooltip.add(new FancyMessage("Durability: ").color(MessageColor.POSITIVE.cc()).style(ChatColor.BOLD).then(String.valueOf(item.getMaxDurability())).color(MessageColor.NEUTRAL.cc()));
            }
        }
        return tooltip.isEmpty() ? null : tooltip;
    }

    public static Object getPrivateField(Object object, String field) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Class<?> clazz = object.getClass();
        Field objectField = clazz.getDeclaredField(field);
        final boolean wasAccessible = objectField.isAccessible();
        objectField.setAccessible(true);
        Object result = objectField.get(object);
        objectField.setAccessible(wasAccessible);
        return result;
    }

    public static Object getPrivateFieldSuper(Object object, String field) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Class<?> clazz = object.getClass().getSuperclass();
        Field objectField = clazz.getDeclaredField(field);
        final boolean wasAccessible = objectField.isAccessible();
        objectField.setAccessible(true);
        Object result = objectField.get(object);
        objectField.setAccessible(wasAccessible);
        return result;
    }

    public static String getRChatGroupPrefix(final String s) {
        try {
            Class.forName("org.royaldev.royalchat.DataManager");
        } catch (ClassNotFoundException e) {
            return null;
        }
        RoyalChat rc = (RoyalChat) Bukkit.getPluginManager().getPlugin("RoyalChat");
        String prefix = rc.dm.getGroupPrefix(s);
        if (prefix.isEmpty()) prefix = null;
        return prefix;
    }

    public static String getRChatGroupSuffix(final String s) {
        try {
            Class.forName("org.royaldev.royalchat.DataManager");
        } catch (ClassNotFoundException e) {
            return null;
        }
        RoyalChat rc = (RoyalChat) Bukkit.getPluginManager().getPlugin("RoyalChat");
        String suffix = rc.dm.getGroupSuffix(s);
        if (suffix.isEmpty()) suffix = null;
        return suffix;
    }

    public static String getRChatPrefix(final Player p) {
        try {
            Class.forName("org.royaldev.royalchat.DataManager");
        } catch (ClassNotFoundException e) {
            return null;
        }
        RoyalChat rc = (RoyalChat) Bukkit.getPluginManager().getPlugin("RoyalChat");
        String prefix = rc.dm.getPrefix(p);
        if (prefix.isEmpty()) prefix = null;
        return prefix;
    }

    public static String getRChatSuffix(final Player p) {
        try {
            Class.forName("org.royaldev.royalchat.DataManager");
        } catch (ClassNotFoundException e) {
            return null;
        }
        RoyalChat rc = (RoyalChat) Bukkit.getPluginManager().getPlugin("RoyalChat");
        String suffix = rc.dm.getSuffix(p);
        if (suffix.isEmpty()) suffix = null;
        return suffix;
    }

    /**
     * Returns a location that is always above ground.
     * If there is no ground under the location, returns
     * null.
     *
     * @param l Location to find safe location for
     * @return Safe location or null if no ground
     */
    public static Location getSafeLocation(Location l) {
        int unsafeY = l.getBlockY();
        if (unsafeY < 0) return null;
        for (int i = unsafeY; i >= 0; i--) {
            if (i < 0) return null;
            Block b = l.getWorld().getBlockAt(l.getBlockX(), i, l.getBlockZ());
            if (b == null) return null;
            if (b.getType() == Material.AIR) continue;
            double safeY = l.getY() - (unsafeY - i);
            return new Location(l.getWorld(), l.getX(), safeY + 1, l.getZ(), l.getYaw(), l.getPitch());
        }
        return null;
    }

    /**
     * Returns a location that is always above ground.
     * If there is no ground under the location, returns
     * null.
     *
     * @param e Entity to derive location from
     * @return Safe location or null if no ground
     */
    public static Location getSafeLocation(Entity e) {
        if (e == null) return null;
        return getSafeLocation(e.getLocation());
    }

    /**
     * Gets the block the player is looking at
     *
     * @param p Player to get block from
     * @return Block player is looking at
     */
    @SuppressWarnings("deprecation")
    public static Block getTarget(Player p) {
        return p.getTargetBlock((Set<Material>) null, 300); // waiting on method for Materials
    }

    /**
     * Gets a timestamp from a player's userdata file.
     *
     * @param p     OfflinePlayer to get timestamp from
     * @param title Path of timestamp
     * @return timestamp or -1 if there was no such timestamp
     */
    public static long getTimeStamp(OfflinePlayer p, String title) {
        PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(p);
        if (pcm.get(title) == null) return -1;
        return pcm.getLong(title);
    }

    public static UUID getUUID(String name) throws Exception {
        final Map<String, UUID> m = new UUIDFetcher(Arrays.asList(name)).call();
        for (Map.Entry<String, UUID> e : m.entrySet()) if (e.getKey().equalsIgnoreCase(name)) return e.getValue();
        throw new Exception("Couldn't find name in results.");
    }

    @Deprecated
    public static Entity getVehicleToTeleport(final Entity rider) {
        if (!Config.vehicleTeleportEnabled) return null;
        final Entity vehicle = rider.getVehicle();
        if (vehicle == null) return null;
        if (Config.vehicleTeleportVehicles && vehicle instanceof Vehicle) return vehicle;
        if (Config.vehicleTeleportAnimals && vehicle instanceof Animals) return vehicle;
        if (Config.vehicleTeleportPlayers && vehicle instanceof Player) return vehicle;
        return null;
    }

    /**
     * Gets a world via its real name, Multiverse name, or WorldManager name.
     *
     * @param name Name of world to get
     * @return World or null if none exists
     */
    public static World getWorld(String name) {
        World w;
        w = Bukkit.getWorld(name);
        if (w != null) return w;
        if (RoyalCommands.mvc != null) {
            MultiverseWorld mvw = RoyalCommands.mvc.getMVWorldManager().getMVWorld(name);
            w = (mvw == null) ? null : mvw.getCBWorld();
            if (w != null) return w;
        }
        w = RoyalCommands.wm.getWorld(name);
        return w;
    }

    public static boolean isBanned(final Player p) {
        final PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(p);
        if (pcm.isSet("bantime")) {
            if (RUtils.isTimeStampValid(p, "bantime")) return true;
            else {
                pcm.set("bantime", null);
                RUtils.unbanPlayer(p);
                return false;
            }
        }
        return p.isBanned();
    }

    public static boolean isIPBanned(Player p) {
        return Bukkit.getIPBans().contains(p.getAddress().getAddress().toString().replace("/", ""));
    }

    public static boolean isIPBanned(String ip) {
        return Bukkit.getIPBans().contains(ip);
    }

    private static boolean isInt(String s) {
        Integer i;
        try {
            i = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            i = null;
        }
        return i != null;
    }

    private static boolean isInt(char c) {
        Integer i;
        try {
            i = Integer.parseInt(String.valueOf(c));
        } catch (NumberFormatException e) {
            i = null;
        }
        return i != null;
    }

    /**
     * Checks to see if teleport is allowed for the specified OfflinePlayer
     *
     * @param p OfflinePlayer to check teleportation status on
     * @return true or false
     */
    public static boolean isTeleportAllowed(OfflinePlayer p) {
        return PlayerConfigurationManager.getConfiguration(p).getBoolean("allow_tp", true);
    }

    /**
     * Checks to see if the timestamp is greater than the current time.
     *
     * @param p     OfflinePlayer to check for
     * @param title Path of timestamp to check
     * @return true if the timestamp has not been passed, false if otherwise
     */
    public static boolean isTimeStampValid(OfflinePlayer p, String title) {
        PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(p);
        if (!pcm.isSet(title)) return false;
        long time = System.currentTimeMillis();
        long overall = pcm.getLong(title);
        return time < overall;
    }

    public static boolean isTimeStampValidAddTime(OfflinePlayer p, String timestamp, String timeset) {
        PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(p);
        if (pcm.get(timestamp) == null || pcm.get(timeset) == null) return false;
        long time = new Date().getTime();
        long overall = (pcm.getLong(timestamp) * 1000L) + pcm.getLong(timeset);
        return time < overall;
    }

    public static String join(Iterable<String> i, String between) {
        String ret = "";
        for (String s : i) ret = ("".equals(ret)) ? ret.concat(s) : ret.concat(between + s);
        return ret;
    }

    public static String join(String[] i, String between) {
        String ret = "";
        for (String s : i) ret = ("".equals(ret)) ? ret.concat(s) : ret.concat(between + s);
        return ret;
    }

    public static String join(Object[] i, String between) {
        String ret = "";
        for (Object o : i)
            ret = ("".equals(ret)) ? ret.concat(o.toString().toLowerCase()) : ret.concat(between + o.toString().toLowerCase());
        return ret;
    }

    /**
     * Kicks a player and sets the last kick for history writing.
     *
     * @param kicked Player to kick
     * @param reason Reason for kick
     */
    public static void kickPlayer(final Player kicked, final CommandSender kicker, final String reason) {
        final PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(kicked);
        pcm.set("last_kick.kicker", (kicker == null) ? "Unknown" : kicker.getName());
        pcm.set("last_kick.reason", decolorize(reason));
        pcm.set("last_kick.timestamp", System.currentTimeMillis());
        kicked.kickPlayer(RUtils.getMessage(Config.kickFormat, reason, kicker));
    }

    /**
     * Lists files in a directory.
     *
     * @param f         Directory to list files in
     * @param recursive Recursively list files?
     * @return List of files - never null
     */
    public static List<File> listFiles(File f, boolean recursive) {
        List<File> fs = new ArrayList<>();
        if (!f.isDirectory()) return fs;
        File[] listed = f.listFiles();
        if (listed == null) return fs;
        for (File in : listed) {
            if (in.isDirectory()) {
                if (!recursive) continue;
                fs.addAll(listFiles(in, true));
                continue;
            }
            fs.add(in);
        }
        return fs;
    }

    /**
     * Makes a scheduled Bukkit task for watching a player when he's warming up for teleport.
     *
     * @param p Player to teleport when warmup is finished
     * @param t Location to teleport to when warmup is finished
     * @return ID of Bukkit task
     */
    private static int makeTeleportRunner(final Player p, final Location t) {
        synchronized (teleRunners) {
            if (teleRunners.containsKey(p.getName())) cancelTeleportRunner(p);
        }
        p.sendMessage(MessageColor.POSITIVE + "Please wait " + MessageColor.NEUTRAL + Config.teleportWarmup + MessageColor.POSITIVE + " seconds for your teleport.");
        final PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(p);
        pcm.set("teleport_warmup", new Date().getTime());
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                long l = pcm.getLong("teleport_warmup", -1);
                if (l < 0) {
                    cancelTeleportRunner(p);
                    return;
                }
                int toAdd = Config.teleportWarmup * 1000;
                l = l + toAdd;
                long c = new Date().getTime();
                if (l < c) {
                    p.sendMessage(MessageColor.POSITIVE + "Teleporting...");
                    teleAllowed.add(p.getName());
                    String error = teleport(p, t);
                    teleAllowed.remove(p.getName());
                    if (!error.isEmpty()) p.sendMessage(MessageColor.NEGATIVE + error);
                    cancelTeleportRunner(p);
                }
            }
        };
        int id = Bukkit.getScheduler().scheduleSyncRepeatingTask(RoyalCommands.getInstance(), r, 0, 10);
        synchronized (teleRunners) {
            teleRunners.put(p.getName(), id);
        }
        return id;
    }

    /**
     * Makes a scheduled Bukkit task for watching a player when he's warming up for teleport.
     *
     * @param p Player to teleport when warmup is finished
     * @param t Entity to teleport to when warmup is finished
     * @return ID of Bukkit task
     */
    private static int makeTeleportRunner(final Player p, final Entity t) {
        return makeTeleportRunner(p, t.getLocation());
    }

    public static boolean nearEqual(double a, double b, double margin) {
        return Math.abs(a - b) < margin;
    }

    public static boolean nearEqual(double a, double b) {
        return nearEqual(a, b, .05D);
    }

    /**
     * Plays the configured teleport sound at a location.
     *
     * @param at Location to play sound at
     */
    public static void playTeleportSound(Location at) {
        if (at == null) throw new IllegalArgumentException("Location cannot be null!");
        if (!Config.teleportSoundEnabled) return;
        Sound toPlay;
        try {
            toPlay = Sound.valueOf(Config.teleportSoundName);
        } catch (IllegalArgumentException e) {
            RoyalCommands.getInstance().getLogger().warning("A teleport sound was attempted, but " + Config.teleportSoundName + " was not a valid sound name!");
            return;
        }
        at.getWorld().playSound(at, toPlay, Config.teleportSoundVolume, Config.teleportSoundPitch);
    }

    public static void removeAssignment(ItemStack is, GeneralConfiguration gcf) {
        setAssignment(is, null, gcf);
    }

    /**
     * Renames an ItemStack.
     *
     * @param is      ItemStack to rename
     * @param newName Name to give ItemStack
     * @return ItemStack with new name
     */
    public static ItemStack renameItem(ItemStack is, String newName) {
        if (is == null) throw new IllegalArgumentException("ItemStack cannot be null!");
        if (newName == null) return is;
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(newName);
        is.setItemMeta(im);
        return is;
    }

    public static String replaceVars(final String orig, final Player p) {
        String repld = orig;
        repld = repld.replace("{name}", p.getName()).replace("{dispname}", p.getDisplayName()).replace("{world}", getMVWorldName(p.getWorld()));
        if (!RoyalCommands.getInstance().vh.usingVault()) return repld;
        try {
            repld = repld.replace("{group}", RoyalCommands.getInstance().vh.getPermission().getPrimaryGroup(p));
        } catch (Exception ignored) {
        }
        try {
            repld = repld.replace("{prefix}", RoyalCommands.getInstance().vh.getChat().getPlayerPrefix(p));
        } catch (Exception ignored) {
            String prefix = getRChatPrefix(p);
            if (prefix != null) repld = repld.replace("{prefix}", prefix);
        }
        try {
            repld = repld.replace("{suffix}", RoyalCommands.getInstance().vh.getChat().getPlayerSuffix(p));
        } catch (Exception ignored) {
            String suffix = getRChatSuffix(p);
            if (suffix != null) repld = repld.replace("{suffix}", suffix);
        }
        return repld;
    }

    /**
     * Saves player backpacks in a forwards-compatible method, using native Bukkit methods.
     *
     * @param p Player to save backpack for
     * @param i Inventory to save as backpack
     */
    public static void saveBackpack(Player p, Inventory i) {
        saveBackpack(p.getUniqueId(), p.getWorld(), i);
    }

    /**
     * Saves player backpacks in a forwards-compatible method, using native Bukkit methods.
     *
     * @param u UUID of player to save backpack for
     * @param i Inventory to save as backpack
     */
    public static void saveBackpack(UUID u, World w, Inventory i) {
        final PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(u);
        if (w == null) return;
        String worldGroup = WorldManager.il.getWorldGroup(w);
        if (worldGroup == null) worldGroup = "w-" + w.getName();
        for (int slot = 0; slot < i.getSize(); slot++)
            pcm.set("backpack." + worldGroup + ".item." + slot, i.getItem(slot));
        pcm.set("backpack." + worldGroup + ".size", i.getSize());
    }

    /**
     * Schedules a player kick via the Bukkit scheduler. Will run as soon as a spot frees for the event.
     *
     * @param p      Player to kick
     * @param reason Reason for kick
     * @throws IllegalArgumentException If p or reason is null
     * @throws NullPointerException     If method could not get the RoyalCommands plugin to schedule with via Bukkit
     */
    public static void scheduleKick(final Player p, final String reason) throws IllegalArgumentException, NullPointerException {
        if (p == null) throw new IllegalArgumentException("Player cannot be null!");
        if (reason == null) throw new IllegalArgumentException("Reason cannot be null!");
        Runnable r = new Runnable() {
            @Override
            public void run() {
                p.kickPlayer(reason);
            }
        };
        Plugin plugin = RoyalCommands.getInstance();
        if (plugin == null) throw new NullPointerException("Could not get the RoyalCommands plugin.");
        RoyalCommands.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(plugin, r);
    }

    public static void setAssignment(ItemStack is, List<String> commands, GeneralConfiguration gcf) {
        if (is == null) return;
        gcf.set(getAssignmentPath(is), commands);
    }

    public static void setFields(Object o, String[] fields, Object... values) throws ReflectiveOperationException {
        try {
            for (int index = 0; index < fields.length; index++) {
                final String field = fields[index];
                final Field f = o.getClass().getDeclaredField(field);
                f.setAccessible(true);
                f.set(o, values[index]);
            }
        } catch (ReflectiveOperationException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Sets the holder of an Inventory
     *
     * @param i  Inventory to set holder of
     * @param ih InventoryHolder to be set
     * @return New inventory with updated holder
     */
    public static Inventory setHolder(Inventory i, InventoryHolder ih) {
        Inventory ii = createInv(ih, i.getSize(), "");
        ii.setContents(i.getContents());
        return ii;
    }

    public static ItemStack setItemStackSpawned(ItemStack stack, String spawner, boolean spawned) {
        final SpawnInfo si = SpawnInfo.SpawnInfoManager.getSpawnInfo(stack);
        si.setSpawner(spawner);
        si.setSpawned(spawned);
        return SpawnInfo.SpawnInfoManager.applySpawnInfo(stack, si);
    }

    /**
     * Sets a timestamp in a player's userdata file
     *
     * @param p       OfflinePlayer to set the timestamp on
     * @param seconds Seconds relative to the current time for timestamp
     * @param title   Path to timestamp
     */
    public static void setTimeStamp(OfflinePlayer p, long seconds, String title) {
        PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(p);
        pcm.set(title, (seconds * 1000) + new Date().getTime());
    }

    /**
     * Shows a temporary empty chest to the player
     *
     * @param player Player to show chest to
     */
    public static void showEmptyChest(Player player) {
        player.openInventory(Bukkit.createInventory(null, InventoryType.CHEST));
    }

    /**
     * Shows a temporary empty chest to the player
     *
     * @param p    Player to show chest to
     * @param name Name of chest
     */
    public static void showEmptyChest(Player p, String name) {
        p.openInventory(Bukkit.createInventory(null, InventoryType.CHEST.getDefaultSize(), name));
    }

    /**
     * Shows a chest filled of an item to player
     *
     * @param p    Player to show chest to
     * @param name Name of item to fill chest with
     */
    public static void showFilledChest(Player p, String name) {
        Inventory inv = Bukkit.createInventory(null, InventoryType.CHEST);
        ItemStack stack = getItem(name, 64);
        for (int i = 0; i < inv.getSize(); i++) inv.addItem(stack);
        p.openInventory(inv);
    }

    public static void silentKick(final Player t, final String reason) {
        t.kickPlayer(reason + "\00-silent");
    }

    /**
     * Teleports a player without registering it in /back.
     *
     * @param p Player to teleport
     * @param l Location to teleport to
     * @return Error message if any.
     */
    @Deprecated
    public static String silentTeleport(Player p, Location l) {
        synchronized (teleRunners) {
            if (Config.teleportWarmup > 0 && !teleRunners.containsKey(p.getName()) && !RoyalCommands.getInstance().ah.isAuthorized(p, "rcmds.exempt.teleportwarmup")) {
                makeTeleportRunner(p, l);
                return "";
            } else if (Config.teleportWarmup > 0 && teleRunners.containsKey(p.getName()) && !teleAllowed.contains(p.getName())) {
                return "";
            }
        }
        final Location toTele = (Config.safeTeleport) ? getSafeLocation(l) : l;
        if (toTele == null) return "There is no ground below.";
        final Chunk c = toTele.getChunk();
        if (!c.isLoaded()) c.load(true);
        final Entity vehicle = getVehicleToTeleport(p);
        if (vehicle != null) {
            return teleportWithVehicle(toTele, p, vehicle, false);
        } else {
            p.setVelocity(new Vector(0, 0, 0));
            p.setFallDistance(0F);
            p.teleport(toTele);
            //playTeleportSound(toTele); Silent
            return "";
        }
    }

    /**
     * Teleports a player without registering it in /back.
     *
     * @param p Player to teleport
     * @param e Entity to teleport to
     * @return Error message if any.
     */
    @Deprecated
    public static String silentTeleport(Player p, Entity e) {
        if (e == null) return "Entity was null";
        return silentTeleport(p, e.getLocation());
    }

    /**
     * Teleports a player and registers it in /back.
     *
     * @param p Player to teleport
     * @param l Location to teleport to
     * @return Error message if any.
     * @deprecated Use {@link org.royaldev.royalcommands.wrappers.player.MemoryRPlayer#getTeleporter}.
     */
    @Deprecated
    public static String teleport(Player p, Location l) {
        synchronized (teleRunners) {
            if (Config.teleportWarmup > 0 && !teleRunners.containsKey(p.getName()) && !RoyalCommands.getInstance().ah.isAuthorized(p, "rcmds.exempt.teleportwarmup")) {
                makeTeleportRunner(p, l);
                return "";
            } else if (Config.teleportWarmup > 0 && teleRunners.containsKey(p.getName()) && !teleAllowed.contains(p.getName())) {
                return "";
            }
        }
        final Location toTele = (Config.safeTeleport) ? getSafeLocation(l) : l;
        if (toTele == null) return "There is no ground below.";
		if (toTele.getWorld() == null) return "That world doesn't exist.";
        final Chunk c = toTele.getChunk();
        if (!c.isLoaded()) c.load(true);
        final Entity vehicle = getVehicleToTeleport(p);
        if (vehicle != null) {
            return teleportWithVehicle(toTele, p, vehicle);
        } else {
            CmdBack.addBackLocation(p, p.getLocation());
            p.setVelocity(new Vector(0, 0, 0));
            p.setFallDistance(0F);
            p.teleport(toTele);
            playTeleportSound(toTele);
            return "";
        }
    }

    /**
     * Teleports a player and registers it in /back.
     *
     * @param p Player to teleport
     * @param e Entity to teleport to
     * @return Error message if any.
     */
    @Deprecated
    public static String teleport(Player p, Entity e) {
        if (e == null) return "Entity was null";
        return teleport(p, e.getLocation());
    }

    @Deprecated
    private static String teleportWithVehicle(Location l, Entity passenger, Entity vehicle) {
        return teleportWithVehicle(l, passenger, vehicle, false);
    }

    @Deprecated
    private static String teleportWithVehicle(Location l, Entity passenger, Entity vehicle, boolean silent) {
        if (!Config.vehicleCrossWorldTeleport && !l.getWorld().getName().equals(vehicle.getWorld().getName()))
            return "Passenger on vehicle cannot teleport to a different world!";
        vehicle.eject();
        if (!silent && passenger instanceof Player) {
            final Player p = (Player) passenger;
            CmdBack.addBackLocation(p, p.getLocation());
        }
        passenger.setVelocity(new Vector(0, 0, 0));
        passenger.setFallDistance(0F);
        passenger.teleport(l);
        vehicle.setVelocity(new Vector(0, 0, 0));
        vehicle.setFallDistance(0F);
        vehicle.teleport(l);
        vehicle.setPassenger(passenger);
        return "";
    }

    /**
     * Returns the amount of seconds from a string like "6y5d4h3m2s"
     *
     * @param format String like "5y4d3h2m1s"
     * @return -1 if no numbers or incorrect format, the number provided if no letters, and the seconds if correct format
     */
    public static int timeFormatToSeconds(String format) {
        format = format.toLowerCase();
        if (!format.contains("y") && !format.contains("d") && !format.contains("h") && !format.contains("m") && !format.contains("s")) {
            if (isInt(format)) return Integer.valueOf(format);
            return -1;
        }
        String nums = "";
        int num;
        int seconds = 0;
        for (int i = 0; i < format.length(); i++) {
            char c = format.charAt(i);
            if (isInt(c)) {
                nums += c;
                continue;
            }
            if (nums.isEmpty()) return -1; // this will happen if someone enters 5dd3h, etc. (invalid format)
            switch (c) {
                case 'y':
                    num = Integer.valueOf(nums);
                    seconds += num * 31556926;
                    nums = "";
                    break;
                case 'd':
                    num = Integer.valueOf(nums);
                    seconds += num * 86400;
                    nums = "";
                    break;
                case 'h':
                    num = Integer.valueOf(nums);
                    seconds += num * 3600;
                    nums = "";
                    break;
                case 'm':
                    num = Integer.valueOf(nums);
                    seconds += num * 60;
                    nums = "";
                    break;
                case 's':
                    num = Integer.valueOf(nums);
                    seconds += num;
                    nums = "";
                    break;
                default:
                    return -1;
            }
        }
        return seconds;
    }

    public static void unbanPlayer(OfflinePlayer t) {
        final BanList bl = Bukkit.getBanList(BanList.Type.NAME);
        bl.pardon(t.getName());
    }

    /**
     * Wraps text to fit evenly in the chat box
     *
     * @param text Text to wrap
     * @param len  Length to wrap at
     * @return Array of strings
     */
    @SuppressWarnings("unchecked")
    public static String[] wrapText(String text, int len) {
        // return empty array for null text
        if (text == null) return new String[]{};
        // return text if len is zero or less
        if (len <= 0) return new String[]{text};
        // return text if less than length
        if (text.length() <= len) return new String[]{text};
        char[] chars = text.toCharArray();
        java.util.Vector lines = new java.util.Vector();
        StringBuilder line = new StringBuilder();
        StringBuilder word = new StringBuilder();
        for (char aChar : chars) {
            word.append(aChar);
            if (aChar == ' ') {
                if ((line.length() + word.length()) > len) {
                    lines.add(line.toString());
                    line.delete(0, line.length());
                }
                line.append(word);
                word.delete(0, word.length());
            }
        }
        // handle any extra chars in current word
        if (word.length() > 0) {
            if ((line.length() + word.length()) > len) {
                lines.add(line.toString());
                line.delete(0, line.length());
            }
            line.append(word);
        }
        // handle extra line
        if (line.length() > 0) lines.add(line.toString());
        String[] ret = new String[lines.size()];
        int c = 0; // counter
        for (Enumeration e = lines.elements(); e.hasMoreElements(); c++) ret[c] = (String) e.nextElement();
        return ret;
    }

    /**
     * Writes a string containing all the vital ban information about a player to a list of previous bans in the
     * player's userdata.
     *
     * @param t Player to write ban history of
     */
    public static void writeBanHistory(OfflinePlayer t) {
        if (!t.isBanned()) return;
        PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(t);
        if (!pcm.exists()) pcm.createFile();
        List<String> prevBans = pcm.getStringList("prevbans");
        if (prevBans == null) prevBans = new ArrayList<>();
        // banner,banreason,bannedat,istempban
        prevBans.add(pcm.getString("banner") + "\u00b5" + pcm.getString("banreason") + "\u00b5" + pcm.getString("bannedat") + "\u00b5" + (pcm.get("bantime") != null));
        pcm.set("prevbans", prevBans);
    }
}
