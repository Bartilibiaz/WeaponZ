package com.bartilibiaz.weaponzstats.listeners;

import com.bartilibiaz.weaponzstats.service.WeaponZStatsService;
import com.bartilibiaz.weaponsplugin.api.events.WeaponShootEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class WeaponShootListener implements Listener {

    private final WeaponZStatsService stats;

    public WeaponShootListener(WeaponZStatsService stats) {
        this.stats = stats;
    }

    @EventHandler
    public void onShoot(WeaponShootEvent event) {
        stats.recordShot(event.getShooter().getUniqueId());
    }
}
