package com.example.myclient;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class Config {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("aurix-client.properties");

    private Config() {}

    private static String key(Module m) {
        return m.name.toLowerCase().replaceAll("[^a-z0-9]+", "_");
    }

    private static int readInt(Properties props, String k, int fallback) {
        String v = props.getProperty(k);
        if (v == null) return fallback;
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public static void load() {
        if (!Files.exists(FILE)) return;
        Properties props = new Properties();
        try (Reader reader = Files.newBufferedReader(FILE)) {
            props.load(reader);
        } catch (IOException e) {
            System.err.println("[Aurix] Nie udalo sie wczytac ustawien: " + e.getMessage());
            return;
        }
        String savedCape = props.getProperty("cape", "none").trim();
        Cosmetics.cape = Cosmetics.isValid(Cosmetics.CAPES, savedCape) ? savedCape : "none";
        String savedBack = props.getProperty("backwings", "none").trim();
        Cosmetics.backWings = Cosmetics.isValid(Cosmetics.BACK_WINGS, savedBack) ? savedBack : "none";
        String savedWings = props.getProperty("wings", "none").trim();
        Cosmetics.wings = Cosmetics.isValid(Cosmetics.WINGS, savedWings) ? savedWings : "none";
        Profile.accounts.clear();
        String savedAccounts = props.getProperty("accounts", "");
        for (String part : savedAccounts.split(",")) {
            String n = part.trim();
            if (Profile.isValidName(n) && !Profile.accounts.contains(n)) {
                Profile.accounts.add(n);
            }
        }
        String savedActive = props.getProperty("active", "").trim();
        Profile.active = Profile.accounts.contains(savedActive) ? savedActive : "";
        boolean positionsValid = "2".equals(props.getProperty("config_version"));
        for (Module m : Modules.ALL) {
            String value = props.getProperty(key(m));
            if (value != null) {
                m.enabled = Boolean.parseBoolean(value.trim());
            }
            if (m.hud && positionsValid) {
                m.x = readInt(props, key(m) + "_x", m.defX);
                m.y = readInt(props, key(m) + "_y", m.defY);
            }
            String fav = props.getProperty(key(m) + "_fav");
            if (fav != null) {
                m.favorite = Boolean.parseBoolean(fav.trim());
            }
        }
    }

    public static void save() {
        Properties props = new Properties();
        props.setProperty("config_version", "2");
        props.setProperty("cape", Cosmetics.cape);
        props.setProperty("wings", Cosmetics.wings);
        props.setProperty("backwings", Cosmetics.backWings);
        props.setProperty("accounts", String.join(",", Profile.accounts));
        props.setProperty("active", Profile.active);
        for (Module m : Modules.ALL) {
            props.setProperty(key(m), Boolean.toString(m.enabled));
            props.setProperty(key(m) + "_fav", Boolean.toString(m.favorite));
            if (m.hud) {
                props.setProperty(key(m) + "_x", Integer.toString(m.x));
                props.setProperty(key(m) + "_y", Integer.toString(m.y));
            }
        }
        try (Writer writer = Files.newBufferedWriter(FILE)) {
            props.store(writer, "Aurix Client - ustawienia modulow");
        } catch (IOException e) {
            System.err.println("[Aurix] Nie udalo sie zapisac ustawien: " + e.getMessage());
        }
    }
}
