package org.royaldev.royalcommands.rcommands.back;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;
import org.royaldev.royalcommands.RUtils;


/**
 * A class representing an item in the /back history.
 */
public class Back {

    private final Location loc;
    private final Vector coordinates = new Vector();
    private final String world;
    private final String biome;
	private final String block;

	/**
	 * Create a cached location item. Stores key data to improve performance when fetching.
	 * Turns most data into strings (with formatting and styling) and coordinates into a vector.
	 * 
	 * @param location The data to cache
	 */
    public Back(final Location location) {
        this.loc = location;
		this.coordinates.copy(location.toVector());
		this.world = RUtils.getMVWorldName(location.getWorld());
        Block b = location.getBlock().getRelative(BlockFace.DOWN);
		this.biome = RUtils.getFriendlyEnumName(b.getBiome());
		this.block = RUtils.getItemName(b.getType());
    }
	
	/**
	 * Only use this function when preparing for teleport, as it has a performance impact.
	 * @return Original Location class.
	 */
	public Location getLoc() {
		return this.loc;
	}
	
	/**
	 * 
	 * @return Cached Vector's X coordinate.
	 */
	public double getX() {
		return this.coordinates.getX();
	}
	
	/**
	 * 
	 * @return Cached Vector's Y coordinate.
	 */
	public double getY() {
		return this.coordinates.getY();
	}
	
	/**
	 * 
	 * @return Cached Vector's Z coordinate.
	 */
	public double getZ() {
		return this.coordinates.getZ();
	}
	
	/**
	 * 
	 * @return Cached world name, formatted and styled
	 */
	public String getWorldName() {
		return this.world;
	}
	
	/**
	 * 
	 * @return Cached friendly enum biome name.
	 */
	public String getBiomeName() {
		return this.biome;
	}
	
	/**
	 * 
	 * @return Cached block the player was standing on.
	 */
	public String getBlockName() {
		return this.block;
	}
}
