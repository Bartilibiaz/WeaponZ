import com.bartilibiaz.weaponzstats.stats.StatsManager;
import com.bartilibiaz.weaponzstats.gui.StatsGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
public class StatsCommand implements CommandExecutor {

    private final StatsManager stats;

    public StatsCommand(StatsManager stats) {
        this.stats = stats;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Tylko gracze mogą używać tej komendy!");
            return true;
        }
    
        StatsGUI gui = new StatsGUI(stats);
    
        // Wypełnij inventory statystykami broni
        Inventory inv = Bukkit.createInventory(null, 27, "§8WeaponZ §7Stats");
    
        ItemStack weaponStats = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = weaponStats.getItemMeta();
        meta.setDisplayName("§bZabójstwa broniami");
    
        List<String> lore = new ArrayList<>();
        for (var entry : stats.getWeaponKills(player).entrySet()) {
            lore.add("§7" + entry.getKey() + ": §f" + entry.getValue());
        }
        if (lore.isEmpty()) lore.add("§7Brak danych");
    
        meta.setLore(lore);
        weaponStats.setItemMeta(meta);
    
        inv.setItem(11, weaponStats);
    
        // Jeśli chcesz dodać kills / deaths / shots:
        inv.setItem(13, new ItemStack(Material.BOW)); // przykładowy slot dla strzałów
        inv.setItem(15, new ItemStack(Material.RED_BED)); // przykładowy slot dla deaths
    
        player.openInventory(inv);
    
        return true;
    }

    private ItemStack item(Material mat, String name, String lore) {
        ItemStack i = new ItemStack(mat);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(name);
        m.setLore(List.of(lore));
        i.setItemMeta(m);
        return i;
    }
}
