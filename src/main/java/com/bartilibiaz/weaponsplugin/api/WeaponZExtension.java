package com.bartilibiaz.weaponsplugin.api;

import com.bartilibiaz.weaponsplugin.WeaponsPlugin;

public interface WeaponZExtension {
    void onEnable(WeaponsPlugin weaponZ);

    /**
     * Called when WeaponZ is disabled
     */
    void onDisable();
    default void onReload(){};
    String getExtensionName();

    /** Extension version shown in console */
    String getVersion();
}


