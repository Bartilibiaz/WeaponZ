package com.bartilibiaz.weaponzrecipes;

import com.bartilibiaz.weaponsplugin.WeaponsPlugin;
import com.bartilibiaz.weaponsplugin.api.WeaponZExtension;
import com.bartilibiaz.weaponzrecipes.recipes.RecipeManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class WeaponZRecipesPlugin extends JavaPlugin implements WeaponZExtension {

    private RecipeManager recipeManager;

    @Override
    public void onEnable() {
        // 1. Sprawdzamy, czy folder pluginu istnieje
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // 2. 🛡️ PANCERNE ZABEZPIECZENIE PLIKU
        // Tworzymy plik tylko wtedy, gdy go FIZYCZNIE NIE MA.
        // Jeśli plik już jest, plugin go NIE DOTYKA.
        File recipesFile = new File(getDataFolder(), "recipes.yml");
        if (!recipesFile.exists()) {
            saveResource("recipes.yml", false); 
            getLogger().info("Utworzono domyślny plik recipes.yml");
        }

        // 3. Uruchamiamy menedżera
        if (WeaponsPlugin.getInstance() != null) {
            this.recipeManager = new RecipeManager(this, WeaponsPlugin.getInstance());
            this.recipeManager.loadRecipes();
            
            WeaponsPlugin.getInstance().registerExtension(this);
        } else {
            getLogger().severe("Nie znaleziono WeaponZ! Plugin się wyłącza.");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        // Nic nie zapisujemy przy wyłączaniu!
        // Dzięki temu Twoje ręczne edycje w pliku są bezpieczne.
    }
    @Override
    public void onReload() {
        if (recipeManager != null) {
            // 1. Logujemy
            getLogger().info("Przeładowywanie receptur...");
            
            // 2. Jeśli plik nie istnieje (ktoś go usunął), przywróć go
            File recipesFile = new File(getDataFolder(), "recipes.yml");
            if (!recipesFile.exists()) {
                saveResource("recipes.yml", false);
            }
            
            // 3. Wczytaj na nowo
            recipeManager.loadRecipes();
        }
    }
    @Override
    public void onEnable(WeaponsPlugin weaponZ) {
    }

    @Override
    public String getExtensionName() {
        return "WeaponZRecipes";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }
}