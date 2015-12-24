package org.trompgames.utils;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.trompgames.splegg.SpleggMain;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.MCEditSchematicFormat;

import net.md_5.bungee.api.ChatColor;

public class Schematic {

	public static void loadArea(SpleggMain plugin, World world, File file, Location loc, boolean withAir){
		EditSession session = plugin.getWorldEdit().getWorldEdit().getEditSessionFactory().getEditSession(new BukkitWorld(world), 1000000);
	    session.enableQueue();
		try {
	        MCEditSchematicFormat.getFormat(file).load(file).paste(session, new Vector(loc.getX(), loc.getY(), loc.getZ()), !withAir);
	    } catch (Exception e) {
			Bukkit.broadcastMessage(ChatColor.DARK_RED + "" +  ChatColor.BOLD + "Error: " + ChatColor.RED + "Failed to paste schematic: " + file.getName());
	        e.printStackTrace();
	    }
		session.flushQueue();	   
	}
	
	
}
