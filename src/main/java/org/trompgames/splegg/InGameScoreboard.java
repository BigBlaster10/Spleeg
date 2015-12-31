package main.java.org.trompgames.splegg;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import net.md_5.bungee.api.ChatColor;

public class InGameScoreboard extends SpleggScoreboard{

	private ConfigMessage configMessage;
	
	private String playersIdentifier;
	private String spectatorsIdentitifier;
	private String eliminatedPlayersIdentifier;
	
	public InGameScoreboard(SpleggHandler handler, ConfigMessage configMessage) {
		super(handler);
		this.configMessage = configMessage;
		createScoreboard();
		
		this.playersIdentifier = configMessage.getMessage("game.inGameScoreboardPlayersIdentifier", handler);
		this.eliminatedPlayersIdentifier = configMessage.getMessage("game.inGameScoreboardEliminatedIdentifier", handler);
		this.spectatorsIdentitifier = configMessage.getMessage("game.inGameScoreboardSpectatorsIdentifier", handler);
	}

	@Override
	protected void createScoreboard() {
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		
		board = manager.getNewScoreboard();
		objective = board.registerNewObjective("test", "dummy");
		objective.setDisplayName(configMessage.getMessage("game.inGameScoreboardHeader", handler));
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		objective.getScore(configMessage.getMessage("game.inGameScoreboardPlayers", handler)).setScore(20); 

		objective.getScore(" ").setScore(19); 
		
		objective.getScore(configMessage.getMessage("game.inGameScoreboardEliminated", handler)).setScore(18); 

		objective.getScore("  ").setScore(17); 		
		
		objective.getScore(configMessage.getMessage("game.inGameScoreboardSpectators", handler)).setScore(16); 		
	 
		objective.getScore("    ").setScore(15); 

	}

	@Override
	protected void updateScoreboard() {
		objective.setDisplayName(configMessage.getMessage("game.inGameScoreboardHeader", handler));
		for(String s : objective.getScoreboard().getEntries()){
			if(s.toLowerCase().contains(playersIdentifier.toLowerCase())){
				if((configMessage.getMessage("game.inGameScoreboardPlayers", handler)).equals(s)) continue;
				int scoreInt = objective.getScore(s).getScore();
				objective.getScoreboard().resetScores(s);

				Score score = objective.getScore(configMessage.getMessage("game.inGameScoreboardPlayers", handler)); 
				score.setScore(scoreInt);			
			}else if(s.toLowerCase().contains(spectatorsIdentitifier.toLowerCase())){
				if(configMessage.getMessage("game.inGameScoreboardSpectators", handler).equals(s)) continue;
				int scoreInt = objective.getScore(s).getScore();
				objective.getScoreboard().resetScores(s);

				Score score = objective.getScore(configMessage.getMessage("game.inGameScoreboardSpectators", handler)); 
				score.setScore(scoreInt);			
			}else if(s.toLowerCase().contains(eliminatedPlayersIdentifier.toLowerCase())){
				if(configMessage.getMessage("game.inGameScoreboardEliminated", handler).equals(s)) continue;
				int scoreInt = objective.getScore(s).getScore();
				objective.getScoreboard().resetScores(s);

				Score score = objective.getScore(configMessage.getMessage("game.inGameScoreboardEliminated", handler)); 
				score.setScore(scoreInt);			
			}
		}
	}

}
