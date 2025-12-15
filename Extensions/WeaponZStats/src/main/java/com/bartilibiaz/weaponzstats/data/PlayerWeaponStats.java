package com.bartilibiaz.weaponzstats.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerWeaponStats {

    private final UUID uuid;

    private int shotsFired;
    private int playerKills;

    private final Map<String, Integer> killsPerWeapon = new HashMap<>();

    public PlayerWeaponStats(UUID uuid) {
        this.uuid = uuid;
    }

    public void addShot() {
        shotsFired++;
    }

    public void addKill(String weaponId) {
        playerKills++;
        killsPerWeapon.merge(weaponId, 1, Integer::sum);
    }

    // Gettery
    public int getShotsFired() { return shotsFired; }
    public int getPlayerKills() { return playerKills; }
    public Map<String, Integer> getKillsPerWeapon() { return killsPerWeapon; }
}
