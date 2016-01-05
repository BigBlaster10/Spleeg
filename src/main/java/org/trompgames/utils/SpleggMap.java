package main.java.org.trompgames.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;

import main.java.org.trompgames.splegg.SpleggMain;
import net.md_5.bungee.api.ChatColor;

public class SpleggMap {

	private String mapName;
	private File file;
	private FileConfiguration config;
	private World world;
	private int mapId;
	private Map map;
	
	public SpleggMap(int mapId, FileConfiguration config, World world){
		this.mapId = mapId;
		this.config = config;
		this.world = world;
		getData();
	}
	
	private void getData(){
		mapName = config.getString("map." + mapId + ".name");
		file = new File("plugins/Splegg/Maps/" + config.getString("map." + mapId + ".schem") + ".splegg");
		map = new Map(file);
	}
	
	public void loadMap(SpleggMain plugin, Location loc){
		map.loadMap(world, loc, plugin);
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
	
	
	public static class Map{
		
		private File file;
		private ArrayList<MapBlock> mapBlocks = new ArrayList<>();
		
		public Map(File file){
			this.file = file;
			loadMap();
		}	
		
		public static void createMap(Player player, WorldEditPlugin we, String name){
			
			Selection select = we.getSelection(player);
			Location min = select.getMinimumPoint();
			Location max = select.getMaximumPoint();
			
			Location playerLoc = player.getLocation();
			int pX = playerLoc.getBlockX();			
			int pY = playerLoc.getBlockY();			
			int pZ = playerLoc.getBlockZ();			

			ArrayList<MapBlock> blocks = new ArrayList<>();
			
			for(int x = min.getBlockX(); x <= max.getBlockX(); x++){
				for(int y = min.getBlockY(); y <= max.getBlockY(); y++){
					for(int z = min.getBlockZ(); z <= max.getBlockZ(); z++){
						Location loc = new Location(player.getWorld(), x, y, z);					
						Material mat = loc.getBlock().getType();
						if(mat.equals(Material.AIR)) continue;
						
						byte data = loc.getBlock().getData();

						blocks.add(new MapBlock(x - pX, y - pY, z - pZ, mat, data));

					}
				}
			}
			
			player.sendMessage(ChatColor.GREEN + "Selected " + blocks.size() + " blocks");	
			
			
			File file = new File("plugins/Splegg/Maps/" + name + ".splegg");
			
		    PrintWriter out;
			try {
				out = new PrintWriter(file);
				
				
				for(MapBlock block : blocks){
					out.println(block.toString());
				}
				
				
				
				
				
				 out.flush();
				 out.close();
			} catch (FileNotFoundException e) {
				player.sendMessage(ChatColor.DARK_RED + "" +  ChatColor.BOLD + "Error: " + ChatColor.RED + "Couldn't write to file'");
				e.printStackTrace();
			}

		    
		   
			
			
		}
		
		public ArrayList<MapBlock> getMapBlocks(){
			return mapBlocks;
		}
		
		public void loadMap(World world, Location origin, SpleggMain plugin){		
			long time = System.currentTimeMillis();
			repeat(plugin, (ArrayList<MapBlock>) mapBlocks.clone(), world, origin, mapBlocks.size()/180, 1L);			
		}
		
		/*
		 * for(MapBlock block : mapBlocks){
				Location loc = new Location(world, block.getX(), block.getY(), block.getZ());
				loc.add(origin);				
				Material mat = block.getMat();
				byte data = block.getData();
				
				if(loc.getBlock().getType().equals(mat) && loc.getBlock().getData() == data) continue;
				
				loc.getBlock().setType(mat);
				loc.getBlock().setData(data);			
			}	
		 */
		
		private void repeat(SpleggMain plugin, ArrayList<MapBlock> blocks, World world, Location origin, int amount, long delay){
			
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){

				@Override
				public void run() {
					int blockSize = blocks.size();
					for(int i = blocks.size()-1; i >= blockSize - amount; i--){
						if(i == 0) continue;
						MapBlock block = blocks.get(i);
						
						Location loc = new Location(world, block.getX(), block.getY(), block.getZ());
						loc.add(origin);				
						Material mat = block.getMat();
						byte data = block.getData();
						
						//if(loc.getBlock().getType().equals(mat) && loc.getBlock().getData() == data) continue;
						
						loc.getBlock().setType(mat);
						loc.getBlock().setData(data);			
						
						
						blocks.remove(i);						
					}
					repeat(plugin, blocks, world, origin, amount, delay);
					
				}
				
				
				
			}, delay);
			
			
			
		}
		
		
		
		private void loadMap(){			
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(file));
			} catch (FileNotFoundException e) {
				Bukkit.broadcastMessage("Failed to find map '" + file.getName() + "'");
				e.printStackTrace();
				return;
			}
			
			String line = null;
			ArrayList<String> lines = new ArrayList<String>();
			try {
				while((line = reader.readLine()) != null){
					lines.add(line);				
				}
			} catch (IOException e) {
				e.printStackTrace();
			}			
			
			for(String l : lines){
				if(l.trim().equals("")) continue;
				mapBlocks.add(getMapBlock(l));			
			}	
			
		}
		
		private MapBlock getMapBlock(String string){			
			int x = -1000;
			int y = -1000;
			int z = -1000;
			
			Material mat = null;
			byte data = -1;
			
			
			String current = "";
			
			for(int i = 0; i < string.length(); i++){				
				if(string.charAt(i) == ','){
					if(x == -1000){
						x = Integer.parseInt(current);
					}else if(y == -1000){
						y = Integer.parseInt(current);
					}else if(z == -1000){
						z = Integer.parseInt(current);
					}else if(mat == null){
						mat = Material.getMaterial(current);
					}else if(data == -1){
						data = (byte) Integer.parseInt(current);
					}				
					current = "";
				}else{
					current += string.charAt(i);
				}			
			}	
			if(data == -1){
				data = (byte) Integer.parseInt(current);
			}			
			return new MapBlock(x,y,z,mat,data);
		}
		
		
		public static class MapBlock{
			
			private int x;
			private int y;
			private int z;
			private Material mat;
			private byte data;
			
			public MapBlock(int x, int y, int z, Material mat, byte data){
				this.x = x;
				this.y = y;
				this.z = z;
				this.mat = mat;
				this.data = data;
			}

			public int getX() {
				return x;
			}

			public int getY() {
				return y;
			}

			public int getZ() {
				return z;
			}

			public Material getMat() {
				return mat;
			}

			public byte getData() {
				return data;
			}	
			
			public String toString(){				
				return "" + x + "," + y + "," + z + "," + mat.toString() + "," + data;			
			}
			
			
		}
		
		
	}
	
	
	
}
