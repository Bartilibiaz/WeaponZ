package com.bartilibiaz.weaponsplugin.api.events;

import com.bartilibiaz.weaponsplugin.weapons.Weapon;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class WeaponKillPlayerEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Player killer;
    private final Player victim;
    private final Weapon weapon;

    public WeaponKillPlayerEvent(Player killer, Player victim, Weapon weapon) {
        this.killer = killer;
        this.victim = victim;
        this.weapon = weapon;
    }

    public Player getKiller() {
        return killer;
    }

    public Player getVictim() {
        return victim;
    }

    public Weapon getWeapon() {
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
