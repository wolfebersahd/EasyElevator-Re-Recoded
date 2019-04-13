package dzjkb.EasyElevator;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;

public class EEConfiguration
{
    private String configFile = "config.yml";
    private EasyElevator ee;

    public int maxPerimeter = 24;
    public int maxFloors = 10;
    public boolean playArrivalSound = true;
    public boolean sendArrivalMessage = true;
    public String blockBorder = "GOLD_BLOCK";
    public String blockFloor = "IRON_BLOCK";
    public String blockOutputDoor = "RED_WOOL";
    public String blockOutputFloor = "ORANGE_WOOL";


    public EEConfiguration(EasyElevator e) {
        this.ee = e;
    }

    public void loadConfig()
    {
        File cfgFile = new File(this.ee.getDataFolder(), this.configFile);
        if (!cfgFile.exists()) {
            this.ee.getLogger().info("Creating default config");
            this.ee.saveDefaultConfig();
        }

        loadValues();
    }

    private FileConfiguration config() {
        return this.ee.getConfig();
    }

    private void loadValues() {
        this.maxPerimeter = readInt("maxPerimeter");
        this.maxFloors = readInt("maxFloors");
        this.playArrivalSound = readBoolean("arrival.playSound");
        this.sendArrivalMessage = readBoolean("arrival.sendMessage");
        this.blockBorder = readString("blocks.border");
        this.blockFloor = readString("blocks.floor");
        this.blockOutputFloor = readString("blocks.outputFloor");
        this.blockOutputDoor = readString("blocks.outputDoor");
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
