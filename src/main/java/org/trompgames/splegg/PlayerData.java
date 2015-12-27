package main.java.org.trompgames.splegg;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import main.java.org.trompgames.utils.Updateable;

public class PlayerData extends Updateable{

	public static final int SHOOTCOOLDOWN = 2;

	private Player player;
	private boolean isDead = false;
	
	private boolean hasVoted = false;
	
	private int cooldown = 0;
	
	private PlayerStats playerStats;
	
	private static List<PlayerData> playerData = new ArrayList<>();
	
	private PlayerData(Player player){
		super(1);
		this.player = player;
		this.playerStats = new PlayerStats(player);
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
	
	public boolean hasVoted(){
		return hasVoted;
	}
	
	public void setVoted(boolean hasVoted){
		this.hasVoted = hasVoted;
	}
	
	public void setDead(boolean isDead){
		this.isDead = isDead;
		if(isDead) this.getPlayerStats().addDeath();
	}

	public boolean canShoot(){
		return cooldown <= 0;
	}
	
	public void shoot(){
		this.cooldown = SHOOTCOOLDOWN;
		this.playerStats.addEggsShot();
	}
	
	public PlayerStats getPlayerStats(){
		return playerStats;
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
	
	public static class PlayerStats{
		
		private Player player;
		
		private int deaths;
		private int gamesPlayed;
		private int eggsShot;
		private int blocksDestroyed;
		private int pointsAquired;		
		private int wins;
		
		public static ArrayList<PlayerStats> stats = new ArrayList<PlayerStats>();
		
		private PlayerStats(Player player){
			this.player = player;
			stats.add(this);
		}
		
		public static PlayerStats getPlayer(Player player){
			for(PlayerStats stat : stats){
				if(stat.getPlayer().equals(player)) return stat;
			}
			return new PlayerStats(player);
		}				
		
		public Player getPlayer(){
			return player;
		}
		
		
		public void addDeath(){
			deaths++;
		}
		
		public void addGamePlayed(){
			gamesPlayed++;
		}
		
		public void addEggsShot(){
			eggsShot++;
		}
		
		public void addBlocksDestroyed(){
			blocksDestroyed++;
		}
		
		public void addPointsAquired(int points){
			pointsAquired += points;
		}
		
		public void addWin(){
			wins++;
		}

		public int getDeaths() {
			return deaths;
		}

		public int getGamesPlayed() {
			return gamesPlayed;
		}

		public int getEggsShot() {
			return eggsShot;
		}

		public int getBlocksDestroyed() {
			return blocksDestroyed;
		}

		public int getPointsAquired() {
			return pointsAquired;
		}

		public int getWins() {
			return wins;
		}

		public static ArrayList<PlayerStats> getStats() {
			return stats;
		}	
	
	}
	
	
	

}
