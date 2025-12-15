package com.bartilibiaz.weaponsplugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import com.bartilibiaz.weaponsplugin.WeaponsPlugin;
import com.bartilibiaz.weaponsplugin.weapons.Weapon;
import com.bartilibiaz.weaponsplugin.weapons.DynamicWeapon;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WeaponListener implements Listener {

    private WeaponsPlugin plugin;
    private Map<String, Boolean> playerShiftState = new HashMap<>();
    public static  Map<String, Boolean> playerReloading = new HashMap<>();  // ✅ NOWY: track reload status
    private Map<String, Integer> playerReloadTaskId = new HashMap<>();
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
        

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        // ========== GRACZ ZACZYNA TRZYMAĆ SHIFT ==========
        if (isSneaking && isWeapon(mainHand)) {
            Weapon weapon = getWeaponFromItem(mainHand);
            if (weapon == null) return;
            if (playerReloading.getOrDefault(player.getName(), false)) {
                return;  // Nie przerzucaj podczas reloadu!
            }
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
     * Handles the F key press for reloading
     */
    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHand = event.getMainHandItem();
        ItemStack offHand = event.getOffHandItem();
    
        // Jeśli gracz nie trzyma broni w żadnej ręce – wyjdź
        if (!isWeapon(mainHand) && !isWeaponOffhand(offHand)) return;
    
        // Anuluj domyślną zmianę ręki
        event.setCancelled(true);
    
        // Pobierz broń
        DynamicWeapon weapon = isWeapon(mainHand) ? (DynamicWeapon) getWeaponFromItem(mainHand)
                                                  : (DynamicWeapon) getWeaponFromItem(offHand);
        if (weapon == null) return;
    
        // Anuluj reload, jeśli trwa
        cancelReload(player);
    
        // Wywołaj reload (DynamicWeapon sam ustawia ammo, dźwięki, progress bar)
        weapon.onReload(player);
    }


    private void cancelReload(Player player) {
        if (playerReloading.getOrDefault(player.getName(), false)) {
            Integer taskId = playerReloadTaskId.get(player.getName());
            if (taskId != null) Bukkit.getScheduler().cancelTask(taskId);
        
            playerReloading.put(player.getName(), false);
            playerReloadTaskId.remove(player.getName());
            player.sendActionBar("§cReload cancelled!");
        }
    }

    /**
     * Obsługuje prawy klik - strzał LUB F - reload
     */
    

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