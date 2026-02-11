package com.bartilibiaz.weaponzstats.gui;

import com.bartilibiaz.weaponzstats.stats.StatsManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class StatsGUI {

    private final StatsManager statsManager;

    public StatsGUI(StatsManager statsManager) {
        this.statsManager = statsManager;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(
                null,
                27,
                "§8WeaponZ §7Stats"
        );

        // 🔫 KILLS
        inv.setItem(11, createItem(
                Material.IRON_SWORD,
                "§cKills",
                List.of("§7Zabójstwa: §f" + statsManager.getKills(player))
        ));

        // 🎯 SHOTS
        inv.setItem(13, createItem(
                Material.BOW,
                "§eShots",
                List.of("§7Oddane strzały: §f" + statsManager.getShots(player))
        ));

        // Możesz dodać więcej statystyk tutaj...

        player.openInventory(inv);
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}