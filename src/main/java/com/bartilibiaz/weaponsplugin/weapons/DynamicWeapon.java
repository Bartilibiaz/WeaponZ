package com.bartilibiaz.weaponsplugin.weapons;

import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import com.bartilibiaz.weaponsplugin.WeaponsPlugin;
import com.bartilibiaz.weaponsplugin.config.WeaponConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamicWeapon extends Weapon {
    
    private WeaponConfig config;
    private WeaponsPlugin plugin;
    private Map<String, BukkitTask> reloadTasks = new HashMap<>();
    
    public DynamicWeapon(WeaponConfig config) {
        super(
            config.getName(),
            config.getMainhandCmd(),
            config.getOffhandCmd(),
            config.getSpreadNormal(),
            config.getSpreadScoped(),
            (float) config.getDamage(),  // ✅ FIX: Cast double na float
            config.getRange()
        );
        this.config = config;
        this.plugin = WeaponsPlugin.getInstance();
    }
    
    // ✅ GETTER do WeaponConfig
    public WeaponConfig getConfig() {
        return config;
    }
    
    @Override
    public ItemStack createItem() {
        try {
            Material material = Material.valueOf(config.getMainhandType());
            ItemStack item = new ItemStack(material);
            var meta = item.getItemMeta();
            
            if (meta == null) return new ItemStack(Material.STICK);
            
            meta.setCustomModelData(config.getMainhandCmd());
            meta.setDisplayName(config.getDisplayName());
            
            var lore = new ArrayList<String>();
            lore.add("§7Ammo: §a" + config.getMagazineSize() + "/" + config.getMagazineSize());
            meta.setLore(lore);
            
            item.setItemMeta(meta);
            return item;
        } catch (Exception e) {
            plugin.getLogger().severe("Błąd tworzenia itemu dla: " + config.getName());
            return new ItemStack(Material.STICK);
        }
    }
    
    public ItemStack createOffhandItem() {
        try {
            Material material = Material.valueOf(config.getOffhandType());
            ItemStack item = new ItemStack(material);
            var meta = item.getItemMeta();
            
            if (meta == null) return new ItemStack(Material.STICK);
            
            meta.setCustomModelData(config.getOffhandCmd());
            meta.setDisplayName(config.getDisplayName() + " §8(Left Hand)");
            
            var lore = new ArrayList<String>();
            lore.add("§7Ammo: §a" + config.getMagazineSize() + "/" + config.getMagazineSize());
            meta.setLore(lore);
            
            item.setItemMeta(meta);
            return item;
        } catch (Exception e) {
            plugin.getLogger().severe("Błąd tworzenia offhand itemu dla: " + config.getName());
            return new ItemStack(Material.STICK);
        }
    }
    
    @Override
    public void onShoot(Player player, boolean isScoped) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        
        // Określ którą rękę trzymasz
        ItemStack weapon = player.isSneaking() ? offHand : mainHand;
        
        // Sprawdź czy gracz ma amunicję w broni
        int currentAmmo = getWeaponAmmo(weapon);  // ✅ FIX: Bez parametru config.getMagazineSize()
        if (currentAmmo <= 0) {
            player.sendActionBar("§cBrak amunicji! [0/" + config.getMagazineSize() + "]");
            player.playSound(player.getLocation(), config.getSoundEmpty(), 1.0f, 1.0f);
            return;
        }
        
        // Zmniejsz amunicję w BRONI
        currentAmmo--;
        setWeaponAmmo(weapon, currentAmmo);  // ✅ FIX: Bez parametru config.getMagazineSize()
        
        // Pobranie kierunku patrzenia gracza
        Vector direction = player.getEyeLocation().getDirection().normalize();
        
        // Rozrzut
        float spread = isScoped ? config.getSpreadScoped() : config.getSpreadNormal();
        
        // Dodanie losowego rozrzutu do kierunku
        if (spread > 0) {
            double randomX = (Math.random() - 0.5) * spread;
            double randomY = (Math.random() - 0.5) * spread;
            double randomZ = (Math.random() - 0.5) * spread;
            
            direction.add(new Vector(randomX, randomY, randomZ)).normalize();
        }
        
        // Spawn linii particli i sprawdzenie trafienia
        castRayAndCreateParticles(player, direction);
        
        // Custom dźwięk
        playCustomSound(player);
        
        // Pokaż amunicję na action barze
        player.sendActionBar("§a[" + currentAmmo + "/" + config.getMagazineSize() + "]");
    }
    
    @Override
    public void onReload(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        
        // Określ która broń ma być przeładowana
        ItemStack weapon = null;
        
        if (isWeapon(mainHand)) {
            weapon = mainHand;
        } else if (isWeaponOffhand(offHand)) {
            weapon = offHand;
        } else {
            return;
        }
        
        // Sprawdź czy broń ma już pełną amunicję
        int currentAmmo = getWeaponAmmo(weapon);
        if (currentAmmo >= config.getMagazineSize()) {
            player.sendActionBar("§cJuż pełna amunicja!");
            return;
        }
        
        // Sprawdź czy gracz ma amunicję w ekwipunku
        int ammoInInventory = countAmmoItems(player);
        if (ammoInInventory <= 0) {
            player.sendActionBar("§cBrak magazynków!");
            player.playSound(player.getLocation(), config.getSoundEmpty(), 1.0f, 1.0f);
            return;
        }
        
        // Uruchom reload z progressbar
        startReload(player, weapon);
    }
    
    /**
     * Reload z progress bar
     */
    private void startReload(Player player, ItemStack weapon) {
        String playerName = player.getName();
        double reloadTime = config.getReloadTime();
        
        // Jeśli już trwa reload, anuluj go
        if (reloadTasks.containsKey(playerName)) {
            reloadTasks.get(playerName).cancel();
        }
        
        // Progressbar: reloadTime w sekundach / 15 ticków
        final int[] progress = {0};
        final int maxProgress = 15;
        
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                progress[0]++;
                
                // Buduj progress bar: "Reloading ----------- XX%"
                int filledDashes = (progress[0] * 11) / maxProgress;
                int emptyDashes = 11 - filledDashes;
                
                StringBuilder bar = new StringBuilder();
                bar.append("§6Reloading ");
                
                // Pełne kreski
                for (int i = 0; i < filledDashes; i++) {
                    bar.append("§a-");
                }
                
                // Puste kreski
                for (int i = 0; i < emptyDashes; i++) {
                    bar.append("§7-");
                }
                
                // Procent
                int percent = (progress[0] * 100) / maxProgress;
                bar.append(String.format(" §e%d%%", percent));
                
                player.sendActionBar(bar.toString());
                
                // Reload ukończony
                if (progress[0] >= maxProgress) {
                    // Usuń magazynek ⚠️ runTask na main thread!
                    Bukkit.getScheduler().runTask(plugin, () -> removeAmmoItem(player));
                    
                    // Ustaw amunicję w broni na pełną
                    setWeaponAmmo(weapon, config.getMagazineSize());
                    
                    // Dźwięk
                    player.playSound(player.getLocation(), config.getSoundReload(), 1.0f, 0.8f);
                    player.sendActionBar("§6✓ Przeładowana! [" + config.getMagazineSize() + "/" + config.getMagazineSize() + "]");
                    
                    // Anuluj task
                    BukkitTask taskToCancel = reloadTasks.get(playerName);
                    if (taskToCancel != null) {
                        taskToCancel.cancel();
                    }
                    reloadTasks.remove(playerName);
                }
            }
        }, 0L, (long)(reloadTime * 20 / 15));  // 20 ticks = 1 sekunda
        
        reloadTasks.put(playerName, task);
    }
    
    /**
     * Tworzy raycast z białymi particlami i sprawdza trafienia
     */
    private void castRayAndCreateParticles(Player player, Vector direction) {
        Location startLocation = player.getEyeLocation();
        Vector rayDirection = direction.normalize();
        
        Location particleLocation = startLocation.clone();
        boolean hitEntity = false;
        double particleStep = 0.15;
        
        for (double distance = 0; distance < config.getRange(); distance += particleStep) {
            
            Block block = particleLocation.getBlock();
            if (block.getType().isSolid() && !block.isLiquid()) {
                hitEntity = true;
                particleLocation.getWorld().spawnParticle(
                    org.bukkit.Particle.SNOWFLAKE,
                    particleLocation,
                    5,
                    0.1, 0.1, 0.1,
                    0.05
                );
                break;
            }
            
            List<Entity> nearbyEntities = getNearbyEntities(particleLocation, 0.5);
            for (Entity entity : nearbyEntities) {
                if (entity.equals(player) || !(entity instanceof LivingEntity)) {
                    continue;
                }
                
                LivingEntity livingEntity = (LivingEntity) entity;
                livingEntity.damage(config.getDamage(), player);
                
                particleLocation.getWorld().spawnParticle(
                    org.bukkit.Particle.SNOWFLAKE,
                    particleLocation,
                    10,
                    0.2, 0.2, 0.2,
                    0.1
                );
                
                hitEntity = true;
                break;
            }
            
            if (hitEntity) break;
            
            particleLocation.add(rayDirection.clone().multiply(particleStep));
        }
    }
    
    /**
     * Pobiera wszystkie entity w promieniu
     */
    private List<Entity> getNearbyEntities(Location location, double radius) {
        List<Entity> nearby = new ArrayList<>();
        for (Entity entity : location.getWorld().getNearbyEntities(location, radius, radius, radius)) {
            nearby.add(entity);
        }
        return nearby;
    }
    
    /**
     * Odtwarza custom sound z resource packa
     */
    private void playCustomSound(Player player) {
        player.playSound(
            player.getEyeLocation(),
            config.getSoundShoot(),
            org.bukkit.SoundCategory.PLAYERS,
            1.0f,
            1.0f
        );
        
        // Dla innych graczy w pobliżu
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getWorld().equals(player.getWorld()) &&
                onlinePlayer.getLocation().distance(player.getLocation()) < 64) {
                onlinePlayer.playSound(
                    player.getEyeLocation(),
                    config.getSoundShoot(),
                    org.bukkit.SoundCategory.PLAYERS,
                    1.0f,
                    1.0f
                );
            }
        }
    }
    
    /**
     * Zlicza amunicję w ekwipunku (magazynki)
     */
    private int countAmmoItems(Player player) {
        int count = 0;
        ItemStack[] inventory = player.getInventory().getContents().clone();
        
        for (ItemStack item : inventory) {
            if (item != null && item.getType().name().equals(config.getAmmoType())) {
                try {
                    if (item.getItemMeta() != null && item.getItemMeta().getCustomModelData() == config.getAmmoCmd()) {
                        count += item.getAmount();
                    }
                } catch (Exception e) {
                    // Skip
                }
            }
        }
        return count;
    }
    
    /**
     * Usuwa jeden magazynek z ekwipunku
     */
    private void removeAmmoItem(Player player) {
        ItemStack[] inventory = player.getInventory().getContents().clone();
        
        for (int i = 0; i < inventory.length; i++) {
            ItemStack item = inventory[i];
            if (item != null && item.getType().name().equals(config.getAmmoType())) {
                try {
                    if (item.getItemMeta() != null && item.getItemMeta().getCustomModelData() == config.getAmmoCmd()) {
                        if (item.getAmount() > 1) {
                            item.setAmount(item.getAmount() - 1);
                        } else {
                            player.getInventory().removeItem(item);
                        }
                        return;
                    }
                } catch (Exception e) {
                    // Skip
                }
            }
        }
    }
    
    /**
     * Pobiera amunicję z LORE broni
     */
    private int getWeaponAmmo(ItemStack weapon) {
        if (weapon == null || weapon.getType().isAir()) return 0;
        try {
            var meta = weapon.getItemMeta();
            if (meta == null || !meta.hasLore()) return config.getMagazineSize();
            
            var lore = meta.getLore();
            if (lore == null || lore.isEmpty()) return config.getMagazineSize();
            
            for (String line : lore) {
                if (line.contains("Ammo:")) {
                    String cleaned = line.replaceAll("§.", "");
                    String[] parts = cleaned.split("/");
                    
                    if (parts.length > 0) {
                        String ammoPart = parts[0].replaceAll("[^0-9]", "");
                        if (!ammoPart.isEmpty()) {
                            return Integer.parseInt(ammoPart);
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            return config.getMagazineSize();
        }
        return config.getMagazineSize();
    }
    
    /**
     * Ustawia amunicję w LORE broni
     */
    private void setWeaponAmmo(ItemStack weapon, int ammo) {
        if (weapon == null || weapon.getType().isAir()) return;
        try {
            var meta = weapon.getItemMeta();
            if (meta == null) return;
            
            var newLore = new ArrayList<String>();
            
            if (meta.hasLore() && meta.getLore() != null) {
                for (String line : meta.getLore()) {
                    if (!line.contains("Ammo:")) {
                        newLore.add(line);
                    }
                }
            }
            
            newLore.add(0, "§7Ammo: §a" + ammo + "/" + config.getMagazineSize());
            
            meta.setLore(newLore);
            weapon.setItemMeta(meta);
        } catch (Exception e) {
            // Skip
        }
    }
    
    /**
     * Pobiera linię LORE z amunicją
     */
    public String getAmmoCopyFromLore(ItemStack weapon) {
        if (weapon == null || weapon.getType().isAir()) return null;
        try {
            var meta = weapon.getItemMeta();
            if (meta == null || !meta.hasLore()) return null;
            
            var lore = meta.getLore();
            if (lore == null || lore.isEmpty()) return null;
            
            for (String line : lore) {
                if (line.contains("Ammo:")) {
                    return line;
                }
            }
        } catch (Exception e) {
            // Skip
        }
        return null;
    }
    
    /**
     * Ustawia skopiowane LORE z amunicją
     */
    public void setSurvivalAmmoLore(ItemStack weapon, String ammoPart) {
        if (weapon == null || weapon.getType().isAir() || ammoPart == null) return;
        try {
            var meta = weapon.getItemMeta();
            if (meta == null) return;
            
            var newLore = new ArrayList<String>();
            newLore.add(ammoPart);
            
            meta.setLore(newLore);
            weapon.setItemMeta(meta);
        } catch (Exception e) {
            // Skip
        }
    }
    
    /**
     * Sprawdza czy item to ta broń w głównej ręce
     * ⚠️ MUSI BYĆ PUBLIC - override z parent klasy Weapon!
     */
    @Override
    public boolean isWeapon(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        try {
            var meta = item.getItemMeta();
            if (meta == null) return false;
            return meta.getCustomModelData() == config.getMainhandCmd();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Sprawdza czy item to ta broń w drugiej ręce
     */
    private boolean isWeaponOffhand(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        try {
            var meta = item.getItemMeta();
            if (meta == null) return false;
            return meta.getCustomModelData() == config.getOffhandCmd();
        } catch (Exception e) {
            return false;
        }
    }
}