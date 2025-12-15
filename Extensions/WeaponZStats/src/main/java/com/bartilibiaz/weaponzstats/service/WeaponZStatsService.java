package com.bartilibiaz.weaponzstats.service;

import com.bartilibiaz.weaponzstats.data.PlayerWeaponStats;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WeaponZStatsService {

    private final Map<UUID, PlayerWeaponStats> statsMap = new HashMap<>();

    public PlayerWeaponStats getStats(UUID uuid) {
        return statsMap.computeIfAbsent(uuid, PlayerWeaponStats::new);
    }

    public void recordShot(UUID uuid) {
        getStats(uuid).addShot();
    }

    public void recordKill(UUID killer, String weaponId) {
        getStats(killer).addKill(weaponId);
    }
}
