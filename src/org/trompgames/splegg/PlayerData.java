package org.trompgames.splegg;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.trompgames.utils.Updateable;

public class PlayerData extends Updateable{

	public static final int SHOOTCOOLDOWN = 2;
	
	
	private Player player;
	private boolean isDead = false;
	
	private int cooldown = 0;
	
	public static ArrayList<PlayerData> playerData = new ArrayList<PlayerData>();
	
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
	
	
	
	
	
	
	
	
}
