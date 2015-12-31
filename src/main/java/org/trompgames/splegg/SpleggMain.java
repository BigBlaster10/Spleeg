package main.java.org.trompgames.splegg;


import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;

import main.java.org.trompgames.splegg.PlayerData.PlayerStats;
import main.java.org.trompgames.utils.MapVote;
import main.java.org.trompgames.utils.Updateable;
import net.md_5.bungee.api.ChatColor;

public class SpleggMain extends JavaPlugin {

    private World world;
    private WorldEditPlugin we;
    
    private SpleggHandler handler;
    private boolean bungee = false;
    
    @Override
    public void onEnable() {
        world = Bukkit.getWorlds().get(0);


        Bukkit.broadcastMessage(ChatColor.AQUA + "Splegg Initialized...");
        getWorldEdit();
        
        this.saveDefaultConfig();

        RankData.loadData(this.getConfig());
        
        String url = this.getConfig().getString("mysql.url");
        String user = this.getConfig().getString("mysql.username");
        String pass = this.getConfig().getString("mysql.password");
        String schema = this.getConfig().getString("mysql.schema");
        String table = this.getConfig().getString("mysql.table");
        
        PlayerData.PlayerStats.createConnection(url, user, pass, schema, table);
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        
        
        //if(this.getConfig().getString("debug").equalsIgnoreCase("true")){
        //	Bukkit.broadcastMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "NOTICE: " + ChatColor.RED + "Debug/Setup mode is enabled, to enable the game turn 'debug' to false in the config");
        //	System.out.println("[Splegg] NOTICE: Debug/Setup mode is enabled, to enable the game turn 'debug' to false in the config");
        //	return;        	
        //}
       
        if(this.getConfig().getConfigurationSection("lobby") == null || this.getConfig().getConfigurationSection("lobby").getKeys(false) == null || this.getConfig().getConfigurationSection("lobby").getKeys(false).size() == 0){
        	Bukkit.broadcastMessage(ChatColor.DARK_RED + "ERROR: " + ChatColor.RED + "Splegg has not been configured!");
        	Bukkit.broadcastMessage(ChatColor.RED + "Use /splegg setSpawn <lobbyName> <yaw> <pitch> to set the default lobby spawn");
        	Bukkit.broadcastMessage(ChatColor.RED + "Use /splegg setArenaSpawn <lobbyName> <yaw> <pitch> to set the default arena spawn");
        	Bukkit.broadcastMessage(ChatColor.RED + "Use /splegg to get a list of commands");
        	return;
        }
        
        if(this.getConfig().getConfigurationSection("map") == null || this.getConfig().getConfigurationSection("map").getKeys(false) == null || this.getConfig().getConfigurationSection("map").getKeys(false).size() == 0){
        	Bukkit.broadcastMessage(ChatColor.DARK_RED + "ERROR: " + ChatColor.RED + "Splegg has not been configured!");
        	Bukkit.broadcastMessage(ChatColor.RED + "No maps have been found!");
        	Bukkit.broadcastMessage(ChatColor.RED + "Use /splegg to get a list of commands");
        	return;
        }
        
       
        ConfigMessage configMessage = new ConfigMessage(this.getConfig());

        if(this.getConfig().getBoolean("bungee.enabled")){
        	String defaultName = this.getConfig().getConfigurationSection("lobby").getKeys(false).iterator().next(); 
        	double y = this.getConfig().getDouble("lobby." + defaultName + ".y");
        	if(y == 0){
            	Bukkit.broadcastMessage(ChatColor.DARK_RED + "ERROR: " + ChatColor.RED + "The y level on the map '" + defaultName + "' has not been configured!");
            	Bukkit.broadcastMessage(ChatColor.RED + "Use /splegg to get a list of commands");
    			return;
    		}
        	Location lobbyLoc = getLocationFromConfig("lobby." + defaultName + ".spawn");              
            Location mid = getLocationFromConfig("lobby." + defaultName + ".arena"); 
            bungee = true;
            handler = new SpleggHandler(lobbyLoc, mid, y, defaultName, this.getConfig(), this, configMessage);
            Bukkit.broadcastMessage(ChatColor.GREEN + "[" + ChatColor.GOLD + "Splegg" + ChatColor.GREEN + "] Map: " + ChatColor.AQUA + defaultName + ChatColor.GREEN + " has been initialized");

        }else{        	
        	for(String defaultName : this.getConfig().getConfigurationSection("lobby").getKeys(false)){
        		double y = this.getConfig().getDouble("lobby." + defaultName + ".y");
        		
        		if(y == 0){
                	Bukkit.broadcastMessage(ChatColor.DARK_RED + "ERROR: " + ChatColor.RED + "The y level on the map '" + defaultName + "' has not been configured!");
                	Bukkit.broadcastMessage(ChatColor.RED + "Use /splegg to get a list of commands");
        			return;
        		}
        		
            	Location lobbyLoc = getLocationFromConfig("lobby." + defaultName + ".spawn");              
                Location mid = getLocationFromConfig("lobby." + defaultName + ".arena"); 
                new SpleggHandler(lobbyLoc, mid, y, defaultName, this.getConfig(), this, configMessage);
                Bukkit.broadcastMessage(ChatColor.GREEN + "[" + ChatColor.GOLD + "Splegg" + ChatColor.GREEN + "] Map: " + ChatColor.AQUA + defaultName + ChatColor.GREEN + " has been initialized");
        	}       	
        }
        
        
        Bukkit.getServer().getPluginManager().registerEvents(new SpleggListener(this, configMessage), this);

        
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, Updateable::updateUpdateables, 0L, 1L);


    }
    
    /*
     * lobby:
  x: 173.5
  y: 122.5
  z: 247.5
  yaw: 90.0
  pitch: 0.0
arena:
  x: 212.5
  y: 120.5
  z: 249.5
  yaw: 90.0
  pitch: 0.0
     */
    
    public Location getLocationFromConfig(String path){
    	double x = this.getConfig().getDouble(path + ".x");
    	double y = this.getConfig().getDouble(path + ".y");
    	double z = this.getConfig().getDouble(path + ".z");

        double yaw = this.getConfig().getDouble(path + ".yaw");
        double pitch = this.getConfig().getDouble(path + ".pitch");

        Location loc = new Location(world, x, y, z);
        
        loc.setYaw((float) yaw);
        loc.setPitch((float) pitch);
        return loc;
    	
    	
    }

    public World getWorld() {
        return world;
    }

    public SpleggHandler getBungeeSpleggHandler(){
    	return handler;
    }
    
    public boolean isBungeeCord(){
    	return bungee;
    }
    
    public WorldEditPlugin getWorldEdit() {
        try {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldEdit");
            if (we == null) {
                we = (WorldEditPlugin) plugin;
            }
            return we;
        } catch (Exception e) {
            Bukkit.broadcastMessage(ChatColor.DARK_RED + ChatColor.BOLD.toString() + "[Splegg] Error: " + ChatColor.RED + "Failed to find WorldEdit");
        }
        return null;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Random rand = new Random();          
            
            if (cmd.getName().equalsIgnoreCase("vote") || cmd.getName().equalsIgnoreCase("v")) {
            	PlayerData data = PlayerData.getPlayerData(player);
            	if(!data.isInGame()){
            		player.sendMessage(this.getConfig().getString("game.voteNotInGameError"));
            		return false;
            	}
            	SpleggHandler handler = data.getSpleggHandler();
            	ConfigMessage configMessage = handler.getConfigMessage();
            	if(args.length < 1){
            		handler.getMapVote().sendVotingOptions(data, handler);
            		return false;
            	}
            	
            	int number = -1;
            	try{            		
            		number = Integer.parseInt(args[0]);
            	}catch(Exception e){
            		player.sendMessage(configMessage.getMessage("game.voteError", handler));
            		return false;
            	}
            	
            	
            	MapVote vote = handler.getMapVote();
            	if(number <= 0 || number > vote.getVotes().length){
            		player.sendMessage(configMessage.getMessage("game.voteError", handler));
            		return false;
            	}            	
            	
            	handler.playerVote(player, number);            	
            }            	
            if (!player.isOp()) return false;
      
                //Schematic.loadArea(world, new File("plugins\\WorldEdit\\schematics\\" + schem + ".schematic"), player.getLocation(), true);
            if (cmd.getName().equalsIgnoreCase("splegg")) {
    			if(args.length == 0 || (args.length >= 1 && args[0].equals("help"))){
    				spleggHelp(player);
    				return false;
    			}
    			
    			switch(args[0].toLowerCase()){
    			
    			case "join":
    				if(args.length < 2){
    					spleggHelp(player);
    					return false;
    				}    				
    				if(SpleggHandler.getSpleggHandler(args[1]) == null){
    					player.sendMessage(ChatColor.RED + "Error: The lobby '" + args[1] + "' was not found");
    				}else{
    					SpleggHandler.getSpleggHandler(args[1]).playerJoin(player);    					
    				}
    				return true;
    			case "points":
    				PlayerData.getPlayerData(player).getPlayerStats().addPoints(Integer.parseInt(args[1]));
    				PlayerStats.saveStats();
    				return true;    			
    			case "maps":
    				
    				String s = ChatColor.GREEN + "Maps: " + ChatColor.GRAY + "[" + ChatColor.GREEN;
    				
    				
    				for(int i = 0; i < getMapNames().size(); i++){
    					String name = getMapNames().get(i);
    					if(i != getMapNames().size()-1)
    						s += name + ", ";
    					else s += name;
    				}
    				s += ChatColor.GRAY + "]";
    				player.sendMessage(s);
    				return true;
    			case "sety":
    				if(args.length < 3){
    					spleggHelp(player);
    					return false;
    				}
    				
    				double y;
    				try{
    					y = Double.parseDouble(args[2]);
    				}catch(Exception e){
    					spleggHelp(player);
    					return false;
    				}	
    				
    				this.getConfig().set("lobby." + args[1] + ".y", y);
    				this.saveConfig();
    				player.sendMessage(ChatColor.GREEN + "Set y to " + y);
    				
    				return true;
    			case "setspawn":
    				if(args.length < 4){
    					spleggHelp(player);
    					return false;
    				}
    				
    				double yaw;
    				double pitch;
    				
    				try{
    					yaw = Double.parseDouble(args[2]);
    					pitch = Double.parseDouble(args[3]);					
    				}catch(Exception e){
    					spleggHelp(player);
    					return false;
    				}	
    				setSpawn(player.getLocation(), yaw, pitch, args[1]);
    				player.sendMessage(ChatColor.GREEN + "Set spawn for lobby");

    				return true;
    			case "setarenaspawn":
    				if(args.length < 4){
    					spleggHelp(player);
    					return false;
    				}
    				
    				yaw = 0;
    				pitch = 0;
    				
    				try{
    					yaw = Double.parseDouble(args[2]);
    					pitch = Double.parseDouble(args[3]);					
    				}catch(Exception e){
    					spleggHelp(player);
    					return false;
    				}	
    				setArenaSpawn(player.getLocation(), yaw, pitch, args[1]);
    				player.sendMessage(ChatColor.GREEN + "Set arena spawn for lobby");

    				return true;
    			case "create":
    				if(args.length < 3){
    					spleggHelp(player);
    					return false;
    				}
    				createMap(args[1], args[2]);
    				player.sendMessage(ChatColor.GREEN + "Created map " + ChatColor.GOLD + args[1] + ChatColor.GREEN + " with the schematic " + ChatColor.GOLD + args[2]);
    				//player.sendMessage(ChatColor.GREEN + "To finish creating the map use /splegg setSpawn");
    				return true;
    			case "remove":
    				if(args.length < 2){
    					spleggHelp(player);
    					return false;
    				}
    				boolean removed = removeMap(args[1]);
    				if(removed){
    					player.sendMessage(ChatColor.GREEN + "The map " + ChatColor.GOLD + args[1] + ChatColor.GREEN + " has been removed");
    				}else{
    					player.sendMessage(ChatColor.DARK_RED + "" +  ChatColor.BOLD + "Error: " + ChatColor.RED + "Couldn't find a map by the name of '" + args[1] + "'");
    				}
    			}
    			return true;

    			
    			
    			
    			
    			}
    			return true;
        	}
			return false;
    	}

    public void spleggHelp(Player player){
        player.sendMessage(ChatColor.GRAY + "------------------------------------------");
        player.sendMessage(ChatColor.GREEN + "Splegg Commands - Created by " + ChatColor.GOLD + "" + ChatColor.BOLD + "BigBlaster10");
        player.sendMessage(ChatColor.GREEN + "/splegg maps" + ChatColor.GRAY + " | " + ChatColor.GOLD + "Get all maps from config");
        player.sendMessage(ChatColor.GREEN + "/splegg create <mapName> <schematicName>" + ChatColor.GRAY + " | " + ChatColor.GOLD + "Creates a map");
        player.sendMessage(ChatColor.GREEN + "/splegg remove <mapName>" + ChatColor.GRAY + " | " + ChatColor.GOLD + "Removes a map");
        player.sendMessage(ChatColor.GREEN + "/splegg setSpawn <lobbyName> <yaw> <pitch>" + ChatColor.GRAY + " | " + ChatColor.GOLD + "Sets the lobby spawn");
        player.sendMessage(ChatColor.GREEN + "/splegg setArenaSpawn <lobbyName> <yaw> <pitch>" + ChatColor.GRAY + " | " + ChatColor.GOLD + "Sets the arena spawn");
        player.sendMessage(ChatColor.GREEN + "/splegg join <lobbyName>" + ChatColor.GRAY + " | " + ChatColor.GOLD + "Join splegg");
        player.sendMessage(ChatColor.GREEN + "/splegg setY <lobbyName> <y>" + ChatColor.GRAY + " | " + ChatColor.GOLD + "Sets the death height");
        player.sendMessage(ChatColor.GREEN + "/splegg help" + ChatColor.GRAY + " | " + ChatColor.GOLD + "Shows this message");
        player.sendMessage(ChatColor.GRAY + "------------------------------------------");
    }


    public void createMap(String name, String schem){
        int mapNumber = 0;

        if(this.getConfig().getConfigurationSection("map") != null)
            mapNumber = this.getConfig().getConfigurationSection("map").getKeys(false).size();
        this.getConfig().set("map." + mapNumber + ".name", name);
        this.getConfig().set("map." + mapNumber + ".schem", schem);

        this.saveConfig();
    }

    public boolean setSpawn(Location loc, double yaw, double pitch, String lobby){
        this.getConfig().set("lobby." + lobby + ".spawn.x", 1.0 * loc.getBlockX() + 0.5);
        this.getConfig().set("lobby." + lobby + ".spawn.y", 1.0 * loc.getBlockY() + 0.5);
        this.getConfig().set("lobby." + lobby + ".spawn.z", 1.0 * loc.getBlockZ() + 0.5);

        this.getConfig().set("lobby." + lobby + ".spawn.yaw", yaw);
        this.getConfig().set("lobby." + lobby + ".spawn.pitch", pitch);

        this.saveConfig();

        return true;
    }
    
    public boolean setArenaSpawn(Location loc, double yaw, double pitch, String lobby){
        this.getConfig().set("lobby." + lobby + ".arena.x", 1.0 * loc.getBlockX() + 0.5);
        this.getConfig().set("lobby." + lobby + ".arena.y", 1.0 * loc.getBlockY() + 0.5);
        this.getConfig().set("lobby." + lobby + ".arena.z", 1.0 * loc.getBlockZ() + 0.5);

        this.getConfig().set("lobby." + lobby + ".arena.yaw", yaw);
        this.getConfig().set("lobby." + lobby + ".arena.pitch", pitch);

        this.saveConfig();

        return true;
    }

    public boolean removeMap(String name){
        int mapId = getMapId(name);
        if(mapId == -1) return false;
        int size = this.getConfig().getConfigurationSection("map").getKeys(false).size();

        for(int i = mapId+1; i < size; i++){
            String n = this.getConfig().getString("map." + i + ".name");
            String schem = this.getConfig().getString("map." + i + ".schem");

            int newPos = i-1;
            //Bukkit.broadcastMessage(n + " i: " + newPos);
            this.getConfig().set("map." + newPos + ".name", n);
            this.getConfig().set("map." + newPos + ".schem", schem);


        }

        this.getConfig().set("map." + (size-1), null);

        this.saveConfig();

        return true;
    }

    /*   This doesn't seem to work...
    public List<String> getMapNames(){
        return this.getConfig().getConfigurationSection("map").getKeys(false).stream().collect(Collectors.toList());
    }
    */
    
    public ArrayList<String> getMapNames(){
		ArrayList<String> mapNames = new ArrayList<String>();
		for(int i = 0; i < this.getConfig().getConfigurationSection("map").getKeys(false).size(); i++ ){
			mapNames.add(this.getConfig().getString("map." + i + ".name"));
		}		
		return mapNames;
	}


    public int getMapId(String name){
        int mapId = -1;
        for(int i = 0; i < this.getConfig().getConfigurationSection("map").getKeys(false).size(); i++ ){
            if(this.getConfig().getString("map." + i + ".name").equals(name)){
                mapId = i;
                break;
            }
        }
        return mapId;

    }

}
