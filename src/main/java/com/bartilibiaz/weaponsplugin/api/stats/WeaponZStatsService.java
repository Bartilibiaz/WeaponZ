package com.bartilibiaz.weaponsplugin.api.stats;

import com.bartilibiaz.weaponsplugin.weapons.Weapon;
import com.bartilibiaz.weaponsplugin.weapons.WeaponManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface WeaponZStatsService {

    /* ========== WEAPON IDENTIFICATION ========== */


    Weapon getWeapon(String item);

    /* ========== STATS ========== */

    void addShot(Player player);

    int getShotsFired(Player player);

    /* ========== LIFECYCLE (OPTIONAL) ========== */

    void reset(Player player);
}

