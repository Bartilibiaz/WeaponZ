package com.bartilibiaz.weaponzstats.listeners;

import com.bartilibiaz.weaponzstats.service.WeaponZStatsService;
import com.bartilibiaz.weaponsplugin.api.events.WeaponKillPlayerEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class WeaponKillListener implements Listener {

    private final WeaponZStatsService stats;

    public WeaponKillListener(WeaponZStatsService stats) {
        this.stats = stats;
    }

    @EventHandler
    public void onKill(WeaponKillPlayerEvent event) {
        stats.recordKill(
                event.getKiller().getUniqueId(),
                event.getWeapon().toString()
        );
    }
}
