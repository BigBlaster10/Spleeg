package main.java.org.trompgames.splegg;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import main.java.org.trompgames.utils.Updateable;

public class Launcher implements Listener{
	
	private Material launcherMat;

	private double velocityMult;
	private double yAdd;
	SpleggMain plugin;
	public Launcher(SpleggMain plugin, FileConfiguration config) {
		this.launcherMat = Material.getMaterial(config.getInt("game.launcherMat"));
		this.velocityMult = config.getDouble("game.launcherMult");
		this.yAdd = config.getDouble("game.launcherYAdd");
		this.plugin = plugin;
		Bukkit.broadcastMessage("" + launcherMat.toString());
		Bukkit.broadcastMessage("c " + velocityMult);
		Bukkit.broadcastMessage("Y: " + yAdd);		
	}

	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event){
		if(!event.getAction().equals(Action.PHYSICAL)) return;
		Player player = event.getPlayer();
		//if(!PlayerData.hasData(player) || PlayerData.getPlayerData(player).getSpleggHandler() == null) return;
		if(!event.getClickedBlock().getType().equals(launcherMat)) return;
		Vector v = player.getLocation().getDirection();
		v.multiply(velocityMult);
		v.setY(yAdd);

		
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {			 
            public void run() {
                player.setVelocity(v);
            }
         }, 1L);
		
		
	}
	
	
	
	
	
}
