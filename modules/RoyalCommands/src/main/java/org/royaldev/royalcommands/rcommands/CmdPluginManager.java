/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import com.google.common.io.Files;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import org.royaldev.royalcommands.rcommands.pluginmanager.SCmdDownload;
import org.royaldev.royalcommands.rcommands.pluginmanager.SCmdDownloadLink;
import org.royaldev.royalcommands.rcommands.pluginmanager.SCmdEnable;
import org.royaldev.royalcommands.rcommands.pluginmanager.SCmdFindTag;
import org.royaldev.royalcommands.rcommands.pluginmanager.SCmdInfo;
import org.royaldev.royalcommands.rcommands.pluginmanager.SCmdList;
import org.royaldev.royalcommands.rcommands.pluginmanager.SCmdLoad;
import org.royaldev.royalcommands.rcommands.pluginmanager.SCmdReload;
import org.royaldev.royalcommands.rcommands.pluginmanager.SCmdReloadAll;
import org.royaldev.royalcommands.rcommands.pluginmanager.SCmdUnload;
import org.royaldev.royalcommands.rcommands.pluginmanager.SCmdUpdate;
import org.royaldev.royalcommands.rcommands.pluginmanager.SCmdUpdateCheck;
import org.royaldev.royalcommands.rcommands.pluginmanager.SCmdUpdateCheckAll;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@ReflectCommand
public class CmdPluginManager extends ParentCommand {

    public CmdPluginManager(final RoyalCommands instance, final String name) {
        super(instance, name, true);
        this.addSubCommand(new SCmdCommands(this.plugin, this));
        this.addSubCommand(new SCmdDelete(this.plugin, this));
        this.addSubCommand(new SCmdDisable(this.plugin, this));
        //this.addSubCommand(new SCmdDownload(this.plugin, this));
        this.addSubCommand(new SCmdDownloadLink(this.plugin, this));
        this.addSubCommand(new SCmdEnable(this.plugin, this));
        //this.addSubCommand(new SCmdFindTag(this.plugin, this));
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
            huc = (HttpURLConnection) new URL(url).openConnection();
            huc.setInstanceFollowRedirects(true);
            huc.connect();
            bis = new BufferedInputStream(huc.getInputStream());
        } catch (MalformedURLException e) {
            cs.sendMessage(MessageColor.NEGATIVE + "The download link is invalid!");
            return false;
        } catch (IOException e) {
            cs.sendMessage(MessageColor.NEGATIVE + "An internal input/output error occurred. Please try again. (" + MessageColor.NEUTRAL + e.getMessage() + MessageColor.NEGATIVE + ")");
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
        if (cs == null) return null;
        for (String key : cs.getKeys(false)) {
            if (!key.equalsIgnoreCase(name)) continue;
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

    public String updateCheck(String name, String currentVersion) throws Exception {
        String tag = this.getCustomTag(name);
        if (tag == null) tag = name.toLowerCase();
        String pluginUrlString = "https://api.spigotmc.org/simple/0.2/index.php?action=getResource&id=" + tag;
        URL url = new URL(pluginUrlString);
        URLConnection request = url.openConnection();
        request.connect();
        JsonParser jp = new JsonParser();
        JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
        JsonObject rootobj = root.getAsJsonObject();
        if (rootobj.has("current_version")) {
            return rootobj.get("current_version").getAsString();
        }
        return currentVersion;
    }
}
