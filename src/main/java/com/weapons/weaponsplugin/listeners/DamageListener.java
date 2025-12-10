package com.weapons.weaponsplugin.listeners;

import com.weapons.weaponsplugin.WeaponsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

public class DamageListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Check if the damage was caused by a projectile
        if (event.getDamager() instanceof Projectile) {
            // Check if the entity that was hit is a living entity
            if (event.getEntity() instanceof LivingEntity) {
                final LivingEntity victim = (LivingEntity) event.getEntity();

                // Remove damage immunity ticks, allowing for rapid hits
                victim.setNoDamageTicks(0);

                // Schedule a task to run on the next server tick to cancel knockback
                Bukkit.getScheduler().runTaskLater(WeaponsPlugin.getInstance(), () -> {
                    victim.setVelocity(new Vector(0, 0, 0));
                }, 1L);
            }
        }
    }
}
