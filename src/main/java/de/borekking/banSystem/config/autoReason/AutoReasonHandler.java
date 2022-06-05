package de.borekking.banSystem.config.autoReason;

import de.borekking.banSystem.config.ConfigFile;
import de.borekking.banSystem.duration.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.md_5.bungee.config.Configuration;

public class AutoReasonHandler {

    // Class for handling automated Punishments
    // Expected Format; id;name;duration (duration like 6s, 20min, 7d, ...)

    private final String name;

    private final ConfigFile configFile;

    private final Map<Integer, AutoReason> reasons; // AutoReason.id -> AutoReason

    // Note name is not a filename and not ending with .yml
    public AutoReasonHandler(String path, String name) {
        this.name = name;
        this.configFile = new ConfigFile(path, name + ".yml");
        this.reasons = new HashMap<>();

        this.load();
    }

    public void clear() {
        this.reasons.clear();
    }

    public AutoReason getReasonByID(int id) {
        return this.reasons.get(id);
    }

    // Load from file
    private void load() {
        this.setExample();

        this.configFile.save();
        this.configFile.loadFile();

        List<String> list = this.configFile.getConfig().getStringList(this.name);

        for (String str : list) {
            try {
                this.addAutoReason(str);
            } catch (Duration.IllegalDurationException ignored) {
            }
        }
    }

    // On first creation, set example into file
    private void setExample() {
        Configuration config = this.configFile.getConfig();

        if (!config.contains(this.name)) {
            this.configFile.clearFile();

            config.set(this.name, new String[]{"0;TEST;10d"});
        }
    }

    private void addAutoReason(String str) throws Duration.IllegalDurationException {
        String[] split = str.split(";");

        int id;
        try {
            id = Integer.parseInt(split[0]);
        } catch (NumberFormatException e) {
            return;
        }

        String name = split[1], durationStr = split[2];
        long duration = Duration.getValueOfOne(durationStr);

        AutoReason autoReason = new AutoReason(id, duration, name);
        this.addAutoReason(autoReason);
    }

    private void addAutoReason(AutoReason reason) {
        this.reasons.put(reason.getId(), reason);
    }

    public List<Integer> getIds() {
        return new ArrayList<>(this.reasons.keySet());
    }
}
