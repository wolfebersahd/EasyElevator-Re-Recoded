package dzjkb.EasyElevator;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;

import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.CommandExecutor;

import dzjkb.EasyElevator.EECommands;
import dzjkb.EasyElevator.EEConfigurationManager;
import dzjkb.EasyElevator.EEPlayerListener;

public class EasyElevator
        extends JavaPlugin
{
    private EEConfigurationManager configManager = new EEConfigurationManager(this);
    private EEConfiguration config;
    private EECommands cmd;
    private EEPlayerListener playerListener;
    private List<Elevator> elevators = new ArrayList<>();
    
    public EasyElevator() {}

    public void onEnable() {
        this.cmd = new EECommands(this);
        this.playerListener = new EEPlayerListener(this);
        this.getCommand("elv").setExecutor(this.cmd);
        this.getCommand("eelevator").setExecutor(this.cmd);
        this.getServer().getPluginManager().registerEvents(this.playerListener, this);
        reloadConfig();
    }

    public void onDisable() {
        for (Elevator e : this.elevators) {
            if (e.currentFloor != null) {
                e.currentFloor.switchRedstoneFloorOn(false);
            }
        }
    }

    public void reloadConfig() {
        this.config = this.configManager.loadConfig();

        // Propagate the config change to any classes that need it
        // playerListener, elevators
    }

    public List<Elevator> getElevators() {
        return this.elevators;
    }

    public EEConfiguration getEEConfig() {
        return this.config;
    }
}
