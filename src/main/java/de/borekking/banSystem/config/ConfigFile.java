package de.borekking.banSystem.config;

import de.borekking.banSystem.BungeeMain;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigFile {

    // Class representing a Bungee-Config with its file;
    // Methode to: clear (/empty) the file; (re-)load the file; save the file

    private File file; // Actual file
    private Configuration config; // Bungee Config


    // Constructor for path and filename
    public ConfigFile(String path, String fileName) {
        try {
            this.loadConfig(path, fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Constructor for fileName only
    public ConfigFile(String fileName) {
        this(null, fileName);
    }

    public void clearFile() {
        for (String key : this.config.getKeys()) {
            this.config.set(key, null);
        }
    }

    public void loadFile() {
        try {
            this.config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(this.file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(this.config, this.file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Create file and Config (given fileName; no path)
    private void loadConfig(String fileName) throws IOException {
        this.loadConfig(null, fileName);
    }

    // Create file and Config (given path and fileName)
    private void loadConfig(String path, String fileName) throws IOException {
        File dataFolder = BungeeMain.getInstance().getDataFolder();
        if (!dataFolder.exists()) dataFolder.mkdir(); // Create plugin folder if needed

        // Get parent from plugin folder and given path (if not null)
        String parent = dataFolder.getPath();
        if (path != null) {
            parent +=  "/" + path;
        }

        this.file = new File(parent, fileName);

        if (!this.file.exists()) this.file.createNewFile();

        this.loadFile();
        this.save();
    }

    public Configuration getConfig() {
        return config;
    }
}
