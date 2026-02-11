package com.bartilibiaz.weaponzstats;

import com.bartilibiaz.weaponsplugin.WeaponsPlugin;
import com.bartilibiaz.weaponsplugin.api.WeaponZExtension;
import com.bartilibiaz.weaponzstats.stats.StatsManager;
import com.bartilibiaz.weaponzstats.commnads.StatsCommand; // Twoja literówka w nazwie folderu
import com.bartilibiaz.weaponzstats.listeners.WeaponKillListener;
import com.bartilibiaz.weaponzstats.listeners.WeaponShootListener;
import org.bukkit.plugin.java.JavaPlugin;

public class WeaponZStatsPlugin extends JavaPlugin implements WeaponZExtension {

    // ✅ Używamy tylko StatsManager (Service jest niepotrzebny)
    private StatsManager statsManager;

    @Override
    public void onEnable() {
        // 1. To jest standardowa metoda, którą uruchamia Bukkit
        this.statsManager = new StatsManager(this);

        if (getCommand("stats") != null) {
            getCommand("stats").setExecutor(new StatsCommand(statsManager));
        } else {
            getLogger().severe("Nie znaleziono komendy 'stats' w plugin.yml!");
        }

        // Rejestruj listenery
        getServer().getPluginManager().registerEvents(new WeaponShootListener(statsManager), this);
        getServer().getPluginManager().registerEvents(new WeaponKillListener(statsManager), this);

        // Rejestracja w głównym pluginie (jeśli jest dostępny)
        if (WeaponsPlugin.getInstance() != null) {
            WeaponsPlugin.getInstance().registerExtension(this);
        }
        
        getLogger().info("[WeaponZStats] Enabled correctly!");
    }

    @Override
    public void onDisable() {
        if (statsManager != null) {
            statsManager.save();
        }
    }

    // --- Metody z interfejsu WeaponZExtension ---
    // Muszą tu być, ale zostawiamy je puste lub proste, bo logika jest wyżej
    
    @Override
    public void onEnable(WeaponsPlugin weaponsZ) {
        // Zostawiamy puste! Wszystko robimy w standardowym onEnable()
    }

    @Override
    public String getExtensionName() {
        return "WeaponZStats";
    }

    @Override
    public String getVersion() {
        return getDescription().getVersion();
    }
}