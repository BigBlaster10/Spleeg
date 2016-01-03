package main.java.org.trompgames.utils;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import main.java.org.trompgames.splegg.SpleggHandler;
import main.java.org.trompgames.splegg.SpleggMain;


public class BungeePing implements PluginMessageListener{

	private SpleggMain spleggMain;
	private String pluginChannel;
	private String serverName;
	
	public BungeePing(SpleggMain spleggMain){
		this.spleggMain = spleggMain;
		getPluginChannel();
		getServerName();
	}
	
	private void getPluginChannel(){
		FileConfiguration config = spleggMain.getConfig();
		this.pluginChannel = config.getString("bungee.pluginChannel");
	}
	
	private void getServerName(){
		FileConfiguration config = spleggMain.getConfig();
		this.serverName = config.getString("bungee.serverName");
	}
	
	@Override
	  public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		//Bukkit.broadcastMessage("Channel: " + channel);
		//Bukkit.broadcastMessage("Plugin: " + pluginChannel);
		if (!channel.equals(pluginChannel)) {
		      return;
		}
		
		ByteArrayDataInput in = ByteStreams.newDataInput(message);
		String subchannel = in.readUTF();
		//Bukkit.broadcastMessage(subchannel);
		if(subchannel.equalsIgnoreCase("ping")){
			try{
				ByteArrayDataOutput out = ByteStreams.newDataOutput();
				out.writeUTF("pong"); 
				player.sendPluginMessage(spleggMain, pluginChannel, out.toByteArray());
			}catch(Exception e){
				Bukkit.broadcastMessage("[Splegg] error -.-");
				e.printStackTrace();
			}
			return;
		}else if(subchannel.equalsIgnoreCase("getStats")){
			String server = in.readUTF();
			if(server.equals(serverName)){
				String sentServer = in.readUTF();
				//Bukkit.broadcastMessage("Sent server: " + sentServer);

				try{
					ByteArrayDataOutput out = ByteStreams.newDataOutput();
					out.writeUTF("getStats");
					out.writeUTF(sentServer);
					out.writeUTF(serverName);
					SpleggHandler handler = spleggMain.getBungeeSpleggHandler();
					
					out.writeInt(handler.getPlayers().size());
					out.writeInt(handler.getMaxPlayers());
					out.writeUTF(handler.getGameState().toString());
					if(handler.getMap() == null){
						out.writeUTF("Voting...");
					}else{
						out.writeUTF(handler.getMap().getMapName());
					}
					
					player.sendPluginMessage(spleggMain, pluginChannel, out.toByteArray());
				}catch(Exception e){
					Bukkit.broadcastMessage("[Splegg] error -.-");
					e.printStackTrace();
				}				
				
			}
		}
		
	}

}
