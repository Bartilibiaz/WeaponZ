package com.bartilibiaz.weaponzattachments.commands; // Upewnij się co do pakietu

import com.bartilibiaz.weaponzattachments.managers.AttachmentManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GiveAttachmentCommand implements CommandExecutor {

    private final AttachmentManager attachmentManager;

    public GiveAttachmentCommand(AttachmentManager attachmentManager) {
        this.attachmentManager = attachmentManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Tylko dla graczy!");
            return true;
        }

        if (!player.hasPermission("weaponz.admin")) {
            player.sendMessage("§cBrak uprawnień.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§cPodaj ID dodatku! (np. extended_mag_1)");
            return true;
        }

        String attachmentId = args[0];
        ItemStack item = attachmentManager.getAttachmentItem(attachmentId);

        if (item == null) {
            player.sendMessage("§cTaki dodatek nie istnieje w attachments.yml!");
            return true;
        }

        player.getInventory().addItem(item);
        player.sendMessage("§aOtrzymano dodatek: " + attachmentId);
        return true;
    }
}