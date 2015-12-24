package main.java.org.trompgames.utils;

import java.io.File;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

public class SpleggMap {

	private String mapName;
	private Location spawnLoc;
	private File file;
	private FileConfiguration config;
	private World world;
	private int mapId;
	
	public SpleggMap(int mapId, FileConfiguration config, World world){
		this.mapId = mapId;
		this.config = config;
		this.world = world;
		getData();
	}
	
	private void getData(){
		mapName = config.getString("map." + mapId + ".name");
		file = new File("plugins\\WorldEdit\\schematics\\" + config.getString("map." + mapId + ".schem") + ".schematic");

		int x = config.getInt("map." + mapId + ".x");
		int y = config.getInt("map." + mapId + ".y");
		int z = config.getInt("map." + mapId + ".z");

		double yaw = config.getDouble("map." + mapId + ".yaw");
		double pitch = config.getDouble("map." + mapId + ".pitch");
		
		this.spawnLoc = new Location(world, x, y, z);
		this.spawnLoc.setYaw((float) yaw);
		this.spawnLoc.setPitch((float) pitch);
	}
	
	
	public String getMapName(){
		return mapName;
	}
	
	public Location getSpawnLoc(){
		return spawnLoc;
	}
	
	public File getFile(){
		return file;
	}
	
	public int getMapId(){
		return mapId;
	}
	
	
	
	
}
