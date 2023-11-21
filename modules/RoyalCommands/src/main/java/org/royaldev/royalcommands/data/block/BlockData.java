/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.data.block;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class BlockData {

    private static final Map<String, Map<String, Object>> data = new HashMap<>();
    private final Plugin p;
    private final BlockLocation bl;
    private final Map<String, Object> thisData = new HashMap<>();

    public BlockData(Plugin p, BlockLocation bl) {
        this.p = p;
        this.bl = bl;
        synchronized (BlockData.data) {
            if (BlockData.data.isEmpty()) this.caughtLoad();
            synchronized (this.thisData) {
                if (BlockData.data.containsKey(bl.toString())) this.thisData.putAll(BlockData.data.get(bl.toString()));
            }
        }
    }

    public boolean caughtLoad() {
        try {
            this.load();
            return true;
        } catch (EOFException ignored) {
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean caughtSave() {
        try {
            this.save();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean contains(String key) {
        synchronized (this.thisData) {
            return this.thisData.containsKey(key);
        }
    }

    public Object get(String key) {
        synchronized (this.thisData) {
            return this.thisData.get(key);
        }
    }

    @SuppressWarnings("unchecked")
    public void load() throws IOException, ClassNotFoundException {
        final File f = new File(this.p.getDataFolder(), "blockdata.dat");
        if (!f.exists()) return;
        final ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(f)));
        final Object o = ois.readObject();
        ois.close();
        if (!(o instanceof Map)) return;
        final HashMap<String, Map<String, Object>> d = (HashMap<String, Map<String, Object>>) o;
        synchronized (BlockData.data) {
            BlockData.data.clear();
            BlockData.data.putAll(d);
        }
        synchronized (this.thisData) {
            this.thisData.clear();
            if (BlockData.data.containsKey(bl.toString())) this.thisData.putAll(BlockData.data.get(this.bl.toString()));
        }
    }

    public void remove(String key) {
        synchronized (this.thisData) {
            this.thisData.remove(key);
        }
    }

    public void save() throws IOException {
        final File f = new File(this.p.getDataFolder(), "blockdata.dat");
        if (!f.exists()) if (!f.createNewFile()) throw new IOException("Couldn't create blockdata.dat");
        BlockData.data.put(this.bl.toString(), this.thisData);
        final ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(new File(this.p.getDataFolder(), "blockdata.dat"))));
        synchronized (BlockData.data) {
            oos.writeObject(BlockData.data);
        }
        oos.flush();
        oos.close();
    }

    public void set(String key, Object value) {
        synchronized (this.thisData) {
            this.thisData.put(key, value);
        }
    }

    public static class BlockLocation implements Serializable {

        private static final long serialVersionUID = 3232014L;

        private String world;
        private int x;
        private int y;
        private int z;

        public BlockLocation(String world, int x, int y, int z) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public BlockLocation(Location l) {
            this.world = l.getWorld().getName();
            this.x = l.getBlockX();
            this.y = l.getBlockY();
            this.z = l.getBlockZ();
        }

        public String getWorld() {
            return this.world;
        }

        public void setWorld(String world) {
            this.world = world;
        }

        public int getX() {
            return this.x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return this.y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getZ() {
            return this.z;
        }

        public void setZ(int z) {
            this.z = z;
        }

        @Override
        public String toString() {
            return String.format("BL:%s,%s,%s,%s", this.world, this.x, this.y, this.z);
        }
    }

}
