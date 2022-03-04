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

        this.ee.reloadConfig();

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
        cfg.debug = readBoolean("debug");

        return cfg;
    }

    private int readInt(String key) {
        if (config().contains(key))
            return config().getInt(key);
        else {
            throwIA("No value for key " + key);
            return 0;
        }
    }

    private String readString(String key) {
        String res = config().getString(key);
        if (res == null) throwIA("No value for key " + key);
        return res;
    }

    private boolean readBoolean(String key) {
        if (config().contains(key))
            return config().getBoolean(key);
        else {
            throwIA("No value for key " + key);
            return false;
        }
    }

    private void throwIA(String msg) {
        throw new IllegalArgumentException("Config: " + msg);
    }
}
