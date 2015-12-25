package main.java.org.trompgames.utils;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import main.java.org.trompgames.splegg.ConfigMessage;
import main.java.org.trompgames.splegg.PlayerData;
import main.java.org.trompgames.splegg.SpleggHandler;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class MapVote {

	private ArrayList<String> maps;
	private int[] votes;
	private FileConfiguration config;
	private ConfigMessage configMessage;
	
	public MapVote(FileConfiguration config, ConfigMessage configMessage){
		this.config = config;
		this.maps = getMapNames();
		this.configMessage = configMessage;
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
		
		options.add(new TextComponent(this.configMessage.getMessage("game.voteMenuHeader")));

		//options.add(new TextComponent(ChatColor.GRAY + "-------------------------"));

		options.add(new TextComponent(this.configMessage.getMessage("game.voteMenuInstructions")));
		//options.add(new TextComponent(ChatColor.GREEN + "To vote click on a map or use /vote #"));
		int i = 1;
		for(String map : maps){
									
			//String s = ChatColor.GRAY + "[" + ChatColor.GOLD + i + ChatColor.GRAY + "]  " + ChatColor.GOLD + map + ChatColor.GRAY + "   |   " + ChatColor.GOLD + votes[i-1] + ChatColor.GREEN + " votes";
			String s = this.configMessage.getMessage(map, this, i, "game.voteMenuMap");
			TextComponent text = new TextComponent(s);
			
			ComponentBuilder hoverText = new ComponentBuilder(this.configMessage.getMessage(map, "game.voteMenuHover"));
			//ComponentBuilder hoverText = new ComponentBuilder("Click to vote for " + map);
			//hoverText.color(ChatColor.GREEN);
			
			text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vote " + i));
			text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText.create()));
		
			options.add(text);
			
			i++;
		}
		options.add(new TextComponent(this.configMessage.getMessage("game.voteMenuFooter")));
		
		return options;
	}
	
	public void sendVotingOptions(PlayerData data, SpleggHandler handler){
		if(handler.getGameState().equals(SpleggHandler.GameState.PREGAME)){			
			sendVotingOptions(data.getPlayer());
		}else{
			data.getPlayer().sendMessage(this.configMessage.getMessage("game.voteEnded"));

			//data.getPlayer().sendMessage(ChatColor.GREEN + "Game has already started!");
		}
	}
	
	private void sendVotingOptions(Player player){
		for(TextComponent s : getVotingOptions()){
			player.spigot().sendMessage(s);
		}
	}
	
	
	
	public void playerVote(Player player, int number, SpleggHandler handler){
		if(handler.getMap() != null){
    		player.sendMessage(this.configMessage.getMessage(player, "game.voteEnded"));
    		return;
		}
			
		if(number <= 0 || number > getVotes().length){
    		player.sendMessage(this.configMessage.getMessage(player, "game.voteError"));
    		return;
    	}
		PlayerData data = PlayerData.getPlayerData(player);
		if(data.hasVoted()){
			data.getPlayer().sendMessage(this.configMessage.getMessage("game.alreadyVoted"));
			//data.getPlayer().sendMessage(ChatColor.GREEN + "You have allready voted!");
			return;
		}
		
		data.setVoted(true);
		votes[number-1] += 1;
		Bukkit.broadcastMessage(this.configMessage.getMessage(player, maps.get(number-1), votes[number-1], "game.vote"));
		//Bukkit.broadcastMessage(ChatColor.GOLD + player.getName() + ChatColor.GREEN + " voted for " + ChatColor.GOLD + maps.get((number-1)) + ChatColor.GRAY + "   |   " + ChatColor.GOLD + votes[number-1] + ChatColor.GREEN + " votes");
		//player.sendMessage(ChatColor.GREEN + "You voted for " + maps.get(number-1));
	}
	
	public SpleggMap getWinner(World world){
		return new SpleggMap(getWinnerId(), config, world);	
	}
	
	public int[] getVotes(){
		return votes;
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
