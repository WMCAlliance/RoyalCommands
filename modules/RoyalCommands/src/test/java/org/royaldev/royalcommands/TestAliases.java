package org.royaldev.royalcommands;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.royaldev.royalcommands.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;


public class TestAliases {

    private YamlConfiguration pluginYml = null;

    private YamlConfiguration getPluginYml() {
        if (this.pluginYml == null) {
            final File pluginFile = new File("src/main/resources/plugin.yml");
            assertTrue("No plugin.yml found!", pluginFile.exists());
            this.pluginYml = YamlConfiguration.loadConfiguration(pluginFile);
        }
        return this.pluginYml;
    }

    @Test
    public void testAliases() throws Throwable {
        final Logger l = Logger.getLogger("org.royaldev.royalcommands");
        final File csv = new File("src/main/resources/items.csv");
        assertTrue("No items.csv found!", csv.exists());
        final Reader in = new FileReader(csv);
        final ItemNameManager inm = new ItemNameManager(new CSVReader(in).readAll());
        boolean allAliasesExist = true;
        for (final Material m : Material.values()) {
            if (inm.aliasExists(m)) continue;
            if (m.name().startsWith("LEGACY")) continue;
            if (allAliasesExist) allAliasesExist = false;
            l.warning("Missing alias for Material " + m.name() + ".");
        }
        assertTrue("Missing aliases!", allAliasesExist);
    }

    private RoyalCommands makeRoyalCommands() {
        final RoyalCommands rc = mock(RoyalCommands.class);
        when(rc.getLogger()).thenReturn(Logger.getAnonymousLogger());
        return rc;
    }

    @Before
    public void setUp() throws Throwable {
        TestHelpers.setInstance(this.makeRoyalCommands());
    }

    @After
    public void tearDown() throws Throwable {
        TestHelpers.clearInstance();
    }

    @Test
    public void testCommandAliases() throws Throwable {
        final ConfigurationSection reflectCommands = this.getPluginYml().getConfigurationSection("reflectcommands");
        final List<String> usedAliases = new ArrayList<>();
        for (final String key : reflectCommands.getKeys(false)) {
            final ConfigurationSection cs = reflectCommands.getConfigurationSection(key);
            final List<String> aliases = cs.getStringList("aliases");
            for (final String alias : aliases) {
                assertFalse("Alias " + alias + " is reused in command " + key + ".", usedAliases.contains(alias));
            }
            usedAliases.addAll(aliases);
        }
    }

}
