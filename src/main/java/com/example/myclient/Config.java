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

    public static void load() {
        if (!Files.exists(FILE)) return;
        Properties props = new Properties();
        try (Reader reader = Files.newBufferedReader(FILE)) {
            props.load(reader);
        } catch (IOException e) {
            System.err.println("[Aurix] Nie udalo sie wczytac ustawien: " + e.getMessage());
            return;
        }
        for (Module m : Modules.ALL) {
            String value = props.getProperty(key(m));
            if (value != null) {
                m.enabled = Boolean.parseBoolean(value.trim());
            }
        }
    }

    public static void save() {
        Properties props = new Properties();
        for (Module m : Modules.ALL) {
            props.setProperty(key(m), Boolean.toString(m.enabled));
        }
        try (Writer writer = Files.newBufferedWriter(FILE)) {
            props.store(writer, "Aurix Client - ustawienia modulow");
        } catch (IOException e) {
            System.err.println("[Aurix] Nie udalo sie zapisac ustawien: " + e.getMessage());
        }
    }
}
