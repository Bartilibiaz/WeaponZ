package com.bartilibiaz.weaponzattachments;

import com.bartilibiaz.weaponsplugin.WeaponsPlugin;
import com.bartilibiaz.weaponsplugin.api.WeaponZExtension;
import com.bartilibiaz.weaponzattachments.managers.AttachmentManager;
import com.bartilibiaz.weaponzattachments.listeners.AttachmentListener;
import com.bartilibiaz.weaponzattachments.commands.GiveAttachmentCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class WeaponZAttachmentsPlugin extends JavaPlugin implements WeaponZExtension {

    private AttachmentManager attachmentManager;

    @Override
    public void onEnable() {
        saveResource("attachments.yml", false);
        
        if (WeaponsPlugin.getInstance() != null) {
            // Inicjalizacja managera
            this.attachmentManager = new AttachmentManager(this);
            
            // Rejestracja listenera (Drag & Drop)
            getServer().getPluginManager().registerEvents(new AttachmentListener(this, attachmentManager), this);
            
            // Rejestracja w API
            WeaponsPlugin.getInstance().registerExtension(this);
            
            // Komenda do dawania dodatków (opcjonalnie)
            getCommand("giveattachment").setExecutor(new GiveAttachmentCommand(attachmentManager));
        }
    }

    @Override
    public void onEnable(WeaponsPlugin plugin) {} // API method

    @Override
    public String getExtensionName() { return "WeaponZAttachments"; }

    @Override
    public String getVersion() { return "1.0.0"; }
}