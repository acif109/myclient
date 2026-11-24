package com.example.myclient;

public class Module {
    public final String name;
    public final String category;
    public boolean enabled;
    public boolean favorite = false;
    public float anim = -1f; // animacja przelacznika w GUI
    public float hover = 0f; // animacja podswietlenia karty
    public String desc = "";

    public Module desc(String d) {
        this.desc = d;
        return this;
    }

    // pozycja na ekranie (HUD)
    public final boolean hud;
    public final int defX;
    public final int defY;
    public int x;
    public int y;

    /** Modul bez elementu na ekranie (np. zoom). */
    public Module(String name, boolean enabled, String category) {
        this.name = name;
        this.enabled = enabled;
        this.category = category;
        this.hud = false;
        this.defX = 0;
        this.defY = 0;
    }

    /** Modul z elementem na ekranie, ktory mozna przesuwac. */
    public Module(String name, boolean enabled, String category, int defX, int defY) {
        this.name = name;
        this.enabled = enabled;
        this.category = category;
        this.hud = true;
        this.defX = defX;
        this.defY = defY;
        this.x = defX;
        this.y = defY;
    }

    public void resetPosition() {
        this.x = defX;
        this.y = defY;
    }
}
