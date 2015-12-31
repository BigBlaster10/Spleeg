package main.java.org.trompgames.splegg;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class PreGameScoreboard extends SpleggScoreboard{

	private ConfigMessage configMessage;
	ArrayList<PlayerData> players = new ArrayList<>();
	
	public PreGameScoreboard(SpleggHandler handler, ConfigMessage configMessage) {
		super(handler);		
		this.configMessage = configMessage;
	}

	@Override
	protected void createScoreboard() {}

	@Override
	protected void updateScoreboard() {}
	
	@Override
	public void addPlayer(Player player){
		players.add(PlayerData.getPlayerData(player));
		setBoard(player);
	}
	
	public void setBoard(Player player){		
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		
		Scoreboard board = manager.getNewScoreboard();
		objective = board.registerNewObjective("test", "dummy");
		objective.setDisplayName(configMessage.getMessage("game.preGameScoreboardHeader", handler));
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
			
		PlayerData data = PlayerData.getPlayerData(player);
		PlayerData.PlayerStats stat = data.getPlayerStats();
		objective.getScore(configMessage.getMessage("game.preGameScoreboardWins", handler)).setScore(stat.getCurrentWins()); 
		objective.getScore(configMessage.getMessage("game.preGameScoreboardGamesPlayed", handler)).setScore(stat.getCurrentGamesPlayed()); 
		objective.getScore(configMessage.getMessage("game.preGameScoreboardEggsShot", handler)).setScore(stat.getCurrentEggsShot()); 
		objective.getScore(configMessage.getMessage("game.preGameScoreboardBlocksDestroyed", handler)).setScore(stat.getCurrentBlocksDestroyed()); 
		objective.getScore(configMessage.getMessage("game.preGameScoreboardDeaths", handler)).setScore(stat.getCurrentDeaths()); 
		objective.getScore(configMessage.getMessage("game.preGameScoreboardPoints", handler)).setScore(stat.getCurrentPoints()); 

		
		
		player.setScoreboard(board);
	}

}
