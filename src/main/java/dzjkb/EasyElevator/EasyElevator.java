package dzjkb.EasyElevator;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;

import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

public class EasyElevator
        extends JavaPlugin
{
    private EEConfiguration config = new EEConfiguration(this);
    private List<Elevator> elevators = new ArrayList<>();
    
    public EasyElevator() {}

    public void onEnable() {
        getServer().getPluginManager().registerEvents(new EEPlayerListener(this), this);
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
        this.config.loadConfig();
    }


    public int getMaxPerimeter()
    {
        return this.config.maxPerimeter;
    }

    public int getMaxFloors()
    {
        return this.config.maxFloors;
    }

    public boolean getArrivalSound()
    {
        return this.config.playArrivalSound;
    }

    public boolean getArrivalMessage()
    {
        return this.config.sendArrivalMessage;
    }

    public String getBlockBorder()
    {
        return this.config.blockBorder;
    }

    public String getBlockFloor()
    {
        return this.config.blockFloor;
    }

    public String getBlockOutputDoor()
    {
        return this.config.blockOutputDoor;
    }

    public String getBlockOutputFloor()
    {
        return this.config.blockOutputFloor;
    }

    public List<Elevator> getElevators() {
        return this.elevators;
    }
}
