package dzjkb.EasyElevator;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;

import dzjkb.EasyElevator.EEConfiguration;

public class EEConfigurationManager
{
    private String configFile = "config.yml";
    private EasyElevator ee;

    public EEConfigurationManager(EasyElevator e) {
        this.ee = e;
    }

    public EEConfiguration loadConfig()
    {
        File cfgFile = new File(this.ee.getDataFolder(), this.configFile);
        if (!cfgFile.exists()) {
            this.ee.getLogger().info("Creating default config");
            this.ee.saveDefaultConfig();
        }

        return loadValues();
    }

    private FileConfiguration config() {
        return this.ee.getConfig();
    }

    private EEConfiguration loadValues() {
        EEConfiguration cfg = new EEConfiguration();
        cfg.maxPerimeter = readInt("maxPerimeter");
        cfg.maxFloors = readInt("maxFloors");
        cfg.playArrivalSound = readBoolean("arrival.playSound");
        cfg.sendArrivalMessage = readBoolean("arrival.sendMessage");
        cfg.blockBorder = readString("blocks.border");
        cfg.blockFloor = readString("blocks.floor");
        cfg.blockOutputFloor = readString("blocks.outputFloor");
        cfg.blockOutputDoor = readString("blocks.outputDoor");

        return cfg;
    }

    private int readInt(String key) {
        if (config().contains(key))
            return config().getInt(key);
        else
            throwIA("No value for key " + key);
            return 0;
    }

    private String readString(String key) {
        String res = config().getString(key);
        if (res == null) throwIA("No value for key " + key);
        return res;
    }

    private boolean readBoolean(String key) {
        if (config().contains(key))
            return config().getBoolean(key);
        else
            throwIA("No value for key " + key);
            return false;
    }

    private void throwIA(String msg) {
        throw new IllegalArgumentException("Config: " + msg);
    }

    // public int getMaxPerimeter()
    // {
    //     this.config = loadConfig();
    //     return this.config.getInt("MaxPerimeter");
    // }

    // public int getMaxFloors()
    // {
    //     this.config = loadConfig();
    //     return this.config.getInt("MaxFloors");
    // }

    // public boolean getArrivalSound()
    // {
    //     this.config = loadConfig();
    //     return this.config.getBoolean("Arrival.Sound");
    // }

    // public boolean getArrivalMessage()
    // {
    //     this.config = loadConfig();
    //     return this.config.getBoolean("Arrival.Message");
    // }

    // public String getBlock(String Block)
    // {
    //     this.config = loadConfig();
    //     if (this.config.contains("Blocks." + Block)) {
    //         return this.config.getString("Blocks." + Block);
            
    //     }
    //     System.out.println("[EasyElevator] An error occured in your config. Please check for errors! (" + Block + ")");
    //     return "ERROR";
    // }

    // public void addNewNodes()
    // {
    //     try
    //     {
    //         if (this.config.contains("PlayElevatorSound"))
    //         {
    //             this.config.set("Arrival.Sound", this.config.get("PlayElevatorSound"));
    //             this.config.set("PlayElevatorSound", null);
    //             System.out.println("[EasyElevator] Added Arrival.Sound Node to Configuration");
    //             System.out.println("[EasyElevator] Removed PlayElevatorSound Node from Configuration");
    //         }
    //         if (!this.config.contains("Arrival.Sound"))
    //         {
    //             this.config.set("Arrival.Sound", Boolean.valueOf(true));
    //             System.out.println("[EasyElevator] Added Arrival.Sound Node to Configuration");
    //         }
    //         if (!this.config.contains("Arrival.Message"))
    //         {
    //             this.config.set("Arrival.Message", Boolean.valueOf(true));
    //             System.out.println("[EasyElevator] Added Arrival.Message Node to Configuration");
    //         }
    //         if (!this.config.contains("Blocks.Border"))
    //         {
    //             this.config.set("Blocks.Border", "41");
    //             System.out.println("[EasyElevator] Added Blocks.Border Node to Configuration");
    //         }
    //         if (!this.config.contains("Blocks.Floor"))
    //         {
    //             this.config.set("Blocks.Floor", "42");
    //             System.out.println("[EasyElevator] Added Blocks.Floor Node to Configuration");
    //         }
    //         if (!this.config.contains("Blocks.OutputDoor"))
    //         {
    //             this.config.set("Blocks.OutputDoor", "35:14");
    //             System.out.println("[EasyElevator] Added Blocks.OutputDoor Node to Configuration");
    //         }
    //         if (!this.config.contains("Blocks.OutputFloor"))
    //         {
    //             this.config.set("Blocks.OutputFloor", "35:1");
    //             System.out.println("[EasyElevator] Added Blocks.OutputFloor Node to Configuration");
    //         }
    //         this.config.save(this.configFile);
    //     }
    //     catch (Exception localException) {}
    // }
}
