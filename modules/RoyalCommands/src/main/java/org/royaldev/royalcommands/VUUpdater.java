/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class that gets VU update data from the CurseForge API. {@link #getUpdateInfo(String)} is the main method of this
 * class.
 */
public final class VUUpdater {

    /**
     * Matches "PluginName vA.B.C (V1V2V3)" according to the spec.
     */
    private static final Pattern vuPattern = Pattern.compile("^(.+)\\s+v(\\d+(\\.\\d){2}).*\\((([0-9a-f]{2}){1,3})\\)$", Pattern.CASE_INSENSITIVE);

    /**
     * Checks for an update for the given plugin and plugin ID. This will check if the plugin version contains
     * "-SNAPSHOT" and set the updater to check for development versions if it does. See
     * {@link #checkForUpdate(org.bukkit.plugin.Plugin, String, boolean)} if that is not the behavior you desire.
     *
     * @param p        Plugin to check for an update for
     * @param pluginID ID of the plugin to get the information from. The ID comes from CurseForge.
     * @return {@link VUUpdater.UpdateStatus}
     */
    public static VUUpdater.UpdateStatus checkForUpdate(Plugin p, String pluginID) {
        final String pluginVersion = p.getDescription().getVersion();
        return VUUpdater.checkForUpdate(p, pluginID, pluginVersion.toUpperCase().contains("-SNAPSHOT"));
    }

    /**
     * Checks for an update for the given plugin and plugin ID.
     *
     * @param p        Plugin to check for an update for
     * @param pluginID ID of the plugin to get the information from. The ID comes from CurseForge.
     * @param useDev   Should development versions be checked?
     * @return {@link VUUpdater.UpdateStatus}
     */
    public static UpdateStatus checkForUpdate(Plugin p, String pluginID, boolean useDev) {
        final VUUpdateInfo vuui;
        try {
            vuui = VUUpdater.getUpdateInfo(pluginID);
        } catch (IOException | URISyntaxException e) {
            return UpdateStatus.ERROR;
        }
        final String pluginVersion = p.getDescription().getVersion();
        if (useDev && !pluginVersion.contains(vuui.getDevelopment())) return UpdateStatus.UPDATE_FOUND;
        if (!useDev && !pluginVersion.contains(vuui.getStable())) return UpdateStatus.UPDATE_FOUND;
        return UpdateStatus.NO_UPDATE;
    }

    /**
     * Gets the update information from the title of the latest file available from the CurseForge API.
     *
     * @param pluginID ID of the plugin to get the information from. The ID comes from CurseForge.
     * @return {@link org.royaldev.royalcommands.VUUpdater.VUUpdateInfo}
     * @throws IOException If any errors occur
     */
    public static VUUpdateInfo getUpdateInfo(String pluginID) throws IOException, URISyntaxException {
        final URLConnection conn = new URI("https://api.curseforge.com/servermods/files?projectIds=" + pluginID).toURL().openConnection();
        conn.setConnectTimeout(5000);
        conn.setRequestProperty("User-Agent", "VU/1.0");
        conn.setDoOutput(true);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        final String response = reader.readLine();
        final JSONArray array = (JSONArray) JSONValue.parse(response);
        final Matcher m = VUUpdater.vuPattern.matcher((String) ((JSONObject) array.get(array.size() - 1)).get("name"));
        if (!m.matches()) throw new IllegalArgumentException("No match found!");
        final byte[] vuBytes = VUUpdater.hexStringToByteArray(m.group(4));
        return new VUUpdateInfo(m.group(2), vuBytes);
    }

    /**
     * Turns a hexadecimal string into an array of bytes.
     *
     * @param s Hexadecimal string (like FF00FF00)
     * @return Byte array
     */
    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Possible statuses for the update checker to return.
     */
    public static enum UpdateStatus {
        /**
         * An error occurred getting the update information.
         */
        ERROR,
        /**
         * No update was found.
         */
        NO_UPDATE,
        /**
         * An update was found.
         */
        UPDATE_FOUND
    }

    /**
     * Class that holds and processes VU update information for plugins.
     */
    public static class VUUpdateInfo {

        /**
         * VU bytes in the title
         */
        private final byte[] vuBytes;
        /**
         * Version string.
         */
        private String stable = null, devel = null;

        /**
         * Processes VU update information based on the given parameters. After construction, {@link #getStable()} and
         * {@link #getDevelopment()} are available.
         *
         * @param versionOfFile Source version (version of the file with VU bytes on it). e.g. "1.0.0"
         * @param vuBytes       Array of VU bytes obtained from source file title
         * @throws IllegalArgumentException If the length of vuBytes is less than one.
         */
        public VUUpdateInfo(String versionOfFile, byte[] vuBytes) throws IllegalArgumentException {
            if (vuBytes.length < 1) throw new IllegalArgumentException("Not enough VU bytes");
            this.vuBytes = vuBytes;
            int vuForStb = 0, vuForDev = 0;
            if (this.hasFlag(VUFlag.VU_FOR_STABLE_FOLLOW)) {
                vuForStb = this.vuBytes[this.hasFlag(VUFlag.VU_FOR_DEV_FIRST) ? 2 : 1];
            }
            if (this.hasFlag(VUFlag.VU_FOR_DEV_FOLLOW)) {
                vuForDev = this.vuBytes[this.hasFlag(VUFlag.VU_FOR_DEV_FIRST) ? 1 : this.hasFlag(VUFlag.VU_FOR_STABLE_FOLLOW) ? 2 : 1];
            }
            this.stable = versionOfFile;
            this.devel = versionOfFile;
            if (vuForStb != 0) this.stable = vuToVersion(versionToVU(this.stable) + vuForStb);
            if (vuForDev != 0) this.devel = vuToVersion(versionToVU(this.devel) + vuForDev);
            if (this.hasFlag(VUFlag.STABLE_IS_SAME)) {
                this.stable = versionOfFile; // override previous values
            }
            if (this.hasFlag(VUFlag.DEV_IS_STABLE)) {
                this.devel = this.stable; // override previous values
            }
        }

        /**
         * Converts a version string to version units.
         * <p>
         * Converts the following:
         * </p>
         * <p>
         * <code>3.2.0</code> to <code>320</code><br>
         * <code>10.2.0</code> to <code>1020</code>
         * </p>
         *
         * @param version Version string
         * @return Version units
         */
        private int versionToVU(String version) {
            version = version.replace(".", "");
            try {
                return Integer.parseInt(version);
            } catch (NumberFormatException ex) {
                return -1;
            }
        }

        /**
         * Converts version units to a version string.
         * <p>
         * Converts the following:
         * </p>
         * <p>
         * <code>320</code> to <code>3.2.0</code><br>
         * <code>1020</code> to <code>10.2.0</code>
         * </p>
         *
         * @param vu Version units
         * @return Version string
         */
        private String vuToVersion(int vu) {
            String version = String.valueOf(vu).replaceAll(".(?!$)", "$0.");
            while (StringUtils.countMatches(version, ".") > 2) version = version.replaceFirst("\\.", "");
            return version;
        }

        /**
         * Gets the development version string obtained from the VU bytes.
         *
         * @return Development version string
         */
        public String getDevelopment() {
            return this.devel;
        }

        /**
         * Gets the stable version string obtained from the VU bytes.
         *
         * @return Stable version string
         */
        public String getStable() {
            return this.stable;
        }

        /**
         * Returns if the head byte has the given flag set.
         *
         * @param flag Flag to check
         * @return true if set, false if otherwise
         */
        public boolean hasFlag(VUFlag flag) {
            return (this.vuBytes[0] & (1 << flag.getBitNumber())) > 0;
        }

        /**
         * Flags possible to have set on the head byte of VU bytes.
         */
        private enum VUFlag {
            /**
             * Bit 7 of head VU byte.
             * <br>
             * The stable version is the same as the source version if this is set.
             */
            STABLE_IS_SAME((byte) 7),
            /**
             * Bit 6 of head VU byte.
             * <br>
             * The development version is the same as the stable version if this is set.
             */
            DEV_IS_STABLE((byte) 6),
            /**
             * Bit 5 of head VU byte.
             * <br>
             * The VU for the development version will follow this byte if this is set.
             */
            VU_FOR_DEV_FOLLOW((byte) 5),
            /**
             * Bit 4 of head VU byte.
             * <br>
             * The VU for the stable version will follow this byte if this is set.
             */
            VU_FOR_STABLE_FOLLOW((byte) 4),
            /**
             * Bit 3 of head VU byte.
             * <br>
             * The VU for the development version will appear first after this byte if this is set.
             * <br>
             * This should only be set if bit 3 or 4 are set.
             */
            VU_FOR_DEV_FIRST((byte) 3);

            /**
             * Number that would be set for this flag in the VU head byte.
             */
            private final byte bitNumber;

            /**
             * Constructs a VUFlag with the given bit number.
             *
             * @param bitNumber Bit number
             */
            VUFlag(byte bitNumber) {
                this.bitNumber = bitNumber;
            }

            /**
             * Gets the bit number that would be set for this flag.
             *
             * @return Bit number
             */
            public byte getBitNumber() {
                return bitNumber;
            }
        }

    }
}
