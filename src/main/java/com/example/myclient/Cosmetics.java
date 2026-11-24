package com.example.myclient;

import net.minecraft.util.Identifier;

import java.util.List;

/** Kosmetyki widoczne tylko dla Ciebie: pelerynki i skrzydla (tekstura elytry). */
public final class Cosmetics {
    public record Option(String id, String name, int color1, int color2) {}

    public static final List<Option> CAPES = List.of(
            new Option("none", "Brak", 0xFF14193A, 0xFF14193A),
            new Option("aurix", "Aurix", 0xFF3CE6FF, 0xFF7B3FE4),
            new Option("ogien", "Ogień", 0xFFFFB020, 0xFFC81D25),
            new Option("lod", "Lód", 0xFFCFF6FF, 0xFF3A8DDE),
            new Option("noc", "Noc", 0xFF24378C, 0xFF0A0D26)
    );

    public static final List<Option> WINGS = List.of(
            new Option("none", "Domyślne", 0xFF14193A, 0xFF14193A),
            new Option("aurix", "Aurix", 0xFF3CE6FF, 0xFF7B3FE4),
            new Option("ogien", "Ogień", 0xFFFFB020, 0xFFC81D25),
            new Option("lod", "Lód", 0xFFCFF6FF, 0xFF3A8DDE),
            new Option("noc", "Noc", 0xFF24378C, 0xFF0A0D26)
    );

    public static final List<Option> BACK_WINGS = List.of(
            new Option("none", "Brak", 0xFF14193A, 0xFF14193A),
            new Option("aurix", "Aurix", 0xFF3CE6FF, 0xFF7B3FE4),
            new Option("ogien", "Ogień", 0xFFFFB020, 0xFFC81D25),
            new Option("lod", "Lód", 0xFFCFF6FF, 0xFF3A8DDE),
            new Option("noc", "Noc", 0xFF3B54C4, 0xFF0A0D26),
            new Option("aniol", "Anioł", 0xFFFFFFFF, 0xFFFFD34D)
    );

    /** Znacznik w SkinTextures.textureUrl, po ktorym rozpoznajemy lokalnego gracza. */
    public static final String LOCAL_MARKER = "aurix:local";

    public static String backWings = "none";
    public static String cape = "none";
    public static String wings = "none";

    private Cosmetics() {}

    public static boolean isValid(List<Option> list, String id) {
        for (Option o : list) {
            if (o.id().equals(id)) return true;
        }
        return false;
    }

    /** Tekstura pelerynki albo null, gdy wylaczona. */
    public static Identifier capeTexture() {
        if (cape.equals("none") || !isValid(CAPES, cape)) return null;
        return Identifier.of("myclient", "textures/cape/" + cape + ".png");
    }

    public static boolean backWingsActive() {
        return !backWings.equals("none") && isValid(BACK_WINGS, backWings);
    }

    /** Tekstura skrzydel na plecach albo null, gdy wylaczone. */
    public static Identifier backWingsTexture() {
        if (!backWingsActive()) return null;
        return Identifier.of("myclient", "textures/wings3d/" + backWings + ".png");
    }

    /** Tekstura skrzydel (elytry) albo null, gdy wylaczona. */
    public static Identifier wingsTexture() {
        if (wings.equals("none") || !isValid(WINGS, wings)) return null;
        return Identifier.of("myclient", "textures/wings/" + wings + ".png");
    }
}
