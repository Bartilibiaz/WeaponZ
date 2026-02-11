package com.bartilibiaz.weaponzattachments.managers;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttachmentManager {

    private final JavaPlugin plugin;
    private final Map<String, AttachmentData> attachments = new HashMap<>();
    public final NamespacedKey KEY_ID;

    public AttachmentManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.KEY_ID = new NamespacedKey(plugin, "attachment_id");
        loadAttachments();
    }

    public void loadAttachments() {
        File file = new File(plugin.getDataFolder(), "attachments.yml");
        if (!file.exists()) return;
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("attachments");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            ConfigurationSection sec = section.getConfigurationSection(key);
            
            // Item
            Material mat = Material.valueOf(sec.getString("item.material", "STONE"));
            String name = sec.getString("item.name", "Attachment").replace("&", "§");
            List<String> lore = sec.getStringList("item.lore").stream().map(s -> s.replace("&", "§")).toList();
            int cmd = sec.getInt("item.custom-model-data", 0);
            
            // Lore wyświetlane na broni
            String displayLore = sec.getString("display-lore", key).replace("&", "§");

            // Statystyki (Zapisujemy całe sekcje, żeby obsłużyć overrides)
            Map<String, ModifierInfo> modifiers = new HashMap<>();
            ConfigurationSection modsSec = sec.getConfigurationSection("modifiers");
            if (modsSec != null) {
                if (modsSec.contains("clip-size")) modifiers.put("clip-size", new ModifierInfo(modsSec.getConfigurationSection("clip-size")));
                if (modsSec.contains("recoil")) modifiers.put("recoil", new ModifierInfo(modsSec.getConfigurationSection("recoil")));
                if (modsSec.contains("spread")) modifiers.put("spread", new ModifierInfo(modsSec.getConfigurationSection("spread")));
                if (modsSec.contains("damage")) modifiers.put("damage", new ModifierInfo(modsSec.getConfigurationSection("damage")));
            }

            attachments.put(key, new AttachmentData(key, mat, name, lore, cmd, displayLore, modifiers));
        }
        plugin.getLogger().info("Załadowano " + attachments.size() + " konfigurowalnych dodatków.");
    }

    // 🔥 NOWA METODA: Oblicza wartość dla konkretnej broni
    public double getModifierValue(String attachmentId, String modifierType, String weaponId) {
        AttachmentData data = attachments.get(attachmentId);
        if (data == null || !data.modifiers.containsKey(modifierType)) return 0;

        ModifierInfo info = data.modifiers.get(modifierType);
        
        // 1. Sprawdź czy jest wyjątek dla tej broni
        if (info.weaponOverrides.containsKey(weaponId)) {
            return info.weaponOverrides.get(weaponId);
        }
        
        // 2. Jeśli nie, zwróć wartość domyślną
        return info.defaultValue;
    }

    public AttachmentData getData(String id) {
        return attachments.get(id);
    }

    public ItemStack getAttachmentItem(String id) {
        AttachmentData data = attachments.get(id);
        if (data == null) return null;

        ItemStack item = new ItemStack(data.material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(data.name);
        meta.setLore(data.lore);
        if (data.cmd > 0) meta.setCustomModelData(data.cmd);
        meta.getPersistentDataContainer().set(KEY_ID, PersistentDataType.STRING, id);
        item.setItemMeta(meta);
        return item;
    }

    // Klasy pomocnicze do przechowywania danych
    public record AttachmentData(String id, Material material, String name, List<String> lore, int cmd, String displayLore, Map<String, ModifierInfo> modifiers) {}
    
    public static class ModifierInfo {
        double defaultValue;
        Map<String, Double> weaponOverrides = new HashMap<>();

        public ModifierInfo(ConfigurationSection sec) {
            // Obsługa prosta: "clip-size: 10"
            // Obsługa złożona: "clip-size: { default: 10, weapons: ... }"
            if (sec == null) {
                this.defaultValue = 0;
                return;
            }
            this.defaultValue = sec.getDouble("default", 0);
            
            ConfigurationSection overrides = sec.getConfigurationSection("weapons");
            if (overrides != null) {
                for (String wpn : overrides.getKeys(false)) {
                    weaponOverrides.put(wpn, overrides.getDouble(wpn));
                }
            }
        }
    }
}