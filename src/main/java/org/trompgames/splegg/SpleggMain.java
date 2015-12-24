package org.trompgames.splegg;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.trompgames.utils.Updateable;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;

import net.md_5.bungee.api.ChatColor;

public class SpleggMain extends JavaPlugin {

    private World world;
    private WorldEditPlugin we;

    private SpleggHandler handler;

    @Override
    public void onEnable() {
        world = Bukkit.getWorlds().get(0);

        Bukkit.getServer().getPluginManager().registerEvents(new SpleggListener(this), this);

        Bukkit.broadcastMessage(ChatColor.AQUA + "Splegg Initialized...");
        getWorldEdit();

        //this.saveDefaultConfig();


        Location lobbyLoc = new Location(world, 173.5, 122, 247.5);
        Location mid = new Location(world, 212.5, 95, 249.5);

        handler = new SpleggHandler(lobbyLoc, mid, 85);

        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, Updateable::updateUpdateables, 0L, 1L);


    }

    public World getWorld() {
        return world;
    }

    public SpleggHandler getHandler() {
        return handler;
    }

    public WorldEditPlugin getWorldEdit() {
        try {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldEdit");
            if (we == null) {
                we = (WorldEditPlugin) plugin;
            }
            return we;
        } catch (Exception e) {
            Bukkit.broadcastMessage(ChatColor.DARK_RED + ChatColor.BOLD.toString() + "Error: " + ChatColor.RED + "Failed to find WorldEdit");
        }
        return null;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.isOp()) return false;

            if (cmd.getName().equalsIgnoreCase("start")) {
                //String schem = args[0];

                for (Player p : Bukkit.getOnlinePlayers()) {
                    handler.playerJoin(p);
                }

                handler.startGame();

                //Schematic.loadArea(world, new File("plugins\\WorldEdit\\schematics\\" + schem + ".schematic"), player.getLocation(), true);

            }
        }
        return true;
    }

}
