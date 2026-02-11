package com.bartilibiaz.weaponsplugin;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;

import com.bartilibiaz.weaponsplugin.listeners.WeaponClickListener;
import com.bartilibiaz.weaponsplugin.listeners.WeaponListener;
import com.bartilibiaz.weaponsplugin.weapons.WeaponManager;
import com.bartilibiaz.weaponsplugin.commands.WeaponsCommand;
import com.bartilibiaz.weaponsplugin.functions.NoKnockbackNoHurtResistPlugin;
import com.bartilibiaz.weaponsplugin.api.stats.impl.WeaponZStatsServiceImpl;
import com.bartilibiaz.weaponsplugin.api.WeaponZAPI;
import com.bartilibiaz.weaponsplugin.api.WeaponZExtension;

public class WeaponsPlugin extends JavaPlugin {

    private static WeaponsPlugin instance;
    private WeaponManager weaponManager;
    private final List<WeaponZExtension> loadedExtensions = new ArrayList<>();

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

        WeaponZStatsServiceImpl statsService =
                new WeaponZStatsServiceImpl(weaponManager);

        WeaponZAPI.init(statsService);
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
        Bukkit.getScheduler().runTaskLater(this, this::scanExtensionsByJarName, 1L);
    }

    @Override
    public void onDisable() {
        getLogger().info("WeaponsPlugin disabled!");
    }
    public void registerExtension(WeaponZExtension extension) {
        if (!loadedExtensions.contains(extension)) {
            loadedExtensions.add(extension);
            getLogger().info("Registered extension: " + extension.getExtensionName() + " v" + extension.getVersion());
        }
    }
    public static WeaponsPlugin getInstance() {
        return instance;
    }
    
    public List<WeaponZExtension> getExtensions() {
        return loadedExtensions;
    }

    public WeaponManager getWeaponManager() {
        return weaponManager;
    }
    private void scanExtensionsByJarName() {
        File pluginsDir = getDataFolder().getParentFile(); // /plugins

        if (pluginsDir == null || !pluginsDir.exists()) {
            getLogger().warning("Nie znaleziono folderu plugins!");
            return;
        }

        File[] files = pluginsDir.listFiles((dir, name) ->
                name.toLowerCase().startsWith("weaponsz")
                && name.toLowerCase().endsWith(".jar")
                && !name.equalsIgnoreCase(getFile().getName()) // pomiń główny plugin
        );

        getLogger().info("Loaded extensions (" + (files == null ? 0 : files.length) + "):");

        if (files == null || files.length == 0) {
            return;
        }

        for (File file : files) {
            getLogger().info(" - " + file.getName());
        }
    }

}
