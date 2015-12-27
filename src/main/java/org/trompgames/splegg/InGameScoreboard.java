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
		
		this.playersIdentifier = configMessage.getMessage("game.inGameScoreboardPlayersIdentifier");
		this.eliminatedPlayersIdentifier = configMessage.getMessage("game.inGameScoreboardEliminatedIdentifier");
		this.spectatorsIdentitifier = configMessage.getMessage("game.inGameScoreboardSpectatorsIdentifier");
	}

	@Override
	protected void createScoreboard() {
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		
		board = manager.getNewScoreboard();
		objective = board.registerNewObjective("test", "dummy");
		objective.setDisplayName(configMessage.getMessage("game.inGameScoreboardHeader"));
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		objective.getScore(configMessage.getMessage("game.inGameScoreboardPlayers")).setScore(20); 

		objective.getScore(" ").setScore(19); 
		
		objective.getScore(configMessage.getMessage("game.inGameScoreboardEliminated")).setScore(18); 

		objective.getScore("  ").setScore(17); 		
		
		objective.getScore(configMessage.getMessage("game.inGameScoreboardSpectators")).setScore(16); 		
	 
		objective.getScore("    ").setScore(15); 

	}

	@Override
	protected void updateScoreboard() {
		objective.setDisplayName(configMessage.getMessage("game.inGameScoreboardHeader"));
		for(String s : objective.getScoreboard().getEntries()){
			if(s.toLowerCase().contains(playersIdentifier.toLowerCase())){
				if((configMessage.getMessage("game.inGameScoreboardPlayers")).equals(s)) continue;
				int scoreInt = objective.getScore(s).getScore();
				objective.getScoreboard().resetScores(s);

				Score score = objective.getScore(configMessage.getMessage("game.inGameScoreboardPlayers")); 
				score.setScore(scoreInt);			
			}else if(s.toLowerCase().contains(spectatorsIdentitifier.toLowerCase())){
				if(configMessage.getMessage("game.inGameScoreboardSpectators").equals(s)) continue;
				int scoreInt = objective.getScore(s).getScore();
				objective.getScoreboard().resetScores(s);

				Score score = objective.getScore(configMessage.getMessage("game.inGameScoreboardSpectators")); 
				score.setScore(scoreInt);			
			}else if(s.toLowerCase().contains(eliminatedPlayersIdentifier.toLowerCase())){
				if(configMessage.getMessage("game.inGameScoreboardEliminated").equals(s)) continue;
				int scoreInt = objective.getScore(s).getScore();
				objective.getScoreboard().resetScores(s);

				Score score = objective.getScore(configMessage.getMessage("game.inGameScoreboardEliminated")); 
				score.setScore(scoreInt);			
			}
		}
	}

}
