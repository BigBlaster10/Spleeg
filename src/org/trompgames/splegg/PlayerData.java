package org.trompgames.splegg;

import java.util.ArrayList;

import org.bukkit.entity.Player;

public class PlayerData {

	public Player player;
	
	
	public static ArrayList<PlayerData> playerData = new ArrayList<PlayerData>();
	
	private PlayerData(Player player){
		this.player = player;
		playerData.add(this);
	}
	
	public static PlayerData getPlayerData(Player player){
		for(PlayerData data : playerData){
			if(data.getPlayer().equals(player)) return data;
		}
		return new PlayerData(player);
	}
	
	
	public Player getPlayer(){
		return player;
	}
	
}
