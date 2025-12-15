package com.bartilibiaz.weaponsplugin.api;

import com.bartilibiaz.weaponsplugin.weapons.Weapon;

import com.bartilibiaz.weaponsplugin.api.stats.WeaponZStatsService;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


public final class WeaponZAPI {

    private static WeaponZStatsService statsService;

    private WeaponZAPI() {}

    public static void init(WeaponZStatsService service) {
        statsService = service;
    }

    /* ================== WEAPONS ================== */


    public static Weapon getWeapon(String item) {
        return statsService.getWeapon(item);
    }

    /* ================== STATS ================== */

    public static int getShotsFired(Player player) {
        return statsService.getShotsFired(player);
    }

    public static void addShot(Player player) {
        statsService.addShot(player);
    }
}
