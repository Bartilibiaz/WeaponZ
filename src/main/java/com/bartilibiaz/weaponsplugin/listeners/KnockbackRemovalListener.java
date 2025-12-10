package com.bartilibiaz.weaponsplugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import com.bartilibiaz.weaponsplugin.WeaponsPlugin;

public class KnockbackRemovalListener implements Listener {

    private WeaponsPlugin plugin;

    public KnockbackRemovalListener(WeaponsPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * ✅ Blokuj knockback przy strzale (projectile hit)
     * Zapobiega odrzutowi gracza/entity po trafieniu pociskiem
     */
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        // Sprawdź czy to nasz pocisk (Snowball = broń)
        if (event.getEntity() instanceof Snowball) {
            Snowball snowball = (Snowball) event.getEntity();
            
            // Jeśli strzelał gracz i trafił entity
            if (snowball.getShooter() instanceof Player && event.getHitEntity() instanceof LivingEntity) {
                Player shooter = (Player) snowball.getShooter();
                LivingEntity target = (LivingEntity) event.getHitEntity();
                
                // ✅ RESET velocity po trafieniu (brak knockback!)
                target.setVelocity(new Vector(0, target.getVelocity().getY(), 0));
                
                return;
            }
        }
    }

    /**
     * ✅ Blokuj knockback przy melee uderzeniu (sword, etc)
     * Zapobiega odrzutowi gracza po trafieniu mieczem
     */
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Sprawdź czy atakujący to gracz
        if (event.getDamager() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            LivingEntity victim = (LivingEntity) event.getEntity();
            
            // ✅ RESET velocity po ataku (brak knockback!)
            victim.setVelocity(new Vector(0, victim.getVelocity().getY(), 0));
        }
    }
}
