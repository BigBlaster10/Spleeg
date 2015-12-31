package main.java.org.trompgames.splegg;

import java.util.ArrayList;

import org.bukkit.configuration.file.FileConfiguration;

import net.md_5.bungee.api.ChatColor;

public class RankData {

	private static ArrayList<Rank> ranks = new ArrayList<Rank>();

	public static void loadData(FileConfiguration config){
		int rankNumber = config.getConfigurationSection("ranks").getKeys(false).size();
		for(int i = 0; i < rankNumber; i++){
			String name = config.getString("ranks." + i + ".name");
			int points = config.getInt("ranks." + i + ".points");
			ranks.add(new Rank(ChatColor.translateAlternateColorCodes('&', name), points));
		}		
	}
	
	public static Rank getRank(PlayerData data){
		return getRank(data.getPlayerStats().getCurrentPoints());
	}
	
	public static Rank getRank(int points){
		Rank prev = ranks.get(0);
		for(Rank rank : ranks){
			if(points < rank.getPoints()) return new Rank(prev.getName(), points);
			prev = rank;
		}
		return new Rank(prev.getName(), points);
	}
	
	
	public static class Rank{
		
		String name;
		int points;
		
		public Rank(String name, int points){
			this.name = name;
			this.points = points;
		}
		
		public String getName(){
			return name;
		}
		
		public int getPoints(){
			return points;
		}	
		
	}
	
	
	
	
	
}
