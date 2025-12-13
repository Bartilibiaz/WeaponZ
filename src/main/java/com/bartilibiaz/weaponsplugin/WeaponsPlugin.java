package com.bartilibiaz.weaponsplugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;

import com.bartilibiaz.weaponsplugin.listeners.WeaponClickListener;
import com.bartilibiaz.weaponsplugin.listeners.WeaponListener;
import com.bartilibiaz.weaponsplugin.weapons.WeaponManager;
import com.bartilibiaz.weaponsplugin.commands.WeaponsCommand;
import com.bartilibiaz.weaponsplugin.functions.NoKnockbackNoHurtResistPlugin;

public class WeaponsPlugin extends JavaPlugin {

    private static WeaponsPlugin instance;
    private WeaponManager weaponManager;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(
            new NoKnockbackNoHurtResistPlugin(this),
            this
        );
        Bukkit.getPluginManager().registerEvents(
            new WeaponClickListener(this),
            this
        );
        getCommand("weaponsZ").setExecutor(new WeaponsCommand(this));
        instance = this;
        
        getLogger().info("====================================");
        getLogger().info("   WeaponsPlugin v1.0.0 Loading...");
        getLogger().info("====================================");

        // Initialize weapon manager
        weaponManager = new WeaponManager(this);
        
        // Register listeners
        Bukkit.getPluginManager().registerEvents(new WeaponListener(this), this);

        getLogger().info("✓ Weapons loaded successfully!");;
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
