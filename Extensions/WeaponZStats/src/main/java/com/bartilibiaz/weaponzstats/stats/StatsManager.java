package com.bartilibiaz.weaponzstats.stats;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.io.File;
import java.io.IOException;
public class StatsManager {
    private final File file;
    private final FileConfiguration config;

    public StatsManager(JavaPlugin plugin) {
        file = new File(plugin.getDataFolder(), "stats.yml");

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource("stats.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String path(UUID uuid) {
        return "players." + uuid;
    }

    // 🔫 STRZAŁ
    public void addShot(Player player) {
        String base = path(player.getUniqueId());
        config.set(base + ".shots", getShots(player) + 1);
        save();
    }

    public int getShots(Player player) {
        return config.getInt(path(player.getUniqueId()) + ".shots", 0);
    }

    // ☠ KILL
    public void addKill(Player player, String weaponId) {
        String base = path(player.getUniqueId());

        config.set(base + ".kills", getKills(player) + 1);

        String weaponPath = base + ".weapons." + weaponId;
        config.set(weaponPath, config.getInt(weaponPath, 0) + 1);

        save();
    }

    public int getKills(Player player) {
        return config.getInt(path(player.getUniqueId()) + ".kills", 0);
    }

    public Map<String, Integer> getWeaponKills(Player player) {
        String path = path(player.getUniqueId()) + ".weapons";
        ConfigurationSection sec = config.getConfigurationSection(path);

        Map<String, Integer> map = new HashMap<>();
        if (sec == null) return map;

        for (String key : sec.getKeys(false)) {
            map.put(key, sec.getInt(key));
        }
        return map;
    }
}
