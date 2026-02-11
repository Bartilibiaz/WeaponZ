package com.bartilibiaz.weaponzstats.stats;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatsManager {
    private final File file;
    private final FileConfiguration config;
    private final JavaPlugin plugin; // Potrzebne do logowania

    // Cache w pamięci RAM
    private final Map<UUID, Integer> shotsCache = new HashMap<>();
    private final Map<UUID, Integer> killsCache = new HashMap<>();

    public StatsManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "stats.yml");

        // ✅ NAPRAWIONE TWORZENIE PLIKU
        createFileIfNotExists();

        this.config = YamlConfiguration.loadConfiguration(file);
        loadCache();
    }

    private void createFileIfNotExists() {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            
            // Próbujemy zapisać domyślny plik z resources
            try {
                plugin.saveResource("stats.yml", false);
            } catch (IllegalArgumentException e) {
                // ⚠️ Jeśli pliku nie ma w resources (Twój przypadek), tworzymy pusty plik
                try {
                    file.createNewFile();
                    plugin.getLogger().warning("Utworzono pusty plik stats.yml (brak w resources).");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
    }

    private void loadCache() {
        if (config.contains("players")) {
            ConfigurationSection players = config.getConfigurationSection("players");
            if (players == null) return;
            
            for (String key : players.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    shotsCache.put(uuid, config.getInt("players." + key + ".shots", 0));
                    killsCache.put(uuid, config.getInt("players." + key + ".kills", 0));
                } catch (IllegalArgumentException e) {
                    // Ignoruj błędne UUID
                }
            }
        }
    }

    public void save() {
        // Zapisz cache do configu
        for (Map.Entry<UUID, Integer> entry : shotsCache.entrySet()) {
            config.set(path(entry.getKey()) + ".shots", entry.getValue());
        }
        for (Map.Entry<UUID, Integer> entry : killsCache.entrySet()) {
            config.set(path(entry.getKey()) + ".kills", entry.getValue());
        }

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
        shotsCache.merge(player.getUniqueId(), 1, Integer::sum);
    }

    public int getShots(Player player) {
        return shotsCache.getOrDefault(player.getUniqueId(), 0);
    }

    // ☠ KILL
    public void addKill(Player player, String weaponId) {
        killsCache.merge(player.getUniqueId(), 1, Integer::sum);

        // Zapis broni od razu do configu (rzadsze zdarzenie)
        String weaponPath = path(player.getUniqueId()) + ".weapons." + weaponId;
        config.set(weaponPath, config.getInt(weaponPath, 0) + 1);
    }

    public int getKills(Player player) {
        return killsCache.getOrDefault(player.getUniqueId(), 0);
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