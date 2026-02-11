package com.bartilibiaz.weaponzstats.listeners;

import com.bartilibiaz.weaponzstats.stats.StatsManager; // ✅ Zmieniony import
import com.bartilibiaz.weaponsplugin.api.events.WeaponShootEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class WeaponShootListener implements Listener {

    private final StatsManager stats; // ✅ Zmieniony typ pola

    public WeaponShootListener(StatsManager stats) { // ✅ Zmieniony typ w konstruktorze
        this.stats = stats;
    }

    @EventHandler
    public void onShoot(WeaponShootEvent event) {
        // Zapisujemy strzał do pliku/managera
        stats.addShot(event.getShooter()); 
    }
}