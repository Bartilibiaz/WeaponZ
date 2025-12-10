package com.weapons.weaponsplugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import com.weapons.weaponsplugin.listeners.WeaponListener;
import com.weapons.weaponsplugin.weapons.WeaponManager;

public class WeaponsPlugin extends JavaPlugin {

    private static WeaponsPlugin instance;
    private WeaponManager weaponManager;

    @Override
    public void onEnable() {
        instance = this;
        
        getLogger().info("====================================");
        getLogger().info("   WeaponsPlugin v1.0.0 Loading...");
        getLogger().info("====================================");

        // Initialize weapon manager
        weaponManager = new WeaponManager(this);
        
        // Register listeners
        Bukkit.getPluginManager().registerEvents(new WeaponListener(this), this);

        getLogger().info("✓ Weapons loaded successfully!");
        getLogger().info("✓ AK-74 weapon registered");
        getLogger().info("====================================");
    }

    @Override
    public void onDisable() {
        getLogger().info("WeaponsPlugin disabled!");
    }

    public static WeaponsPlugin getInstance() {
        return instance;
    }

    public WeaponManager getWeaponManager() {
        return weaponManager;
    }
}
