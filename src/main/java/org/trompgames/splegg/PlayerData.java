package org.trompgames.splegg;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.trompgames.utils.Updateable;

public class PlayerData extends Updateable{

	public static final int SHOOTCOOLDOWN = 2;

	private Player player;
	private boolean isDead = false;
	
	private int cooldown = 0;
	
	private static List<PlayerData> playerData = new ArrayList<>();
	
	private PlayerData(Player player){
		super(1);
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
	
	public boolean isDead(){
		return isDead;
	}
	
	public void setDead(boolean isDead){
		this.isDead = isDead;
	}

	public boolean canShoot(){
		return cooldown <= 0;
	}
	
	public void shoot(){
		this.cooldown = SHOOTCOOLDOWN;
	}
	
	@Override
	protected void update() {
		if(cooldown > 0){
			cooldown--;		
		}
	}
	
	
	public static List<PlayerData> getPlayerData() {
		if (playerData != null) {
			return playerData;
		}
		throw new NullPointerException("PlayerData not initialized.");
	}

}
