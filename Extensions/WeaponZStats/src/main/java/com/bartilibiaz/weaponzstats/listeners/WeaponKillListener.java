package com.bartilibiaz.weaponzstats.listeners;

import com.bartilibiaz.weaponzstats.stats.StatsManager;
import com.bartilibiaz.weaponsplugin.api.events.WeaponKillPlayerEvent;
import static org.bukkit.Bukkit.getLogger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class WeaponKillListener implements Listener {

    private final StatsManager stats;

    public WeaponKillListener(StatsManager stats) {
        this.stats = stats;
    }

    @EventHandler
    public void onKill(WeaponKillPlayerEvent event) {
        // 1. Zapisz statystyki
        stats.addKill(
                event.getKiller(),
                event.getWeapon().getName()
        );

        // 2. 🎨 WYŚWIETL WIZYTÓWKĘ
        Player killer = event.getKiller();
        Player victim = event.getVictim();

        if (killer != null && victim != null) {
            // \uE001 - Twoja grafika
            // \uE002 - Cofnięcie tekstu (Negative Space)
            String card = "\uE001"; 
            String negativeSpace = "\uE002"; 
            
            String message = "§fZabiłeś: §c" + victim.getName();

            // 🛑 POPRAWKA TUTAJ: Łączymy Obrazek + Cofnięcie + Tekst
            killer.sendActionBar(message);
            
            getLogger().info("Wizytówka zabójstwa wyświetlona dla " + killer.getName());
            
            // Dźwięk
            killer.playSound(killer.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
        }
    }
}