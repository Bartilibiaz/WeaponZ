package com.bartilibiaz.weaponzrecipes.recipes;

import com.bartilibiaz.weaponsplugin.WeaponsPlugin;
import com.bartilibiaz.weaponsplugin.weapons.Weapon;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice; // ✅ Ważny import
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.Set;

public class RecipeManager {

    private final JavaPlugin plugin;
    private final WeaponsPlugin weaponZ;

    public RecipeManager(JavaPlugin plugin, WeaponsPlugin weaponZ) {
        this.plugin = plugin;
        this.weaponZ = weaponZ;
    }

    public void loadRecipes() {
        File file = new File(plugin.getDataFolder(), "recipes.yml");
        if (!file.exists()) return; // Jeśli pliku nie ma, nie robimy nic

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        Set<String> keys = config.getKeys(false);
        int count = 0;

        for (String weaponName : keys) {
            Weapon weapon = weaponZ.getWeaponManager().getWeapon(weaponName);

            if (weapon == null) {
                plugin.getLogger().warning("Pominięto recepturę dla: " + weaponName + " (Brak broni w WeaponZ)");
                continue;
            }

            ItemStack resultItem = weapon.createItem();
            if (resultItem == null) continue;

            NamespacedKey key = new NamespacedKey(plugin, weaponName);
            ShapedRecipe recipe = new ShapedRecipe(key, resultItem);

            List<String> shapeLines = config.getStringList(weaponName + ".shape");
            if (shapeLines.size() != 3) {
                plugin.getLogger().warning("Błąd kształtu dla: " + weaponName);
                continue;
            }
            recipe.shape(shapeLines.get(0), shapeLines.get(1), shapeLines.get(2));

            // Pobieramy sekcję ingredients
            ConfigurationSection ingredients = config.getConfigurationSection(weaponName + ".ingredients");
            if (ingredients == null) continue;

            for (String letter : ingredients.getKeys(false)) {
                char keyChar = letter.charAt(0);

                // 🛑 LOGIKA SKŁADNIKÓW (Zwykły vs CustomModelData)
                if (ingredients.isConfigurationSection(letter)) {
                    // --- OPCJA 1: Składnik ZŁOŻONY (type + cmd) ---
                    ConfigurationSection complexIng = ingredients.getConfigurationSection(letter);
                    String matName = complexIng.getString("type");
                    int cmd = complexIng.getInt("cmd", 0);

                    try {
                        Material mat = Material.valueOf(matName.toUpperCase());
                        ItemStack stack = new ItemStack(mat);
                        
                        // Ustawiamy CustomModelData w składniku
                        if (cmd > 0) {
                            ItemMeta meta = stack.getItemMeta();
                            meta.setCustomModelData(cmd);
                            stack.setItemMeta(meta);
                        }

                        // Używamy ExactChoice - gracz musi dać DOKŁADNIE taki item
                        recipe.setIngredient(keyChar, new RecipeChoice.ExactChoice(stack));

                    } catch (Exception e) {
                        plugin.getLogger().warning("Błąd w złożonym składniku '" + letter + "' dla " + weaponName);
                    }

                } else {
                    // --- OPCJA 2: Składnik PROSTY (tylko nazwa materiału) ---
                    String matName = ingredients.getString(letter);
                    try {
                        Material material = Material.valueOf(matName.toUpperCase());
                        recipe.setIngredient(keyChar, material);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Nieznany materiał '" + matName + "' dla " + weaponName);
                    }
                }
            }

            // Rejestracja receptury
            Bukkit.removeRecipe(key);
            Bukkit.addRecipe(recipe);
            count++;
        }

        plugin.getLogger().info("Załadowano " + count + " receptur (z obsługą CustomModelData)!");
    }
}