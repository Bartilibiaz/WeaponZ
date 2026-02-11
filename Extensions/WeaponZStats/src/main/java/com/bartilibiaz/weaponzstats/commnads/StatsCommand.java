package com.bartilibiaz.weaponzstats.commnads; // ⚠️ Ważne: package musi być pierwszy

import com.bartilibiaz.weaponzstats.stats.StatsManager;
import com.bartilibiaz.weaponzstats.gui.StatsGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatsCommand implements CommandExecutor {

    private final StatsManager stats;

    public StatsCommand(StatsManager stats) {
        this.stats = stats;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Tylko gracze mogą używać tej komendy!");
            return true;
        }
        Player player = (Player) sender;

        // Otwieramy GUI
        StatsGUI gui = new StatsGUI(stats);
        gui.open(player);

        return true;
    }
}