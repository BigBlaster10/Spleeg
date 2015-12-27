package main.java.org.trompgames.splegg;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import main.java.org.trompgames.utils.Updateable;

public abstract class SpleggScoreboard{

	protected Scoreboard board;
	protected Objective objective;
	
	protected SpleggHandler handler;
	
	public SpleggScoreboard(SpleggHandler handler){
		this.handler = handler;
	}
	
	protected abstract void createScoreboard();
	protected abstract void updateScoreboard();
	
	public SpleggHandler getHandler(){
		return handler;
	}
	
	public void addPlayer(Player player){
		if(board == null) Bukkit.broadcastMessage("board == null");
		player.setScoreboard(board);
	}
	
	
	
	
	
}
