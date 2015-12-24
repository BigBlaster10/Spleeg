package main.java.org.trompgames.utils;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class MapVote {

	private ArrayList<String> maps;
	private int[] votes;
	private FileConfiguration config;
	
	public MapVote(FileConfiguration config){
		this.config = config;
		this.maps = getMapNames();

		votes = new int[maps.size()];		
	}
	
	private ArrayList<String> getMapNames(){
		ArrayList<String> mapNames = new ArrayList<String>();
		for(int i = 0; i < config.getConfigurationSection("map").getKeys(false).size(); i++ ){
			mapNames.add(config.getString("map." + i + ".name"));
		}		
		return mapNames;
	}
	
	public ArrayList<TextComponent> getVotingOptions(){
		ArrayList<TextComponent> options = new ArrayList<TextComponent>();
		options.add(new TextComponent(ChatColor.GREEN + "To vote click on a map or use /vote #"));
		int i = 1;
		for(String map : maps){
									
			String s = ChatColor.GRAY + "[" + ChatColor.GOLD + i + ChatColor.GRAY + "]  " + ChatColor.GOLD + map + ChatColor.GRAY + "   |   " + ChatColor.GOLD + votes[i-1] + ChatColor.GREEN + " votes";
			
			TextComponent text = new TextComponent(s);
			
			ComponentBuilder hoverText = new ComponentBuilder("Click to vote for " + map);
			hoverText.color(ChatColor.GREEN);
			
			text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vote " + i));
			text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText.create()));
		
			options.add(text);
			
			i++;
		}
		options.add(new TextComponent(ChatColor.GRAY + "-------------------------"));
		return options;
	}
	
	public void sendVotingOptions(Player player){
		for(TextComponent s : getVotingOptions()){
			player.spigot().sendMessage(s);
		}
	}
	
	
	public SpleggMap getWinner(World world){
		return new SpleggMap(getWinnerId(), config, world);	
	}
	
	private int getWinnerId(){
		int heighest = votes[0];
		int pos = 0;
		for(int i = 0; i < votes.length; i++){
			if(votes[i] > heighest){
				heighest = votes[i];
				pos = i;
			}
		}
		return pos;
	}
	
	
	
	
	
}
