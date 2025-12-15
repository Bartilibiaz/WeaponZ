package com.bartilibiaz.weaponzstats;

import com.bartilibiaz.weaponsplugin.WeaponsPlugin;
import com.bartilibiaz.weaponsplugin.api.WeaponZExtension;
import com.bartilibiaz.weaponzstats.service.WeaponZStatsService;
import com.bartilibiaz.weaponzstats.listeners.WeaponKillListener;
import com.bartilibiaz.weaponzstats.listeners.WeaponShootListener;
import org.bukkit.plugin.java.JavaPlugin;
public class WeaponZStatsPlugin extends JavaPlugin implements WeaponZExtension {

    private WeaponZStatsService statsService;

    @Override
    public void onEnable(WeaponsPlugin weaponsZ) {
        this.statsService = new WeaponZStatsService();

        // Rejestruj listenery
        weaponsZ.getServer().getPluginManager().registerEvents(
                new WeaponShootListener(statsService),
                weaponsZ
        );

        weaponsZ.getServer().getPluginManager().registerEvents(
                new WeaponKillListener(statsService),
                weaponsZ
        );
        weaponsZ.registerExtension(this);
        weaponsZ.getLogger().info("[WeaponZStats] Enabled");
    }

    @Override
    public void onDisable() {}

    @Override
    public String getExtensionName() {
        return "WeaponZStats";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }
}
