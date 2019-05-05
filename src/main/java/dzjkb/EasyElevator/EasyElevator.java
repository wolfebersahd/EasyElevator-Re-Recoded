package dzjkb.EasyElevator;

import org.bukkit.plugin.java.JavaPlugin;

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
    // private List<Elevator> elevators = new ArrayList<>();
    private ElevatorCollection elevators;
    
    public EasyElevator() {}

    public void onEnable() {
        getLogger().info("Enabling EasyElevator");
        reloadConfig();
        this.elevators = new ElevatorCollection(this);
        this.cmd = new EECommands(this);
        this.playerListener = new EEPlayerListener(this);
        this.getCommand("elv").setExecutor(this.cmd);
        this.getCommand("eelevator").setExecutor(this.cmd);
        this.getServer().getPluginManager().registerEvents(this.playerListener, this);
        getLogger().info("EasyElevator enabled!");
    }

    public void onDisable() {
        getLogger().info("Disabling EasyElevator :(");
        for (Elevator e : this.elevators.getElevators()) {
            if (e.currentFloor != null) {
                e.currentFloor.switchRedstoneFloorOn(false);
            }
        }
        getLogger().info("Disabled EasyElevator!");
    }

    public void reloadConfig() {
        this.config = this.configManager.loadConfig();

        // Propagate the config change to any classes that need it
        // playerListener, elevators
    }

    public ElevatorCollection getElevators() {
        return this.elevators;
    }

    public EEConfiguration getEEConfig() {
        return this.config;
    }
}
