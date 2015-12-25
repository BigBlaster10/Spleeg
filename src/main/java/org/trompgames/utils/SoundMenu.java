package main.java.org.trompgames.utils;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import main.java.org.trompgames.splegg.SpleggMain;
import net.md_5.bungee.api.ChatColor;

public class SoundMenu {

	private int menu;
	private float pitch;
	private Player player;
	private SpleggMain plugin;
	
	public static ArrayList<SoundMenu> menus = new ArrayList<SoundMenu>();
	
	private SoundMenu(Player player, SpleggMain plugin){
		this.player = player;
		this.plugin = plugin;
		menus.add(this);
	}
	
	public static SoundMenu getSoundMenu(Player player, SpleggMain plugin){
		for(SoundMenu menu : menus){
			if(menu.player.equals(player)) return menu;
		}
		return new SoundMenu(player, plugin);
	}
	
	public Player getPlayer(){
		return player;
	}
	
	public int getMenuNumber(){
		return menu;
	}
	
	public void openMenu(){
		Inventory inv = Bukkit.createInventory(null, 54, "Sounds");
		//Bukkit.broadcastMessage("Sound size: " + Sound.values().length);
		int data = 0;
		for(int i = menu*45; i < menu*45 + 45; i++){
			if(i+1 >= Sound.values().length) continue;
			ItemStack item = new ItemStack(Material.STAINED_CLAY, 1, (byte) data);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(ChatColor.GREEN + Sound.values()[i].toString());
			item.setItemMeta(meta);
			inv.addItem(item);
			
			data++;
			if(data > 15) data = 0;
		}	
		
		
		ItemStack next = createItem(Material.HOPPER, ChatColor.GREEN + "Next", (byte) 0);
		ItemStack back = createItem(Material.HOPPER, ChatColor.GREEN + "Back", (byte) 0);
		
		float pitch = 0;
		for(int i = 45; i < 53; i++){
			byte d = (byte) 14;
			if(pitch == this.pitch) data = 5; 
			
			ItemStack item = createItem(Material.STAINED_GLASS, ChatColor.GREEN + "Pitch: " + pitch, d);
			inv.setItem(i, item);
			pitch += 0.25f;
		}
		
		inv.setItem(45, back);
		inv.setItem(53, next);		
		player.openInventory(inv);
	}
	
	public void updateInventory(InventoryView inv){
		int data = 0;
		int pos = 0;
		for(int i = menu*45; i < menu*45 + 45; i++){
			if(i+1 >= Sound.values().length) continue;
			ItemStack item = new ItemStack(Material.STAINED_CLAY, 1, (byte) data);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(ChatColor.GREEN + Sound.values()[i].toString());
			item.setItemMeta(meta);
			inv.setItem(pos, item);
			
			pos++;
			data++;
			if(data > 15) data = 0;
		}	
		
		
		ItemStack next = createItem(Material.HOPPER, ChatColor.GREEN + "Next", (byte) 0);
		ItemStack back = createItem(Material.HOPPER, ChatColor.GREEN + "Back", (byte) 0);
		
		float pitch = 0;
		for(int i = 45; i < 53; i++){
			byte d = (byte) 14;
			if(this.pitch == pitch) d = 5; 
			ItemStack item = createItem(Material.STAINED_GLASS, ChatColor.GREEN + "Pitch: " + pitch, d);
			inv.setItem(i, item);
			pitch += 0.25f;
		}
		
		inv.setItem(45, back);
		inv.setItem(53, next);		
	}
	
	public ItemStack createItem(Material mat, String name, byte data){
		ItemStack item = new ItemStack(mat, 1, data);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);	
		item.setItemMeta(meta);
		return item;
	}
	
	public void menuClick(ItemStack clicked){
		String name = clicked.getItemMeta().getDisplayName();
		if(getSound(name) == null){
			if(name.equalsIgnoreCase(ChatColor.GREEN + "Next")){
				menu++;
				updateInventory(player.getOpenInventory());

			}else if(name.equalsIgnoreCase(ChatColor.GREEN + "Back")){
				menu--;	
				if(menu < 0) menu = 0;
				updateInventory(player.getOpenInventory());

			}else if(name.contains(ChatColor.GREEN + "Pitch")){
				String s = name.replace("Pitch: ", "");
				this.pitch = Float.parseFloat(ChatColor.stripColor(s));
				updateInventory(player.getOpenInventory());

			}
			return;
		}
		
		Sound sound = getSound(name);
		player.playSound(player.getLocation(), sound, 1f, pitch);
		
    	player.sendMessage(ChatColor.GREEN + "Played: " + ChatColor.GOLD + sound.toString());
	}
	
	public void open(){
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
            	openMenu();
            }
        }, 1L);
		
		
		
	}
	
	
	public Sound getSound(String s){	    	
	    for(Sound sound : Sound.values()){
	    	if(ChatColor.stripColor(s).equalsIgnoreCase(sound.toString())) return sound;
	    }
	    return null;
	    	
	}
	
	
}
