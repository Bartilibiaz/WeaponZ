
package com.bartilibiaz.weaponsplugin.api.events;

import com.bartilibiaz.weaponsplugin.weapons.Weapon;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class WeaponShootEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Player shooter;
    private final ItemStack weapon;

    public WeaponShootEvent(Player shooter, ItemStack weapon) {
        this.shooter = shooter;
        this.weapon = weapon;
    }

    public Player getShooter() {
        return shooter;
    }

    public ItemStack getWeapon() {
        return weapon;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}

