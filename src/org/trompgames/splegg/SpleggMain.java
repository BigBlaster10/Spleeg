package org.trompgames.splegg;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.trompgames.utils.Schematic;
import org.trompgames.utils.Updateable;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;

import net.md_5.bungee.api.ChatColor;

public class SpleggMain extends JavaPlugin{

	public static Plugin plugin;
	public static World world;
	public static WorldEditPlugin we;
	
	@Override
	public void onEnable(){
		plugin = this;
		world = Bukkit.getWorlds().get(0);

		Bukkit.getServer().getPluginManager().registerEvents(new SpleggListener(), this);
		
		Bukkit.broadcastMessage(ChatColor.AQUA + "Splegg Initialized...");
		getWorldEdit();
		
		//this.saveDefaultConfig();
		
		
		Location lobbyLoc = new Location(world, 173.5, 122, 247.5);
		Location mid = new Location(world, 212.5, 95, 249.5);
		
		new SpleggHandler(lobbyLoc, mid, 85);
		
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
            	Updateable.updateUpdateables();        	
            	
            }
        }, 0L, 1L);	
		
		
	}
	
	public void getWorldEdit(){
		try{
			Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldEdit");
			we = (WorldEditPlugin) plugin;			
		}catch(Exception e){
			Bukkit.broadcastMessage(ChatColor.DARK_RED + "" +  ChatColor.BOLD + "Error: " + ChatColor.RED + "Failed to find WorldEdit");
		}
		
		
	}
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!((Player) sender).isOp()) return false;
		Player player = (Player) sender;
		
		if (cmd.getName().equalsIgnoreCase("start")) {
			//String schem = args[0];
			
			for(Player p : Bukkit.getOnlinePlayers()){
				SpleggHandler.handler.playerJoin(p);
			}
			
			SpleggHandler.handler.startGame();
			
			
			//Schematic.loadArea(world, new File("plugins\\WorldEdit\\schematics\\" + schem + ".schematic"), player.getLocation(), true);
			
			
		}
		return true;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
