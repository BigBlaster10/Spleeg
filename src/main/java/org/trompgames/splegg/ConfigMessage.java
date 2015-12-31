package main.java.org.trompgames.splegg;

import org.apache.commons.lang3.StringEscapeUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import main.java.org.trompgames.utils.MapVote;
import main.java.org.trompgames.utils.SpleggMap;
import net.md_5.bungee.api.ChatColor;

public class ConfigMessage {

	
	//TODO: Fix this crap up, add a builder or something
	
	private FileConfiguration config;
	private SpleggHandler handler;
	
	public ConfigMessage(FileConfiguration config){
		this.config = config;
	}
	
	public void setHandler(SpleggHandler handler){
		this.handler = handler;
	}
	
	public String getMessage(String path){
		String s =  StringEscapeUtils.unescapeJava(config.getString(path));
		s = replaceStuff(s);
		return replaceColorCodes(s);
	}
	
	public String getMessage(Player player, String path){
		String s = StringEscapeUtils.unescapeJava(config.getString(path));
		s = s.replaceAll("%player%", player.getName());
		s = replaceStuff(s);
		return replaceColorCodes(s);
	}
	
	public String getMessage(Player player, int integer, String path){
		String s = StringEscapeUtils.unescapeJava(config.getString(path));
		s = s.replaceAll("%player%", player.getName());
		s = s.replaceAll("%int%", "" + integer);

		s = replaceStuff(s);
		return replaceColorCodes(s);
	}
	
	public String getMessage(String map, MapVote vote, int mapNumbr, String path){
		String s = StringEscapeUtils.unescapeJava(config.getString(path));
		s = s.replaceAll("%mapName%", map);
		s = s.replaceAll("%mapVotes%", "" + vote.getVotes()[mapNumbr-1]);

		s = s.replaceAll("%mapNumber%", "" + (mapNumbr));

		s = replaceStuff(s);
		return replaceColorCodes(s);
	}
	
	public String getMessage(int integer, String path){
		String s = StringEscapeUtils.unescapeJava(config.getString(path));
		s = s.replaceAll("%int%", "" + integer);

		s = replaceStuff(s);
		return replaceColorCodes(s);
	}
	

	public String getMessage(String string, String path){
		String s = StringEscapeUtils.unescapeJava(config.getString(path));
		s = s.replaceAll("%string%", "" + string);

		s = replaceStuff(s);
		return replaceColorCodes(s);
	}
	
	public String getMessage(Player player, String string, int integer, String path){
		String s = StringEscapeUtils.unescapeJava(config.getString(path));
		PlayerData data = PlayerData.getPlayerData(player);
		s = s.replaceAll("%player%", player.getName());
		s = s.replaceAll("%rank%", data.getRank().getName());
		s = s.replaceAll("%points%", "" + data.getRank().getPoints());
		s = s.replaceAll("%int%", "" + integer);
		s = s.replaceAll("%string%", "" + string);

		s = replaceStuff(s);
		return replaceColorCodes(s);
	}
	
	public String replaceStuff(String s){
		s = s.replaceAll("%players%", "" + handler.getPlayers().size());
		s = s.replaceAll("%maxPlayers%", "" + SpleggHandler.getMaxPlayers());
		s = s.replaceAll("%eliminated%", "" + Math.abs((handler.getAllivePlayers().size() - handler.getPlayers().size())));
		s = s.replaceAll("%allivePlayers%", "" + handler.getAllivePlayers().size());
		s = s.replaceAll("%spectators%", "" + handler.getSpectators().size());
		s = s.replaceAll("%minutesLeft%", "" + handler.getMinutesLeft());
		s = s.replaceAll("%secondsLeft%", "" + handler.getSecondsLeft());
		return s;
	}
	
	private String replaceColorCodes(String s){	
		return ChatColor.translateAlternateColorCodes('&', s);
	}
	
	
}
