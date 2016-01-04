package main.java.org.trompgames.utils;

import java.io.File;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import main.java.org.trompgames.splegg.SpleggMain;

public class SpleggMap {

	private String mapName;
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
		file = new File("plugins/WorldEdit/schematics/" + config.getString("map." + mapId + ".schem") + ".schematic");
	}
	
	public void loadMap(SpleggMain plugin, Location loc){
		Schematic.loadArea(plugin, world, file, loc, false);
	}	
	
	public String getMapName(){
		return mapName;
	}
	
	
	
	public File getFile(){
		return file;
	}
	
	public int getMapId(){
		return mapId;
	}
	
	
	
	
}
