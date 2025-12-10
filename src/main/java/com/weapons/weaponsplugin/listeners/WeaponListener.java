package com.weapons.weaponsplugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import com.weapons.weaponsplugin.WeaponsPlugin;
import com.weapons.weaponsplugin.weapons.Weapon;
import com.weapons.weaponsplugin.weapons.DynamicWeapon;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WeaponListener implements Listener {

    private WeaponsPlugin plugin;
    private Map<String, Boolean> playerShiftState = new HashMap<>();
    private Map<String, Boolean> playerReloading = new HashMap<>();  // ✅ NOWY: track reload status

    public WeaponListener(WeaponsPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Obsługuje trzymanie Shifta - dynamiczne przesunięcie broni
     * ⚠️ WAŻNE: Nie przerzucaj jeśli gracz przeładowuje!
     */
    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        boolean isSneaking = event.isSneaking();
        
        // ✅ FIX: Sprawdź czy gracz przeładowuje!
        if (playerReloading.getOrDefault(player.getName(), false)) {
            return;  // Nie przerzucaj podczas reloadu!
        }

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        // ========== GRACZ ZACZYNA TRZYMAĆ SHIFT ==========
        if (isSneaking && isWeapon(mainHand)) {
            Weapon weapon = getWeaponFromItem(mainHand);
            if (weapon == null) return;
            
            DynamicWeapon dynamicWeapon = (DynamicWeapon) weapon;
            
            // Pobierz amunicję z mainhand
            String ammoLore = dynamicWeapon.getAmmoCopyFromLore(mainHand);
            
            // Utwórz offhand broń
            ItemStack offhandVersion = dynamicWeapon.createOffhandItem();
            
            // Skopiuj amunicję do offhand
            if (ammoLore != null) {
                dynamicWeapon.setSurvivalAmmoLore(offhandVersion, ammoLore);
            }

            player.getInventory().setItemInMainHand(offHand);
            player.getInventory().setItemInOffHand(offhandVersion);

            player.playSound(player.getLocation(), "item.armor.equip_generic", 0.5f, 1.0f);
            
            playerShiftState.put(player.getName(), true);
            return;
        }

        // ========== GRACZ PUSZCZA SHIFT (BYŁ W OFFHAND) ==========
        if (!isSneaking && isWeaponOffhand(offHand)) {
            Weapon weapon = getWeaponFromItem(offHand);
            if (weapon == null) return;
            
            DynamicWeapon dynamicWeapon = (DynamicWeapon) weapon;
            
            // Pobierz amunicję z offhand
            String ammoLore = dynamicWeapon.getAmmoCopyFromLore(offHand);
            
            // Utwórz mainhand broń
            ItemStack mainhandVersion = dynamicWeapon.createItem();
            
            // Skopiuj amunicję do mainhand
            if (ammoLore != null) {
                dynamicWeapon.setSurvivalAmmoLore(mainhandVersion, ammoLore);
            }

            player.getInventory().setItemInMainHand(mainhandVersion);
            player.getInventory().setItemInOffHand(mainHand);

            player.playSound(player.getLocation(), "item.armor.equip_generic", 0.5f, 1.0f);
            
            playerShiftState.put(player.getName(), false);
            return;
        }

        playerShiftState.put(player.getName(), isSneaking);
    }

    /**
     * Obsługuje prawy klik - strzał LUB F - reload
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        // ========== F BUTTON (PHYSICAL) = RELOAD (BEZ PRZESUNIĘCIA!) ==========
        if (event.getAction() == Action.PHYSICAL) {
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            ItemStack offHand = player.getInventory().getItemInOffHand();
            
            // Sprawdź czy trzyma broń w którejkolwiek ręce
            if (isWeapon(mainHand) || isWeaponOffhand(offHand)) {
                event.setCancelled(true);  // ⚠️ WAŻNE: Blokuj domyślne F (swap)
                
                Weapon weapon = isWeapon(mainHand) ? getWeaponFromItem(mainHand) : getWeaponFromItem(offHand);
                if (weapon == null) return;
                
                // ✅ FIX: Ustaw flag reloading PRZED callowaniem reload!
                playerReloading.put(player.getName(), true);
                
                // Uruchom reload
                weapon.onReload(player);
                
                // ✅ FIX: Wyczyść flag po 5 sekundach (max reload time)
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    playerReloading.put(player.getName(), false);
                }, 100L);  // 5 sekund = 100 ticków
            }
            return;
        }
        
        // Tylko prawy klik w powietrze lub w blok
        if (event.getAction() != Action.RIGHT_CLICK_AIR && 
            event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        // ========== STRZAŁ Z MAINHAND (bez Shifta) ==========
        if (isWeapon(mainHand) && !player.isSneaking()) {
            event.setCancelled(true);
            
            Weapon weapon = getWeaponFromItem(mainHand);
            if (weapon != null) {
                plugin.getWeaponManager().shootWeapon(player, weapon, false);
            }
            return;
        }

        // ========== STRZAŁ Z OFFHAND (na Shifcie) ==========
        if (isWeaponOffhand(offHand) && player.isSneaking()) {
            event.setCancelled(true);
            
            Weapon weapon = getWeaponFromItem(offHand);
            if (weapon != null) {
                plugin.getWeaponManager().shootWeapon(player, weapon, true);
            }
            return;
        }
    }

    /**
     * Blokuj wyrzucanie snowball'a z custom model data (magazynki)
     * ⚠️ WAŻNE: Kopiuj array przed iteracją!
     */
    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof Snowball) {
            Snowball snowball = (Snowball) event.getEntity();
            
            if (snowball.getShooter() instanceof Player) {
                Player player = (Player) snowball.getShooter();
                
                ItemStack mainHand = player.getInventory().getItemInMainHand();
                ItemStack offHand = player.getInventory().getItemInOffHand();
                
                // Sprawdź czy wyrzucana snowball to magazynek (CMD 3)
                if (isAmmoBall(mainHand) || isAmmoBall(offHand)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    /**
     * Obsługuje zmianę wybranego slotu
     */
    @EventHandler
    public void onPlayerHeldItemChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());

        if (!isWeapon(newItem)) {
            playerShiftState.remove(player.getName());
        }
    }

    /**
     * Pobiera broń z WeaponManager na podstawie Custom Model Data
     * ⚠️ WAŻNE: Używaj getAllWeapons() - thread-safe!
     */
    private Weapon getWeaponFromItem(ItemStack item) {
        if (item == null || item.getType().isAir()) return null;
        try {
            var meta = item.getItemMeta();
            if (meta == null) return null;
            int cmd = meta.getCustomModelData();
            
            // ⚠️ Kopiuj Map! Szukaj broni z tym mainhand CMD
            Map<String, Weapon> allWeapons = new HashMap<>(plugin.getWeaponManager().getAllWeapons());
            
            for (Weapon weapon : allWeapons.values()) {
                if (weapon instanceof DynamicWeapon) {
                    DynamicWeapon dw = (DynamicWeapon) weapon;
                    // Porównaj z konfiguracją
                    if (dw.getConfig().getMainhandCmd() == cmd || dw.getConfig().getOffhandCmd() == cmd) {
                        return weapon;
                    }
                }
            }
        } catch (Exception e) {
            // Skip
        }
        return null;
    }

    /**
     * Sprawdza czy item to broń w głównej ręce
     * ⚠️ WAŻNE: Używaj kopii Map!
     */
    private boolean isWeapon(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        try {
            var meta = item.getItemMeta();
            if (meta == null) return false;
            
            // ⚠️ Kopiuj Map! Szukaj broni z tym CMD
            Map<String, Weapon> allWeapons = new HashMap<>(plugin.getWeaponManager().getAllWeapons());
            
            for (Weapon weapon : allWeapons.values()) {
                if (weapon instanceof DynamicWeapon) {
                    DynamicWeapon dw = (DynamicWeapon) weapon;
                    if (dw.getConfig().getMainhandCmd() == meta.getCustomModelData()) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    /**
     * Sprawdza czy item to broń w drugiej ręce
     * ⚠️ WAŻNE: Używaj kopii Map!
     */
    private boolean isWeaponOffhand(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        try {
            var meta = item.getItemMeta();
            if (meta == null) return false;
            
            // ⚠️ Kopiuj Map! Szukaj broni z tym CMD
            Map<String, Weapon> allWeapons = new HashMap<>(plugin.getWeaponManager().getAllWeapons());
            
            for (Weapon weapon : allWeapons.values()) {
                if (weapon instanceof DynamicWeapon) {
                    DynamicWeapon dw = (DynamicWeapon) weapon;
                    if (dw.getConfig().getOffhandCmd() == meta.getCustomModelData()) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    /**
     * Sprawdza czy item to magazynek (snowball CMD 3)
     * ⚠️ WAŻNE: Używaj kopii Map!
     */
    private boolean isAmmoBall(ItemStack item) {
        if (item == null || item.getType() != org.bukkit.Material.SNOWBALL) return false;
        try {
            var meta = item.getItemMeta();
            if (meta == null) return false;
            
            // ⚠️ Kopiuj Map! Szukaj magazynku po CMD
            Map<String, Weapon> allWeapons = new HashMap<>(plugin.getWeaponManager().getAllWeapons());
            
            for (Weapon weapon : allWeapons.values()) {
                if (weapon instanceof DynamicWeapon) {
                    DynamicWeapon dw = (DynamicWeapon) weapon;
                    if (dw.getConfig().getAmmoCmd() == meta.getCustomModelData()) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}