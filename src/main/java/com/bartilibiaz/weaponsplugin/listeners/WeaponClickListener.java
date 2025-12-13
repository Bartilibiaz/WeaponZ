package com.bartilibiaz.weaponsplugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import com.bartilibiaz.weaponsplugin.WeaponsPlugin;
import com.bartilibiaz.weaponsplugin.weapons.Weapon;
import com.bartilibiaz.weaponsplugin.weapons.DynamicWeapon;
import java.util.HashMap;
import java.util.Map;

public class WeaponClickListener implements Listener {

    private WeaponsPlugin plugin;
    private Map<String, Long> singleShotCooldown = new HashMap<>();  // ✅ Cooldown dla Left Click
    private final Map<Player, Integer> recoilStep = new HashMap<>();
    private final Map<Player, Long> lastShotTime = new HashMap<>();

    public WeaponClickListener(WeaponsPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * ✅ NOWY: Obsługuje Left Click (Single Shot) i Right Click (Full Auto + Camera Teleport)
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        // ========== LEFT CLICK = SINGLE SHOT + COOLDOWN ==========
        if (event.getAction() == Action.LEFT_CLICK_AIR || 
            event.getAction() == Action.LEFT_CLICK_BLOCK) {
            
            ItemStack weapon = player.isSneaking() ? offHand : mainHand;

            // ❗ JEŚLI TO NIE BROŃ → NIE ANULUJ → MOŻNA KOPAĆ
            if (!isWeapon(weapon) && !isWeaponOffhand(weapon)) {
                return;
            }
        
            event.setCancelled(true); // ✅ TYLKO DLA BRONI
            
            // ✅ Sprawdź cooldown
            String playerKey = player.getName();
            long currentTime = System.currentTimeMillis();
            long lastShot = singleShotCooldown.getOrDefault(playerKey, 0L);
            
            // Single Shot Cooldown = 500ms (0.5s)
            long cooldownMs = 500;
            
            if (currentTime - lastShot < cooldownMs) {
                long remainingMs = cooldownMs - (currentTime - lastShot);
                player.sendActionBar("§cCooldown: " + (remainingMs / 100) / 10.0 + "s");
                return;
            }
            
            // ✅ STRZAŁ!
            Weapon weaponObj = isWeapon(weapon) ? getWeaponFromItem(weapon) : getWeaponFromItem(weapon);
            if (weaponObj != null) {
                plugin.getWeaponManager().shootWeapon(player, weaponObj, player.isSneaking());
            }
            
            // Update cooldown
            singleShotCooldown.put(playerKey, currentTime);
            return;
        }

        // ========== RIGHT CLICK = FULL AUTO + CAMERA TELEPORT ==========
        if (event.getAction() == Action.RIGHT_CLICK_AIR || 
            event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            
            ItemStack weapon = player.isSneaking() ? offHand : mainHand;

            // ❗ NIE BROŃ → POZWÓL STAWIAĆ BLOKI
            if (!isWeapon(weapon) && !isWeaponOffhand(weapon)) {
                return;
            }
            Weapon weaponObj2 = getWeaponFromItem(weapon);
            if (weaponObj2 == null) return;

            if (weaponObj2 instanceof DynamicWeapon dw &&
                !dw.getConfig().isRightClickEnabled()) {
                return;
            }

            event.setCancelled(true); // ✅ tylko broń
            
            // ✅ FULL AUTO STRZAŁ!
            Weapon weaponObj = isWeapon(weapon) ? getWeaponFromItem(weapon) : getWeaponFromItem(weapon);
            if (weaponObj != null) {
                plugin.getWeaponManager().shootWeapon(player, weaponObj, player.isSneaking());
                
                // ✅ NOWE: Camera Teleport Effect!
                applyWeaponRecoil(player, weaponObj);
            }
            return;
        }
    }

    /**
     * ✅ NOWE: Camera Teleport - zmienia pozycję kamery gracza
     * Efekt "recoil" - kamera się cofa przy strzale
     */
    private void applyWeaponRecoil(Player player, Weapon weapon) {
        if (!(weapon instanceof DynamicWeapon dw)) return;

        var cfg = dw.getConfig();

        long now = System.currentTimeMillis();
        long last = lastShotTime.getOrDefault(player, 0L);

        // Reset sprayu po przerwie
        if (now - last > cfg.getRecoilResetTimeMs()) {
            recoilStep.put(player, 0);
        }

        lastShotTime.put(player, now);

        int step = recoilStep.getOrDefault(player, 0) + 1;
        recoilStep.put(player, step);

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) return;

            var loc = player.getLocation();

            float vertical =
                    cfg.getRecoilVerticalBase() +
                    (step * cfg.getRecoilVerticalGrowth());

            float horizontal =
                    cfg.getRecoilHorizontalBase() +
                    (step * cfg.getRecoilHorizontalGrowth());

            float yawOffset = (float) ((Math.random() - 0.5) * horizontal);

            loc.setPitch(Math.max(-90f, loc.getPitch() - vertical));
            loc.setYaw(loc.getYaw() + yawOffset);

            player.teleport(loc);
        });
    }


    /**
     * Pobiera broń z WeaponManager na podstawie Custom Model Data
     */
    private Weapon getWeaponFromItem(ItemStack item) {
        if (item == null || item.getType().isAir()) return null;
        try {
            var meta = item.getItemMeta();
            if (meta == null) return null;
            int cmd = meta.getCustomModelData();
            
            Map<String, Weapon> allWeapons = new HashMap<>(plugin.getWeaponManager().getAllWeapons());
            
            for (Weapon weapon : allWeapons.values()) {
                if (weapon instanceof DynamicWeapon) {
                    DynamicWeapon dw = (DynamicWeapon) weapon;
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
     */
    private boolean isWeapon(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        try {
            var meta = item.getItemMeta();
            if (meta == null) return false;
            
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
     */
    private boolean isWeaponOffhand(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        try {
            var meta = item.getItemMeta();
            if (meta == null) return false;
            
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
}