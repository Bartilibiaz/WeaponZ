package com.bartilibiaz.weaponsplugin.api.stats.impl;

import com.bartilibiaz.weaponsplugin.WeaponsPlugin;
import com.bartilibiaz.weaponsplugin.api.stats.WeaponZStatsService;
import com.bartilibiaz.weaponsplugin.weapons.Weapon;
import com.bartilibiaz.weaponsplugin.weapons.WeaponManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WeaponZStatsServiceImpl implements WeaponZStatsService {

    private final WeaponManager weaponManager;

    private final Map<UUID, Integer> shots = new ConcurrentHashMap<>();

    public WeaponZStatsServiceImpl(WeaponManager weaponManager) {
        this.weaponManager = weaponManager;
    }

    /* ========== WEAPON ========== */

    @Override
    public Weapon getWeapon(String item) {
        return weaponManager.getWeapon(item);
    }

    /* ========== STATS ========== */

    @Override
    public void addShot(Player player) {
        shots.merge(player.getUniqueId(), 1, Integer::sum);
    }

    @Override
    public int getShotsFired(Player player) {
        return shots.getOrDefault(player.getUniqueId(), 0);
    }

    @Override
    public void reset(Player player) {
        shots.remove(player.getUniqueId());
    }
}

