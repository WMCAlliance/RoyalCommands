/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import com.google.common.io.Files;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.UnZip;
import org.royaldev.royalcommands.rcommands.pluginmanager.SCmdCommands;
import org.royaldev.royalcommands.rcommands.pluginmanager.SCmdDelete;
import org.royaldev.royalcommands.rcommands.pluginmanager.SCmdDisable;
import org.royaldev.royalcommands.rcommands.pluginmanager.SCmdDownloadLink;
import org.royaldev.royalcommands.rcommands.pluginmanager.SCmdEnable;
import org.royaldev.royalcommands.rcommands.pluginmanager.SCmdInfo;
import org.royaldev.royalcommands.rcommands.pluginmanager.SCmdList;
import org.royaldev.royalcommands.rcommands.pluginmanager.SCmdLoad;
import org.royaldev.royalcommands.rcommands.pluginmanager.SCmdReload;
import org.royaldev.royalcommands.rcommands.pluginmanager.SCmdReloadAll;
import org.royaldev.royalcommands.rcommands.pluginmanager.SCmdUnload;
import org.royaldev.royalcommands.rcommands.pluginmanager.SCmdUpdate;
import org.royaldev.royalcommands.rcommands.pluginmanager.SCmdUpdateCheck;
import org.royaldev.royalcommands.rcommands.pluginmanager.SCmdUpdateCheckAll;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ReflectCommand
public class CmdPluginManager extends ParentCommand {

    public CmdPluginManager(final RoyalCommands instance, final String name) {
        super(instance, name, true);
        this.addSubCommand(new SCmdCommands(this.plugin, this));
        this.addSubCommand(new SCmdDelete(this.plugin, this));
        this.addSubCommand(new SCmdDisable(this.plugin, this));
        this.addSubCommand(new SCmdDownloadLink(this.plugin, this));
        this.addSubCommand(new SCmdEnable(this.plugin, this));
        this.addSubCommand(new SCmdInfo(this.plugin, this));
        this.addSubCommand(new SCmdList(this.plugin, this));
        this.addSubCommand(new SCmdLoad(this.plugin, this));
        this.addSubCommand(new SCmdReload(this.plugin, this));
        this.addSubCommand(new SCmdReloadAll(this.plugin, this));
        this.addSubCommand(new SCmdUnload(this.plugin, this));
        this.addSubCommand(new SCmdUpdate(this.plugin, this));
        this.addSubCommand(new SCmdUpdateCheck(this.plugin, this));
        this.addSubCommand(new SCmdUpdateCheckAll(this.plugin, this));
    }

    /**
     * Gets the names of all plugins that depend on the specified plugin.
     * <br>
     * This will not return plugins that are disabled.
     *
     * @param name Plugin name to find dependencies of
     * @return List of dependencies, may be empty - never null
     */
    private List<String> getDependedOnBy(String name) {
        final List<String> dependedOnBy = new ArrayList<>();
        for (Plugin pl : this.plugin.getServer().getPluginManager().getPlugins()) {
            if (pl == null) continue;
            if (!pl.isEnabled()) continue;
            PluginDescriptionFile pdf = pl.getDescription();
            if (pdf == null) continue;
            List<String> depends = pdf.getDepend();
            if (depends == null) continue;
            for (String depend : depends) if (name.equalsIgnoreCase(depend)) dependedOnBy.add(pl.getName());
        }
        return dependedOnBy;
    }

    public boolean downloadAndMovePlugin(String url, String saveAs, boolean recursive, CommandSender cs) {
        if (saveAs == null) saveAs = "";
        BufferedInputStream bis;
        final HttpURLConnection huc;
        try {
            huc = (HttpURLConnection) new URI(url).toURL().openConnection();
            huc.setInstanceFollowRedirects(true);
            huc.connect();
            bis = new BufferedInputStream(huc.getInputStream());
        } catch (MalformedURLException e) {
            cs.sendMessage(MessageColor.NEGATIVE + "The download link is invalid!");
            return false;
        } catch (IOException e) {
            cs.sendMessage(MessageColor.NEGATIVE + "An internal input/output error occurred. Please try again. (" + MessageColor.NEUTRAL + e.getMessage() + MessageColor.NEGATIVE + ")");
            return false;
        } catch (URISyntaxException e) {
            cs.sendMessage(MessageColor.NEGATIVE + "An unthinkable error happened. Please let the developer know.");
            return false;
        }
        String[] urlParts = huc.getURL().toString().split("(\\\\|/)");
        final String fileName = (!saveAs.isEmpty()) ? saveAs : urlParts[urlParts.length - 1];
        cs.sendMessage(MessageColor.POSITIVE + "Creating temporary folder...");
        File f = new File(System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID().toString() + File.separator + fileName);
        while (f.getParentFile().exists()) // make sure we get our own directory
            f = new File(System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID().toString() + File.separator + fileName);
        if (!fileName.endsWith(".zip") && !fileName.endsWith(".jar")) {
            cs.sendMessage(MessageColor.NEGATIVE + "The file wasn't a zip or jar file, so it was not downloaded.");
            cs.sendMessage(MessageColor.NEGATIVE + "Filename: " + MessageColor.NEUTRAL + fileName);
            return false;
        }
        //noinspection ResultOfMethodCallIgnored
        f.getParentFile().mkdirs();
        BufferedOutputStream bos;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(f));
        } catch (FileNotFoundException e) {
            cs.sendMessage(MessageColor.NEGATIVE + "The temporary download folder was not found. Make sure that " + MessageColor.NEUTRAL + System.getProperty("java.io.tmpdir") + MessageColor.NEGATIVE + " is writable.");
            return false;
        }
        int b;
        cs.sendMessage(MessageColor.POSITIVE + "Downloading file " + MessageColor.NEUTRAL + fileName + MessageColor.POSITIVE + "...");
        try {
            try {
                while ((b = bis.read()) != -1) bos.write(b);
            } finally {
                bos.flush();
                bos.close();
            }
        } catch (IOException e) {
            cs.sendMessage(MessageColor.NEGATIVE + "An internal input/output error occurred. Please try again. (" + MessageColor.NEUTRAL + e.getMessage() + MessageColor.NEGATIVE + ")");
            return false;
        }
        if (fileName.endsWith(".zip")) {
            cs.sendMessage(MessageColor.POSITIVE + "Decompressing zip...");
            UnZip.decompress(f.getAbsolutePath(), f.getParent());
        }
        for (File fi : RUtils.listFiles(f.getParentFile(), recursive)) {
            if (!fi.getName().endsWith(".jar")) continue;
            cs.sendMessage(MessageColor.POSITIVE + "Moving " + MessageColor.NEUTRAL + fi.getName() + MessageColor.POSITIVE + " to plugins folder...");
            try {
                Files.move(fi, new File(this.plugin.getDataFolder().getParentFile() + File.separator + fi.getName()));
            } catch (IOException e) {
                cs.sendMessage(MessageColor.NEGATIVE + "Couldn't move " + MessageColor.NEUTRAL + fi.getName() + MessageColor.NEGATIVE + ": " + MessageColor.NEUTRAL + e.getMessage());
            }
        }
        cs.sendMessage(MessageColor.POSITIVE + "Removing temporary folder...");
        RUtils.deleteDirectory(f.getParentFile());
        return true;
    }

    public String getCustomTag(String name) {
        ConfigurationSection cs = this.plugin.getConfig().getConfigurationSection("pluginmanager.custom_tags");
        if (cs == null)
            return null;
        for (String key : cs.getKeys(false)) {
            if (!key.equalsIgnoreCase(name))
                continue;
            return cs.getString(key);
        }
        return null;
    }

    public int getSpigotTag(String name) {
        // Spigot only supports numerical IDs
        ConfigurationSection cs = this.plugin.getConfig().getConfigurationSection("pluginmanager.spigot_tags");
        if (cs == null)
            return 0;
        for (String key : cs.getKeys(false)) {
            if (!key.equalsIgnoreCase(name))
                continue;
            return cs.getInt(key);
        }
        return 0;
    }

    public String getCurseforgeTag(String name) {
        ConfigurationSection cs = this.plugin.getConfig().getConfigurationSection("pluginmanager.curseforge_tags");
        if (cs == null)
            return null;
        for (String key : cs.getKeys(false)) {
            if (!key.equalsIgnoreCase(name))
                continue;
            return cs.getString(key);
        }
        return null;
    }

    public String getGithubTag(String name) {
        ConfigurationSection cs = this.plugin.getConfig().getConfigurationSection("pluginmanager.github_tags");
        if (cs == null)
            return null;
        for (String key : cs.getKeys(false)) {
            if (!key.equalsIgnoreCase(name))
                continue;
            return cs.getString(key);
        }
        return null;
    }

    public String getModrinthTag(String name) {
        // Modrinth IDs and and Slugs are alphanumeric strings
        ConfigurationSection cs = this.plugin.getConfig().getConfigurationSection("pluginmanager.modrinth_tags");
        if (cs == null)
            return null;
        for (String key : cs.getKeys(false)) {
            if (!key.equalsIgnoreCase(name))
                continue;
            return cs.getString(key);
        }
        return null;
    }

    /**
     * Gets the names of all plugins that depend on the specified plugin.
     * <br>
     * This will not return plugins that are disabled.
     *
     * @param dep Plugin to find dependencies of
     * @return List of dependencies, may be empty - never null
     */
    public List<String> getDependedOnBy(Plugin dep) {
        return getDependedOnBy(dep.getName());
    }

    public List<String> getMatchingJarNames(String arg) {
        final List<String> completions = new ArrayList<>();
        for (final String name : this.plugin.getDataFolder().getParentFile().list()) {
            final String lowerCase = name.toLowerCase();
            if (!lowerCase.endsWith(".jar") || !lowerCase.startsWith(arg)) continue;
            completions.add(lowerCase.equals(arg) ? 0 : completions.size(), name);
        }
        return completions;
    }

    public void removePluginFromList(Plugin p) {
        try {
            @SuppressWarnings("unchecked")
            final List<Plugin> plugins = (List<Plugin>) RUtils.getPrivateField(this.plugin.getServer().getPluginManager(), "plugins");
            plugins.remove(p);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unregisterAllPluginCommands(String pluginName) {
        try {
            Object result = RUtils.getPrivateField(this.plugin.getServer().getPluginManager(), "commandMap");
            SimpleCommandMap commandMap = (SimpleCommandMap) result;
            Object map = RUtils.getPrivateFieldSuper(commandMap, "knownCommands");
            @SuppressWarnings("unchecked") HashMap<String, Command> knownCommands = (HashMap<String, Command>) map;
            final List<Command> commands = new ArrayList<>(commandMap.getCommands());
            for (Command c : commands) {
                if (!(c instanceof PluginCommand)) continue;
                final PluginCommand pc = (PluginCommand) c;
                if (!pc.getPlugin().getName().equals(pluginName)) continue;
                knownCommands.remove(pc.getName());
                for (String alias : pc.getAliases()) {
                    if (knownCommands.containsKey(alias)) {
                        final Command ac = knownCommands.get(alias);
                        if (!(ac instanceof PluginCommand)) continue;
                        final PluginCommand apc = (PluginCommand) ac;
                        if (!apc.getPlugin().getName().equals(pluginName)) continue;
                        knownCommands.remove(alias);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String parseSpigotUpdate(int tag, String currentVersion) throws Exception {
        String pluginUrlString = "https://api.spigotmc.org/simple/0.2/index.php?action=getResource&id=" + tag;
        URL url = new URI(pluginUrlString).toURL();
        URLConnection request = url.openConnection();
        request.connect();
        JSONParser jp = new JSONParser();
        JSONObject rootObj = (JSONObject) jp.parse(new InputStreamReader((InputStream) request.getContent()));
        if (rootObj.containsKey("current_version")) {
            return (String) rootObj.get("current_version");
        }
        return currentVersion;
    }

    public String parseCurseforgeUpdate(String tag, String currentVersion) throws Exception {
        ConfigurationSection cs = this.plugin.getConfig().getConfigurationSection("pluginmanager.curseforge_api");
        String apiKey = cs.getString("api_key");
        if (!cs.getBoolean("enabled", false) || apiKey.isEmpty())
            throw new RuntimeException("CurseForge API support disabled or no API key provided");

        int projectId = 0;
        try {
            projectId = Integer.parseInt(tag);
        } catch (NumberFormatException e) {}

        String pluginUrlString = "https://api.curseforge.com/v1/mods/";
        if (projectId > 0) {
            // Project ID is preferred and most reliable, visible on CurseForge
            pluginUrlString += projectId;
        } else {
            int gameId = cs.getInt("game_id", 432);
            int classId = cs.getInt("class_id", 5);
            if (gameId == 0 || classId == 0)
                throw new RuntimeException("Invalid CurseForge game or class ID");

            pluginUrlString += "search" + "?gameId=" + gameId + "&classId=" + classId + "&slug=" + tag;
        }

        URL url = new URI(pluginUrlString).toURL();
        URLConnection request = url.openConnection();
        request.setRequestProperty("x-api-key", apiKey);
        request.connect();

        JSONParser jp = new JSONParser();
        JSONObject rootObj = (JSONObject) jp.parse(new InputStreamReader((InputStream) request.getContent()));

        JSONObject obj = null;
        if (projectId > 0) {
            obj = (JSONObject) rootObj.get("data");
        } else {
            JSONArray data = (JSONArray) rootObj.get("data");
            obj = (JSONObject) data.getFirst();
        }

        JSONArray files = (JSONArray) obj.get("latestFiles");
        JSONObject latest = (JSONObject) files.getFirst();

        String dispName = (String) latest.get("displayName");
        String semanticVersionRegex = "(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-([0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?(?:\\+([0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?";
        Pattern pattern = Pattern.compile(semanticVersionRegex);
        Matcher matcher = pattern.matcher(dispName);

        if (matcher.find()) {
            return matcher.group();
        } else {
            return dispName;
        }
    }

    public String parseModrinthUpdate(String tag, String currentVersion) throws Exception {
        String pluginUrlString = "https://api.modrinth.com/v2/project/" + tag + "/version";
        URL url = new URI(pluginUrlString).toURL();
        URLConnection request = url.openConnection();
        request.setRequestProperty("User-Agent", "wmcalliance/royalcommands");
        request.connect();
        JSONParser jp = new JSONParser();
        JSONArray rootObj = (JSONArray) jp.parse(new InputStreamReader((InputStream) request.getContent()));
        if (!rootObj.isEmpty() && ((JSONObject)rootObj.getFirst()).containsKey("version_number")) {
            return (String) ((JSONObject)rootObj.getFirst()).get("version_number");
        }
        return currentVersion;
    }

    public String parseGithubUpdate(String tag, String currentVersion) throws Exception {
        String pluginUrlString = "https://api.github.com/repos/" + tag + "/releases";
        URL url = new URI(pluginUrlString).toURL();
        URLConnection request = url.openConnection();
        request.setRequestProperty("User-Agent", "wmcalliance/royalcommands");
        request.connect();
        JSONParser jp = new JSONParser();
        JSONArray rootObj = (JSONArray) jp.parse(new InputStreamReader((InputStream) request.getContent()));
        if (!rootObj.isEmpty() && ((JSONObject)rootObj.getFirst()).containsKey("tag_name")) {
            return (String) ((JSONObject)rootObj.getFirst()).get("tag_name");
        }
        return currentVersion;
    }

    public String updateCheck(String name, String currentVersion) throws Exception {
        /**
         * Check spigot_tags first, which must be numeric
         * Check curseforge_tags second, which are best numeric (Project ID), but may be a slug
         * Check custom_tags, which are either numeric (therefore Spigot) or CurseForge slug. custom_tags is legacy and not included in the default config file.
         */
        int spigotTag = this.getSpigotTag(name);
        if (spigotTag != 0)
            return this.parseSpigotUpdate(spigotTag, currentVersion);

        String cursforgeTag = this.getCurseforgeTag(name);
        if (cursforgeTag != null && !cursforgeTag.isEmpty())
            return this.parseCurseforgeUpdate(cursforgeTag, currentVersion);

        String modrinthTag = this.getModrinthTag(name);
        if (modrinthTag != null && !modrinthTag.isEmpty()) {
            return this.parseModrinthUpdate(modrinthTag, currentVersion);
        }

        String githubTag = this.getGithubTag(name);
        if (githubTag != null && !githubTag.isEmpty()) {
            return this.parseGithubUpdate(githubTag, currentVersion);
        }

        String customTag = this.getCustomTag(name);
         if (customTag != null) {
            // Legacy fallback. If it's numeric, check Spigot. If it's not, check Bukkit/CurseForge
            try {
                spigotTag = Integer.parseInt(customTag);
                if (spigotTag != 0)
                    return this.parseSpigotUpdate(spigotTag, currentVersion);
            } catch (NumberFormatException e) {
                if (!customTag.isEmpty())
                    return this.parseCurseforgeUpdate(customTag.toLowerCase(), currentVersion);
            }
        }

        return currentVersion;
    }
}
