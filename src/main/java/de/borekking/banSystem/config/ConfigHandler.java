package de.borekking.banSystem.config;

import net.md_5.bungee.config.Configuration;


public class ConfigHandler {

    private final ConfigFile configFile;

    public ConfigHandler() {
        this.configFile = new ConfigFile("config.yml");

        this.loadDefaultValues();
    }

    // Set default setting (from ConfigSetting-Enum) into config
    // for every key which is not contained in config yet
    private void loadDefaultValues() {
        Configuration config = this.configFile.getConfig();

        for (ConfigSetting setting : ConfigSetting.values()) {
            String path = setting.getPath();

            if (!config.contains(path)) { // Check if current key/path is not in config
                config.set(path, setting.getValue());
            }
        }

        this.configFile.save();
    }

    // Over right ConfigSetting-Enum values with those from config
    private void loadConfigEnum() {
        Configuration config = this.configFile.getConfig();

        for (ConfigSetting setting : ConfigSetting.values()) {
            Object defaultValue = setting.getValue();
            Object value = config.get(setting.getPath());

            if (!defaultValue.equals(value)) { // Check if value from ConfigEnum is not equal to value from config
                setting.setValue(value);
            }
        }
    }
}
