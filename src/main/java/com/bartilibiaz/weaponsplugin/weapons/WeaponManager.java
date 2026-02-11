package com.bartilibiaz.weaponsplugin.weapons;

import org.bukkit.Bukkit;
import com.bartilibiaz.weaponsplugin.WeaponsPlugin;
import com.bartilibiaz.weaponsplugin.config.WeaponConfig;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class WeaponManager {
    
    private Map<String, Weapon> weapons = new HashMap<>();
    private Map<String, WeaponConfig> configs = new HashMap<>();
    private WeaponsPlugin plugin;
    
    public WeaponManager(WeaponsPlugin plugin) {
        this.plugin = plugin;
        loadAllWeapons();
    }
    
    /**
     * Ładuje wszystkie pliki YAML z folderu weapons/
     */
    public void loadAllWeapons() {
        File weaponsFolder = new File(plugin.getDataFolder(), "weapons");
        
        // Utwórz folder jeśli nie istnieje
        if (!weaponsFolder.exists()) {
            weaponsFolder.mkdirs();
            plugin.getLogger().info("Utworzono folder: plugins/WeaponsPlugin/weapons/");
        }
        
        // Ładuj wszystkie pliki .yml
        File[] yamlFiles = weaponsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        
        if (yamlFiles == null || yamlFiles.length == 0) {
            plugin.getLogger().warning("Nie znaleziono plików broni w folder weapons/!");
            return;
        }
        
        for (File file : yamlFiles) {
            loadWeapon(file);
        }
        
        plugin.getLogger().info("Załadowano " + weapons.size() + " broni!");
    }
    
    /**
     * Ładuje jedną broń z pliku YAML
     */
    private void loadWeapon(File file) {
        try {
            WeaponConfig config = new WeaponConfig(file);
            String weaponName = config.getName();
            
            // Utwórz broń na podstawie konfiguracji
            Weapon weapon = new DynamicWeapon(config);
            
            weapons.put(weaponName, weapon);
            configs.put(weaponName, config);
            
            plugin.getLogger().info("✓ Załadowano broń: " + weaponName);
        } catch (Exception e) {
            plugin.getLogger().severe("Błąd ładowania broni z " + file.getName() + ": " + e.getMessage());
        }
    }
    /**
     * Sprawdza, czy dany przedmiot jest bronią z pluginu WeaponZ.
     * Weryfikacja odbywa się po CustomModelData.
     */
    public boolean isWeapon(org.bukkit.inventory.ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        if (!item.getItemMeta().hasCustomModelData()) return false;

        int itemCMD = item.getItemMeta().getCustomModelData();

        // Przeszukujemy wszystkie załadowane bronie
        // (Zakładam, że mapa z broniami nazywa się 'weapons' lub 'loadedWeapons')
        for (Weapon weapon : weapons.values()) { 
            // Sprawdzamy czy broń ma w ogóle model data i czy pasuje do itemu
            if (weapon.getCustomModelData() == itemCMD) {
                return true;
            }
        }
        return false;
    }
    
    public Weapon getWeapon(String name) {
        return weapons.get(name);
    }
    
    public WeaponConfig getWeaponConfig(String name) {
        return configs.get(name);
    }
    
    public void registerWeapon(String name, Weapon weapon) {
        weapons.put(name, weapon);
    }
    
    public Map<String, Weapon> getAllWeapons() {
        return new HashMap<>(weapons);
    }
    
    public void shootWeapon(org.bukkit.entity.Player player, Weapon weapon, boolean isScoped) {
        // Fire rate check
        weapon.onShoot(player, isScoped);
    }
}