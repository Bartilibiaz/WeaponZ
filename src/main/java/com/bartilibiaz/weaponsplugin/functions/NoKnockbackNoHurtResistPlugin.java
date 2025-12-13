package com.bartilibiaz.weaponsplugin.functions;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Usuwa knockback i hurt resistance wyłącznie dla trafień WeaponZ.
 * Wymaga scoreboard tagu: weaponz_hit
 */
public class NoKnockbackNoHurtResistPlugin implements Listener {

    private final JavaPlugin plugin;

    public NoKnockbackNoHurtResistPlugin(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Wyłącza hurt resistance
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        Entity victim = event.getEntity();

        if (!(victim instanceof LivingEntity living)) return;
        if (!living.getScoreboardTags().contains("weaponz_hit")) return;

        // Tick PO uderzeniu (po tym jak MC ustawi cooldown)
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!living.isValid()) return;

                living.setNoDamageTicks(0);
                living.setMaximumNoDamageTicks(0);

                // Knockback OFF
                living.setVelocity(new Vector(0, 0, 0));

                if (living instanceof Player p) {
                    p.setFallDistance(0f);
                }
            }
        }.runTaskLater(plugin, 1L); // <-- 1 TICK, TO JEST KLUCZ
    }

}