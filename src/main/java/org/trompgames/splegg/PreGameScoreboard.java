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
		objective.setDisplayName(configMessage.getMessage("game.preGameScoreboardHeader"));
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
			
		objective.getScore(configMessage.getMessage("game.preGameScoreboardWins")).setScore(20); 
		objective.getScore(configMessage.getMessage("game.preGameScoreboardGamesPlayed")).setScore(19); 
		objective.getScore(configMessage.getMessage("game.preGameScoreboardEggsShot")).setScore(18); 
		objective.getScore(configMessage.getMessage("game.preGameScoreboardBlocksDestroyed")).setScore(17); 
		objective.getScore(configMessage.getMessage("game.preGameScoreboardDeaths")).setScore(16); 
		objective.getScore(configMessage.getMessage("game.preGameScoreboardDeaths")).setScore(16); 

		
		
		player.setScoreboard(board);
	}

}
