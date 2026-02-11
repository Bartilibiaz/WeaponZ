package com.bartilibiaz.weaponzattachments.listeners;

import com.bartilibiaz.weaponsplugin.WeaponsPlugin;
import com.bartilibiaz.weaponsplugin.weapons.Weapon;
import com.bartilibiaz.weaponzattachments.managers.AttachmentManager;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class AttachmentListener implements Listener {

    private final JavaPlugin plugin;
    private final AttachmentManager manager;

    // Klucze głównego pluginu (tam gdzie DynamicWeapon je czyta)
    private final NamespacedKey MOD_CLIP;
    private final NamespacedKey MOD_RECOIL;
    private final NamespacedKey MOD_SPREAD;
    private final NamespacedKey MOD_DAMAGE;

    public AttachmentListener(JavaPlugin plugin, AttachmentManager manager) {
        this.plugin = plugin;
        this.manager = manager;
        JavaPlugin main = WeaponsPlugin.getInstance();
        MOD_CLIP = new NamespacedKey(main, "mod_clip");
        MOD_RECOIL = new NamespacedKey(main, "mod_recoil");
        MOD_SPREAD = new NamespacedKey(main, "mod_spread");
        MOD_DAMAGE = new NamespacedKey(main, "mod_damage");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        // Walidacja: Czy trzymam dodatek?
        if (cursor == null || cursor.getAmount() == 0 || !cursor.hasItemMeta()) return;
        String attachmentId = cursor.getItemMeta().getPersistentDataContainer().get(manager.KEY_ID, PersistentDataType.STRING);
        if (attachmentId == null) return;

        // Walidacja: Czy klikam w broń?
        if (current == null || !WeaponsPlugin.getInstance().getWeaponManager().isWeapon(current)) return;

        // Pobranie danych dodatku
        AttachmentManager.AttachmentData data = manager.getData(attachmentId);
        if (data == null) return;
        
        // Pobranie ID broni (żeby sprawdzić overrides!)
        // Zakładam, że WeaponManager ma metodę getWeapon(ItemStack) - twoje pliki pokazują, że ma.
        Weapon weaponObj = WeaponsPlugin.getInstance().getWeaponManager().getWeapon(current);
        String weaponId = (weaponObj != null) ? weaponObj.getName() : "unknown";

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();

        // Modyfikacja broni
        ItemMeta meta = current.getItemMeta();
        
        // Sprawdź czy broń już ma ten dodatek (po Lore)
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        if (lore.contains(data.displayLore())) {
            player.sendMessage("§cTa broń ma już ten dodatek!");
            return;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();

        // 🟢 OBLICZANIE WARTOŚCI (Tu sprawdzamy overrides dla snajperki itp.)
        int addedClip = (int) manager.getModifierValue(attachmentId, "clip-size", weaponId);
        double addedRecoil = manager.getModifierValue(attachmentId, "recoil", weaponId);
        double addedSpread = manager.getModifierValue(attachmentId, "spread", weaponId);
        double addedDamage = manager.getModifierValue(attachmentId, "damage", weaponId);

        // Aplikowanie do NBT (Dodajemy do tego co już jest)
        if (addedClip != 0) {
            int currentVal = container.getOrDefault(MOD_CLIP, PersistentDataType.INTEGER, 0);
            container.set(MOD_CLIP, PersistentDataType.INTEGER, currentVal + addedClip);
        }
        if (addedRecoil != 0) {
            double currentVal = container.getOrDefault(MOD_RECOIL, PersistentDataType.DOUBLE, 0.0);
            container.set(MOD_RECOIL, PersistentDataType.DOUBLE, currentVal + addedRecoil);
        }
        // ... reszta (spread, damage) analogicznie ...

        // Dodanie Lore (Wygląd)
        lore.add(data.displayLore());
        meta.setLore(lore);
        
        current.setItemMeta(meta);

        // Zabranie dodatku
        cursor.setAmount(cursor.getAmount() - 1);
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1.5f);
        player.sendMessage("§aZainstalowano: " + data.displayLore());
    }
}