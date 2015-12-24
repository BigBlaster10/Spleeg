package org.trompgames.spleeg;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class SpleegMain extends JavaPlugin{

	
	@Override
	public void onEnable(){
		
		Bukkit.broadcastMessage(ChatColor.AQUA + "Spleeg Initialized...");
		
		
	}
	
	
}
