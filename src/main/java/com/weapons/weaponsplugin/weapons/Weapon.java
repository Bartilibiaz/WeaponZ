package com.weapons.weaponsplugin.weapons;

import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;

public abstract class Weapon {
    
    private String name;
    private int customModelData;
    private int customModelDataOffhand;
    private float spreadWithoutShift;
    private float spreadWithShift;
    private float damage;
    private int fireRate;

    public Weapon(String name, int customModelData, int customModelDataOffhand, 
                  float spreadWithoutShift, float spreadWithShift, 
                  float damage, int fireRate) {
        this.name = name;
        this.customModelData = customModelData;
        this.customModelDataOffhand = customModelDataOffhand;
        this.spreadWithoutShift = spreadWithoutShift;
        this.spreadWithShift = spreadWithShift;
        this.damage = damage;
        this.fireRate = fireRate;
    }

    public abstract ItemStack createItem();
    public abstract void onShoot(Player player, boolean isScoped);
    public abstract void onReload(Player player);

    // Getters
    public String getName() { return name; }
    public int getCustomModelData() { return customModelData; }
    public int getCustomModelDataOffhand() { return customModelDataOffhand; }
    public float getSpreadWithoutShift() { return spreadWithoutShift; }
    public float getSpreadWithShift() { return spreadWithShift; }
    public float getDamage() { return damage; }
    public int getFireRate() { return fireRate; }

    public boolean isWeapon(ItemStack item) {
        if (item == null) return false;
        try {
            var customData = item.getItemMeta().getCustomModelData();
            return customData == customModelData || customData == customModelDataOffhand;
        } catch (Exception e) {
            return false;
        }
    }
}
