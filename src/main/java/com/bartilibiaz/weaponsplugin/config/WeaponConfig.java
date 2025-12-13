package com.bartilibiaz.weaponsplugin.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;

public class WeaponConfig {
    
    private FileConfiguration config;
    private File file;
    
    public WeaponConfig(File file) {
        this.file = file;
        this.config = YamlConfiguration.loadConfiguration(file);
    }
    
    // WEAPON INFO
    public String getName() {
        return config.getString("weapon.name", "unknown");
    }
    
    public String getDisplayName() {
        return config.getString("weapon.display-name", "§cWeapon");
    }
    
    // MAINHAND
    public String getMainhandType() {
        return config.getString("weapon.mainhand.type", "WARPED_FUNGUS_ON_A_STICK");
    }
    
    public int getMainhandCmd() {
        return config.getInt("weapon.mainhand.custom-model-data", 1);
    }
    
    // OFFHAND
    public String getOffhandType() {
        return config.getString("weapon.offhand.type", "WARPED_FUNGUS_ON_A_STICK");
    }
    
    public int getOffhandCmd() {
        return config.getInt("weapon.offhand.custom-model-data", 1);
    }
    
    // AMMUNITION
    public String getAmmoType() {
        return config.getString("weapon.ammunition.item-type", "SNOWBALL");
    }
    
    public int getAmmoCmd() {
        return config.getInt("weapon.ammunition.custom-model-data", 3);
    }
    
    public int getMagazineSize() {
        return config.getInt("weapon.ammunition.magazine-size", 30);
    }
    
    public double getReloadTime() {
        return config.getDouble("weapon.ammunition.reload-time", 1.5);
    }

    // ===== SHOOTING TYPE =====

    public String getShootingType() {
        return config.getString("weapon.shooting.type", "normal").toLowerCase();
    }

    public boolean isRightClickEnabled() {
        return config.getBoolean("weapon.shooting.right-click", true);
    }

    public boolean isExplosion() {
        return getShootingType().equals("explosion");
    }

    // ===== EXPLOSION CONFIG =====

    public float getExplosionPower() {
        return (float) config.getDouble("weapon.explosion.power", 3.5);
    }

    public boolean isExplosionFire() {
        return config.getBoolean("weapon.explosion.fire", false);
    }

    public boolean isExplosionDestroyBlocks() {
        return config.getBoolean("weapon.explosion.destroy-blocks", false);
    }

    public String getExplosionSound() {
        return config.getString("weapon.explosion.sound", "entity.generic.explode");
    }

    public Float getExplosionRadius() {
        return (float) config.getDouble("weapon.explosion.radius", 10);
    }

    public Float getExplosionDamage() {
        return (float) config.getDouble("weapon.explosion.damage", 5);
    }

    public Float getExplosionKnockback() {
        return (float) config.getDouble("weapon.explosion.knockback", 1);
    }
    

    // COMBAT
    public double getDamage() {
        return config.getDouble("weapon.combat.damage", 7.5);
    }
    
    public float getSpreadNormal() {
        return (float) config.getDouble("weapon.combat.spread-normal", 0.25);
    }
    
    public float getSpreadScoped() {
        return (float) config.getDouble("weapon.combat.spread-scoped", 0.05);
    }
    
    public double getFireRate() {
        return config.getDouble("weapon.combat.fire-rate", 0.1);
    }
    
    public int getRange() {
        return config.getInt("weapon.combat.range", 100);
    }

    // ===== RECOIL =====

    public float getRecoilVerticalBase() {
        return (float) config.getDouble("weapon.combat.recoil.vertical-base", 0.4);
    }

    public float getRecoilVerticalGrowth() {
        return (float) config.getDouble("weapon.combat.recoil.vertical-growth", 0.05);
    }

    public float getRecoilHorizontalBase() {
        return (float) config.getDouble("weapon.combat.recoil.horizontal-base", 1.5);
    }

    public float getRecoilHorizontalGrowth() {
        return (float) config.getDouble("weapon.combat.recoil.horizontal-growth", 0.15);
    }

    public long getRecoilResetTimeMs() {
        return config.getLong("weapon.combat.recoil.reset-time-ms", 300);
    }

    // SOUNDS
    public String getSoundShoot() {
        return config.getString("weapon.sounds.shoot", "ak74.shoot");
    }
    
    public String getSoundReload() {
        return config.getString("weapon.sounds.reload", "entity.armor_stand.place");
    }
    
    public String getSoundEmpty() {
        return config.getString("weapon.sounds.empty", "entity.item.break");
    }
}
