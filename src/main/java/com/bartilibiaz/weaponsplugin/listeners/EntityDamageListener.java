package com.bartilibiaz.weaponsplugin.listeners;

import com.bartilibiaz.weaponsplugin.WeaponsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

public class EntityDamageListener implements Listener {

    public EntityDamageListener(WeaponsPlugin plugin) {
        // Constructor, can be used to pass the main plugin instance
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof LivingEntity) {
            final LivingEntity victim = (LivingEntity) event.getEntity();

            // Instantly remove damage immunity ticks to allow for rapid, successive hits from any source.
            victim.setNoDamageTicks(0);

            // Schedule a task to run on the next server tick to reliably cancel knockback.
            Bukkit.getScheduler().runTaskLater(WeaponsPlugin.getInstance(), () -> {
                victim.setVelocity(new Vector(0, 0, 0));
            }, 1L);
        }
    }
}