package dzjkb.EasyElevator;

import org.bukkit.plugin.java.JavaPlugin;

import dzjkb.EasyElevator.EECommands;
import dzjkb.EasyElevator.EEConfigurationManager;
import dzjkb.EasyElevator.EEPlayerListener;

public class EasyElevator
        extends JavaPlugin
{
    private EEConfigurationManager configManager;
    private EEConfiguration config;
    private EECommands cmd;
    private EEPlayerListener playerListener;
    // private List<Elevator> elevators = new ArrayList<>();
    private ElevatorCollection elevators;
    
    public EasyElevator() {}

    public void onEnable() {
        getLogger().info("Enabling EasyElevator");

        this.configManager = new EEConfigurationManager(this);
        this.elevators = new ElevatorCollection(this);
        this.cmd = new EECommands(this);
        this.playerListener = new EEPlayerListener(this, this.config);
        this.getCommand("elv").setExecutor(this.cmd);
        this.getCommand("eelevator").setExecutor(this.cmd);
        this.getServer().getPluginManager().registerEvents(this.playerListener, this);

        reloadEEConfig();
        getLogger().info("EasyElevator enabled!");
    }

    public void onDisable() {
        getLogger().info("Disabling EasyElevator :(");
        for (Elevator e : this.elevators.getElevators()) {
            if (e.currentFloor != null) {
                e.currentFloor.switchRedstoneFloorOn(false);
            }
        }
        getLogger().info("EasyElevator disabled!");
    }

    public void reloadEEConfig() {
        this.config = this.configManager.loadConfig();

        this.dbg("Got config:\n" + this.config.toString());

        // Propagate the config change to any classes that need it
        // playerListener, elevators

        this.playerListener.updateConfig(this.config);
        this.elevators.updateConfig(this.config);
    }

    public ElevatorCollection getElevators() {
        return this.elevators;
    }

    public EEConfiguration getEEConfig() {
        return this.config;
    }

    public void dbg(String msg) {
        if (this.config.debug) {
            this.getLogger().info(msg);
        }
    }
}
