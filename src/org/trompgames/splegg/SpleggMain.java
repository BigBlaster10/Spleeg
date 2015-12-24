package org.trompgames.splegg;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class SpleggMain extends JavaPlugin{

	public static Plugin plugin;
	public static World world;
	
	@Override
	public void onEnable(){
		plugin = this;
		world = Bukkit.getWorlds().get(0);

		Bukkit.getServer().getPluginManager().registerEvents(new SpleggListener(), this);
		
		Bukkit.broadcastMessage(ChatColor.AQUA + "Splegg Initialized...");
		
		
		
		
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
            	
            	
            	
            	
            }
        }, 0L, 1L);	
		
		
	}
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!((Player) sender).isOp()) return false;
		Player player = (Player) sender;
		
		if (cmd.getName().equalsIgnoreCase("start")) {
			
			
		}
		return true;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
