package com.bartilibiaz.weaponsplugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.bartilibiaz.weaponsplugin.WeaponsPlugin;

public class WeaponsCommand implements CommandExecutor {

    private WeaponsPlugin plugin;

    public WeaponsCommand(WeaponsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Sprawdź czy to gracz
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cTa komenda jest tylko dla graczy!");
            return true;
        }

        Player player = (Player) sender;

        // Sprawdź permisję
        if (!player.hasPermission("weapons.admin")) {
            player.sendMessage("§cNie masz uprawnień do tej komendy!");
            return true;
        }

        // Brak argumentów
        if (args.length == 0) {
            player.sendMessage("§6========== WEAPONS PLUGIN ==========");
            player.sendMessage("§e/weaponsZ reload §7- Przeładuj wszystkie bronie z plików YAML");
            player.sendMessage("§6====================================");
            return true;
        }

        // ========== /weaponsZ reload ==========
        if (args[0].equalsIgnoreCase("reload")) {
            player.sendMessage("§e[WEAPONS] Przeładowuję bronie...");

            try {
                // ✅ Wyczyszcz stare bronie
                plugin.getWeaponManager().getAllWeapons().clear();

                // ✅ Załaduj nowe bronie z plików YAML
                plugin.getWeaponManager().loadAllWeapons();

                player.sendMessage("§a✓ Bronie załadowane pomyślnie!");
                player.sendMessage("§a✓ Ilość broni: " + plugin.getWeaponManager().getAllWeapons().size());

            } catch (Exception e) {
                player.sendMessage("§c✗ Błąd podczas przeładowania!");
                player.sendMessage("§c" + e.getMessage());
                plugin.getLogger().severe("Błąd przy reloadzie broni: " + e.getMessage());
                e.printStackTrace();
            }

            return true;
        }

        // Nieznana komenda
        player.sendMessage("§cNieznana podkomenda! Użyj §e/weaponsZ reload");
        return true;
    }
}
