package main.java.org.trompgames.splegg;


import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;

import main.java.org.trompgames.utils.MapVote;
import main.java.org.trompgames.utils.SoundMenu;
import main.java.org.trompgames.utils.Updateable;
import net.md_5.bungee.api.ChatColor;

public class SpleggMain extends JavaPlugin {

    private World world;
    private WorldEditPlugin we;

    private SpleggHandler handler;
    private ConfigMessage configMessage;
    
    @Override
    public void onEnable() {
        world = Bukkit.getWorlds().get(0);

        Bukkit.getServer().getPluginManager().registerEvents(new SpleggListener(this), this);

        Bukkit.broadcastMessage(ChatColor.AQUA + "Splegg Initialized...");
        getWorldEdit();

        this.saveDefaultConfig();

        
       
        Location lobbyLoc = getLocationFromConfig("lobby"); 
        
        Location mid = getLocationFromConfig("arena"); 
        configMessage = new ConfigMessage(this.getConfig());

        handler = new SpleggHandler(lobbyLoc, mid, 85, this.getConfig(), this, configMessage);
        
        configMessage.setHandler(handler);
        
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, Updateable::updateUpdateables, 0L, 1L);


    }
    
    public Location getLocationFromConfig(String path){
    	int x = this.getConfig().getInt(path + ".x");
        int y = this.getConfig().getInt(path + ".y");
        int z = this.getConfig().getInt(path + ".z");

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

    public SpleggHandler getHandler() {
        return handler;
    }

    public WorldEditPlugin getWorldEdit() {
        try {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldEdit");
            if (we == null) {
                we = (WorldEditPlugin) plugin;
            }
            return we;
        } catch (Exception e) {
            Bukkit.broadcastMessage(ChatColor.DARK_RED + ChatColor.BOLD.toString() + "Error: " + ChatColor.RED + "Failed to find WorldEdit");
        }
        return null;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            
           
            
            if (cmd.getName().equalsIgnoreCase("vote") || cmd.getName().equalsIgnoreCase("v")) {
            	PlayerData data = PlayerData.getPlayerData(player);
            	if(args.length < 1){
            		handler.getMapVote().sendVotingOptions(data, handler);
            		return false;
            	}
            	
            	int number = -1;
            	try{            		
            		number = Integer.parseInt(args[0]);
            	}catch(Exception e){
            		player.sendMessage(this.configMessage.getMessage(player, "game.voteError"));
            		return false;
            	}
            	
            	
            	MapVote vote = handler.getMapVote();
            	if(number <= 0 || number > vote.getVotes().length){
            		player.sendMessage(this.configMessage.getMessage(player, "game.voteError"));
            		return false;
            	}            	
            	
            	handler.playerVote(player, number);            	
            }            	
            if (!player.isOp()) return false;

            
            if(cmd.getName().equalsIgnoreCase("sound")){
            	
            	SoundMenu.getSoundMenu(player, this).openMenu();
            	
            	
            }
            
            if (cmd.getName().equalsIgnoreCase("start")) {
                //String schem = args[0];
            	
                for (Player p : Bukkit.getOnlinePlayers()) {
                    handler.playerJoin(p);
                }

                handler.startGame();
            }
                //Schematic.loadArea(world, new File("plugins\\WorldEdit\\schematics\\" + schem + ".schematic"), player.getLocation(), true);
            if (cmd.getName().equalsIgnoreCase("splegg")) {
    			if(args.length == 0 || (args.length >= 1 && args[0].equals("help"))){
    				spleggHelp(player);
    				return false;
    			}
    			
    			switch(args[0].toLowerCase()){
    			
    			case "join":
    				handler.playerJoin(player);
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
    	
    			case "setspawn":
    				if(args.length < 3){
    					spleggHelp(player);
    					return false;
    				}
    				
    				double yaw;
    				double pitch;
    				
    				try{
    					yaw = Double.parseDouble(args[1]);
    					pitch = Double.parseDouble(args[2]);					
    				}catch(Exception e){
    					spleggHelp(player);
    					return false;
    				}	
    				setSpawn(player.getLocation(), yaw, pitch);
    				player.sendMessage(ChatColor.GREEN + "Set spawn for lobby");

    				return true;
    			case "setarenaspawn":
    				if(args.length < 3){
    					spleggHelp(player);
    					return false;
    				}
    				
    				yaw = 0;
    				pitch = 0;
    				
    				try{
    					yaw = Double.parseDouble(args[1]);
    					pitch = Double.parseDouble(args[2]);					
    				}catch(Exception e){
    					spleggHelp(player);
    					return false;
    				}	
    				setArenaSpawn(player.getLocation(), yaw, pitch);
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
        player.sendMessage(ChatColor.GREEN + "/splegg setSpawn <yaw> <pitch>" + ChatColor.GRAY + " | " + ChatColor.GOLD + "Sets the lobby spawn");
        player.sendMessage(ChatColor.GREEN + "/splegg setArenaSpaawn <yaw> <pitch>" + ChatColor.GRAY + " | " + ChatColor.GOLD + "Sets the arena spawn");
        player.sendMessage(ChatColor.GREEN + "/splegg join" + ChatColor.GRAY + " | " + ChatColor.GOLD + "Join splegg");
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

    public boolean setSpawn(Location loc, double yaw, double pitch){
        this.getConfig().set("lobby.x", 1.0 * loc.getBlockX() + 0.5);
        this.getConfig().set("lobby.y", 1.0 * loc.getBlockY() + 0.5);
        this.getConfig().set("lobby.z", 1.0 * loc.getBlockZ() + 0.5);

        this.getConfig().set("lobby.yaw", yaw);
        this.getConfig().set("lobby.pitch", pitch);

        this.saveConfig();

        return true;
    }
    
    public boolean setArenaSpawn(Location loc, double yaw, double pitch){
        this.getConfig().set("arena.x", 1.0 * loc.getBlockX() + 0.5);
        this.getConfig().set("arena.y", 1.0 * loc.getBlockY() + 0.5);
        this.getConfig().set("arena.z", 1.0 * loc.getBlockZ() + 0.5);

        this.getConfig().set("arena.yaw", yaw);
        this.getConfig().set("arena.pitch", pitch);

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
