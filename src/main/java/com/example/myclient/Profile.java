package com.example.myclient;

import com.example.myclient.mixin.MinecraftClientAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Konta offline (jak w In-Game Account Switcher). Zmieniaja nick w grze dla serwerow
 * bez weryfikacji (offline-mode), sieci LAN i gry jednoosobowej. Serwery online-mode
 * i tak wymagaja prawdziwego konta, wiec nie da sie na nich wejsc cudzym nickiem.
 */
public final class Profile {
    /** Zapisane nicki offline. */
    public static final List<String> accounts = new ArrayList<>();
    /** Aktywny nick offline; pusty = konto oryginalne. */
    public static String active = "";

    private static Session original = null;

    private Profile() {}

    public static boolean isValidName(String name) {
        return name != null && name.matches("[A-Za-z0-9_]{3,16}");
    }

    private static void rememberOriginal(MinecraftClient mc) {
        if (original == null) {
            original = mc.getSession();
        }
    }

    public static String originalName(MinecraftClient mc) {
        return original != null ? original.getUsername() : mc.getSession().getUsername();
    }

    /** Aktualnie uzywany nick (z sesji). */
    public static String displayName(MinecraftClient mc) {
        if (mc != null && mc.getSession() != null) return mc.getSession().getUsername();
        return "Gracz";
    }

    public static void applyOffline(MinecraftClient mc, String name) {
        if (!isValidName(name)) return;
        rememberOriginal(mc);
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        Session session = new Session(name, uuid, "0", Optional.empty(), Optional.empty(), Session.AccountType.LEGACY);
        ((MinecraftClientAccessor) mc).aurix$setSession(session);
        active = name;
    }

    public static void restoreOriginal(MinecraftClient mc) {
        if (original != null) {
            ((MinecraftClientAccessor) mc).aurix$setSession(original);
        }
        active = "";
    }
}
